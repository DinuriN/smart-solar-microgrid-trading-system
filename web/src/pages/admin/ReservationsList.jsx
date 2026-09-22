import { useEffect, useState, useCallback } from "react";
import {
  getBackOfficeQueue,
  getOperatorReservations,
  searchReservations,
  approveReservation,
  blockReservation,
} from "../../api/reservationService";
import StatusBadge from "../../components/reservations/StatusBadge";
import ReservationFormModal from "../../components/reservations/ReservationFormModal";
import CancelConfirmModal from "../../components/reservations/CancelConfirmModal";
import { usePageHeader } from "../../context/PageHeaderContext";

const FILTERS = ["All", "Pending", "Approved", "Cancelled"];

export default function ReservationsList() {
  const role = localStorage.getItem("userRole"); // "BackOfficeUser" or "GridOperator"
  const operatorId = localStorage.getItem("userId");

  const [reservations, setReservations] = useState([]);
  const [filter, setFilter] = useState("All");
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [formModal, setFormModal] = useState(null); 
  const [cancelTarget, setCancelTarget] = useState(null);

  // Dynamic assigned node ID derived from operator reservations
  const [assignedNodeId, setAssignedNodeId] = useState("");

  const isBackOffice = role === "BackOfficeUser";

  const { setHeader } = usePageHeader();

  useEffect(() => {
    setHeader({
      title: "Reservations",
      breadcrumb: `solara-grid / ${isBackOffice ? "operations" : "field-ops"} / reservations`,
    });
  }, [isBackOffice, setHeader]);

  // Load reservations directly from backend API
  const loadReservations = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      let response;
      const statusParam = filter === "All" ? undefined : filter;

      if (search.trim()) {
        // Delegate both text search and status filter parameters to backend database query
        response = await searchReservations(search.trim(), statusParam);
      } else {
        // Fallback to role-specific list endpoints when search box is empty
        response = isBackOffice
          ? await getBackOfficeQueue(statusParam)
          : await getOperatorReservations(operatorId, statusParam);
      }

      const fetchedData = response.data ?? [];
      setReservations(fetchedData);

      // Automatically set the assignedNodeId from the first reservation record
      if (!isBackOffice && fetchedData.length > 0 && fetchedData[0]?.nodeId) {
        setAssignedNodeId(fetchedData[0].nodeId);
      }
    } catch {
      setError("Could not load reservations.");
    } finally {
      setLoading(false);
    }
  }, [search, filter, isBackOffice, operatorId]);

  // Debounced trigger (300ms delay to prevent excessive API requests while typing)
  useEffect(() => {
    const timer = setTimeout(() => {
      loadReservations();
    }, 300);

    return () => clearTimeout(timer);
  }, [loadReservations]);

  const handleApprove = async (id) => {
    try {
      await approveReservation(id);
      loadReservations();
    } catch (err) {
      setError(err.response?.data?.message ?? "Could not approve reservation.");
    }
  };

  const handleBlock = async (id) => {
    try {
      await blockReservation(id, "Blocked by BackOffice");
      loadReservations();
    } catch (err) {
      setError(err.response?.data?.message ?? "Could not block reservation.");
    }
  };

  return (
    <div className="p-6">
      {!isBackOffice && (
        <div className="mb-4 rounded border border-amber-500/30 bg-amber-500/5 px-4 py-3 text-sm text-slate-300">
          <span className="font-semibold text-amber-400">Scheduling rule:</span> new reservations must
          fall within the next 7 days.{" "}
          <span className="font-semibold text-amber-400">Modification rule:</span> updates or
          cancellations require at least 12 hours' notice.
        </div>
      )}

      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search by NIC, node, or slot…"
          className="w-64 rounded border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-200 outline-none focus:border-amber-500"
        />

        <div className="flex items-center gap-2">
          <div className="flex gap-2">
            {FILTERS.map((f) => (
              <button
                key={f}
                onClick={() => setFilter(f)}
                className={`rounded-full border px-3 py-1 text-xs ${
                  filter === f
                    ? "border-emerald-500 text-emerald-400"
                    : "border-slate-700 text-slate-400 hover:border-slate-500"
                }`}
              >
                {f}
              </button>
            ))}
          </div>

          {!isBackOffice && (
            <button
              onClick={() => setFormModal({ mode: "create" })}
              className="rounded bg-amber-500 px-4 py-2 text-xs font-semibold text-slate-900 hover:brightness-110"
            >
              + Create Reservation
            </button>
          )}
        </div>
      </div>

      {error && (
        <div className="mb-4 rounded border border-rose-500/40 bg-rose-500/10 px-3 py-2 text-sm text-rose-300">
          {error}
        </div>
      )}

      <div className="overflow-hidden rounded border border-slate-800">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-800 text-left text-xs text-slate-500">
              <th className="px-4 py-3">Reservation</th>
              <th className="px-4 py-3">Prosumer</th>
              <th className="px-4 py-3">Node</th>
              <th className="px-4 py-3">Slot</th>
              <th className="px-4 py-3">Scheduled</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody>
            {loading && (
              <tr>
                <td colSpan={7} className="px-4 py-6 text-center text-slate-500">
                  Loading…
                </td>
              </tr>
            )}

            {!loading && reservations.length === 0 && (
              <tr>
                <td colSpan={7} className="px-4 py-6 text-center text-slate-500">
                  No reservations found.
                </td>
              </tr>
            )}

            {!loading &&
              reservations.map((r) => {
                const isInactive = ["Completed", "Cancelled", "Blocked"].includes(r.status);

                return (
                  <tr key={r.id} className="border-b border-slate-800 text-slate-200">
                    <td className="px-4 py-3 font-mono text-xs text-slate-400">{r.id}</td>
                    <td className="px-4 py-3 font-semibold">{r.prosumerNic}</td>
                    <td className="px-4 py-3">{r.nodeId}</td>
                    <td className="px-4 py-3 font-mono text-xs text-amber-400">
                      {r.batterySlotId || "N/A"}
                    </td>
                    <td className="px-4 py-3 font-mono text-xs">
                      {new Date(r.scheduledDateTime).toLocaleString()}
                    </td>
                    <td className="px-4 py-3">
                      <StatusBadge status={r.status} />
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-2">
                        {isBackOffice ? (
                          r.status === "Pending" ? (
                            <>
                              <button
                                onClick={() => handleApprove(r.id)}
                                className="rounded border border-emerald-600 px-3 py-1 text-xs text-emerald-400 hover:bg-emerald-600/10"
                              >
                                Approve
                              </button>
                              <button
                                onClick={() => handleBlock(r.id)}
                                className="rounded border border-rose-600 px-3 py-1 text-xs text-rose-400 hover:bg-rose-600/10"
                              >
                                Block
                              </button>
                            </>
                          ) : (
                            <>
                              <button
                                disabled
                                className="rounded border border-slate-800 px-3 py-1 text-xs text-slate-600 opacity-40 cursor-not-allowed"
                              >
                                Approve
                              </button>
                              <button
                                disabled
                                className="rounded border border-slate-800 px-3 py-1 text-xs text-slate-600 opacity-40 cursor-not-allowed"
                              >
                                Block
                              </button>
                            </>
                          )
                        ) : (
                          <>
                            <button
                              disabled={isInactive}
                              onClick={() => setFormModal({ mode: "edit", reservation: r })}
                              className="rounded border border-slate-700 px-3 py-1 text-xs text-slate-300 hover:border-slate-500 disabled:border-slate-800 disabled:text-slate-600 disabled:opacity-40 disabled:hover:border-slate-800 disabled:cursor-not-allowed"
                            >
                              Modify
                            </button>
                            <button
                              disabled={isInactive}
                              onClick={() => setCancelTarget(r)}
                              className="rounded border border-rose-600 px-3 py-1 text-xs text-rose-400 hover:bg-rose-600/10 disabled:border-slate-800 disabled:text-slate-600 disabled:hover:bg-transparent disabled:opacity-40 disabled:cursor-not-allowed"
                            >
                              Cancel
                            </button>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
          </tbody>
        </table>
      </div>

      {formModal && (
        <ReservationFormModal
          mode={formModal.mode}
          reservation={formModal.reservation}
          assignedNodeId={assignedNodeId}
          onClose={() => setFormModal(null)}
          onSaved={loadReservations}
        />
      )}

      {cancelTarget && (
        <CancelConfirmModal
          reservation={cancelTarget}
          onClose={() => setCancelTarget(null)}
          onCancelled={loadReservations}
        />
      )}
    </div>
  );
}
