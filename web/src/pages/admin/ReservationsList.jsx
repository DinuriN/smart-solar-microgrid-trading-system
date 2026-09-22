import { useEffect, useState, useCallback } from "react";
import {
  getBackOfficeQueue,
  getOperatorReservations,
  approveReservation,
  blockReservation,
} from "../../api/reservationService";
import StatusBadge from "../../components/reservations/StatusBadge";
import ReservationFormModal from "../../components/reservations/ReservationFormModal";
import CancelConfirmModal from "../../components/reservations/CancelConfirmModal";
import { usePageHeader } from "../../context/PageHeaderContext";

const FILTERS = ["All", "Pending", "Approved"];

export default function ReservationsList() {
  const role = localStorage.getItem("userRole"); // "BackOfficeUser" or "GridOperator"
  const operatorId = localStorage.getItem("userId"); 

  const [reservations, setReservations] = useState([]);
  const [filter, setFilter] = useState("All");
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [formModal, setFormModal] = useState(null); // { mode, reservation } | null
  const [cancelTarget, setCancelTarget] = useState(null);

  const isBackOffice = role === "BackOfficeUser";

  const { setHeader } = usePageHeader();

  useEffect(() => {
    setHeader({
      title: "Reservations",
      breadcrumb: `solara-grid / ${isBackOffice ? "operations" : "field-ops"} / reservations`,
    });
  }, [isBackOffice, setHeader]);

  const loadReservations = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const status = filter === "All" ? undefined : filter;
      const response = isBackOffice
        ? await getBackOfficeQueue(status)
        : await getOperatorReservations(operatorId, status);
      setReservations(response.data);
    } catch {
      setError("Could not load reservations.");
    } finally {
      setLoading(false);
    }
  }, [filter, isBackOffice, operatorId]);

  useEffect(() => {
    loadReservations();
  }, [loadReservations]);

  const filteredBySearch = reservations.filter((r) =>
    `${r.prosumerNic} ${r.nodeId}`.toLowerCase().includes(search.toLowerCase())
  );

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

  const isLocked = (scheduledDateTime) => {
    const hoursUntil = (new Date(scheduledDateTime) - new Date()) / (1000 * 60 * 60);
    return hoursUntil < 12;
  };

  return (
    <div className="p-6">
      <div className="mb-4 rounded border border-amber-500/30 bg-amber-500/5 px-4 py-3 text-sm text-slate-300">
        <span className="font-semibold text-amber-400">Scheduling rule:</span> new reservations must
        fall within the next 7 days.{" "}
        <span className="font-semibold text-amber-400">Modification rule:</span> updates or
        cancellations require at least 12 hours' notice.
      </div>

      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search by NIC or node…"
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
              <th className="px-4 py-3">Scheduled</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody>
            {loading && (
              <tr><td colSpan={6} className="px-4 py-6 text-center text-slate-500">Loading…</td></tr>
            )}

            {!loading && filteredBySearch.length === 0 && (
              <tr><td colSpan={6} className="px-4 py-6 text-center text-slate-500">No reservations found.</td></tr>
            )}

            {!loading && filteredBySearch.map((r) => {
              const locked = isLocked(r.scheduledDateTime);
              return (
                <tr key={r.id} className="border-b border-slate-800 text-slate-200">
                  <td className="px-4 py-3 font-mono text-xs text-slate-400">{r.id}</td>
                  <td className="px-4 py-3">{r.prosumerNic}</td>
                  <td className="px-4 py-3">{r.nodeId}</td>
                  <td className="px-4 py-3 font-mono text-xs">
                    {new Date(r.scheduledDateTime).toLocaleString()}
                  </td>
                  <td className="px-4 py-3">
                    {locked && r.status === "Pending" ? (
                      <StatusBadge status="Blocked" />
                    ) : (
                      <StatusBadge status={r.status} />
                    )}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex justify-end gap-2">
                      {isBackOffice && r.status === "Pending" && (
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
                      )}

                      {!isBackOffice && ["Pending", "Approved"].includes(r.status) && (
                        <>
                          <button
                            disabled={locked}
                            onClick={() => setFormModal({ mode: "edit", reservation: r })}
                            className="rounded border border-slate-700 px-3 py-1 text-xs text-slate-300 hover:border-slate-500 disabled:opacity-40"
                          >
                            Modify
                          </button>
                          <button
                            disabled={locked}
                            onClick={() => setCancelTarget(r)}
                            className="rounded border border-rose-600 px-3 py-1 text-xs text-rose-400 hover:bg-rose-600/10 disabled:opacity-40"
                          >
                            Cancel
                          </button>
                        </>
                      )}

                      {["Completed", "Cancelled", "Blocked"].includes(r.status) && (
                        <span className="text-xs text-slate-600">—</span>
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