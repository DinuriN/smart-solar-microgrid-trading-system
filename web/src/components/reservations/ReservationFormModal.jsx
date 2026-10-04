import { useState, useEffect } from "react";
import {
  createReservation,
  updateReservation,
  getAvailableSlots,
} from "../../api/reservationService";
import { nodeService } from "../../api/nodeService";

const RESERVATION_TYPES = ["Charging", "EnergyDropOff"];

// Helper to format current local date/time for datetime-local input (YYYY-MM-DDTHH:mm)
const getDefaultDateTime = () => {
  const now = new Date();
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
  return now.toISOString().slice(0, 16);
};

// Converts the API's UTC time into local time for the datetime-local input
const toLocalInput = (iso) => {
  const d = new Date(iso);
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 16);
};

// Text shown for a slot in the dropdown
const slotLabel = (slot) =>
  slot.current
    ? "Current slot"
    : `${new Date(slot.startTime).toLocaleString()} - ${new Date(slot.endTime).toLocaleString()}`;

export default function ReservationFormModal({
  mode, // "create" or "edit"
  reservation, // required when mode === "edit"
  onClose,
  onSaved,
}) {
  const isOperator = localStorage.getItem("userRole") === "GridOperator";

  // Form State
  const [nodes, setNodes] = useState([]);
  const [nodeId, setNodeId] = useState(reservation?.nodeId ?? "");
  const [prosumerNic, setProsumerNic] = useState(reservation?.prosumerNic ?? "");
  const [scheduledDateTime, setScheduledDateTime] = useState(
    reservation?.scheduledDateTime ? toLocalInput(reservation.scheduledDateTime) : getDefaultDateTime()
  );
  const [batterySlotId, setBatterySlotId] = useState(reservation?.batterySlotId ?? "");
  const [type, setType] = useState(reservation?.type ?? RESERVATION_TYPES[0]);

  // Dynamic slot state
  const [slots, setSlots] = useState([]);
  const [loadingSlots, setLoadingSlots] = useState(false);

  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  // Load the microgrid nodes for the node dropdown
  useEffect(() => {
    nodeService
      .getAll()
      .then((list) => setNodes(list ?? []))
      .catch(() => setError("Failed to load microgrid nodes."));
  }, []);

  // Fetch available slots from API whenever nodeId or scheduledDateTime changes
  useEffect(() => {
    if (!nodeId || !scheduledDateTime) {
      setSlots([]);
      return;
    }

    async function fetchSlots() {
      setLoadingSlots(true);
      setError("");
      try {
        const isoDateTime = new Date(scheduledDateTime).toISOString();
        const response = await getAvailableSlots(nodeId, isoDateTime);
        const freeSlots = response.data?.data ?? [];

        // Edit mode: the booking's own slot is Booked, so it is not in the free list - keep it as an option
        const ownSlotId = mode === "edit" ? reservation?.batterySlotId : null;
        const list = ownSlotId
          ? [{ id: ownSlotId, current: true }, ...freeSlots.filter((s) => s.id !== ownSlotId)]
          : freeSlots;
        setSlots(list);

        // Create mode: auto-select the first free slot
        if (mode === "create") {
          setBatterySlotId(list[0]?.id ?? "");
        }
      } catch {
        setError("Failed to load available battery slots for the selected date & time.");
      } finally {
        setLoadingSlots(false);
      }
    }

    fetchSlots();
  }, [nodeId, scheduledDateTime, mode, reservation]);

  useEffect(() => {
    if (mode === "edit" && reservation) {
      setBatterySlotId(reservation.batterySlotId);
      setType(reservation.type);
      setScheduledDateTime(
        reservation.scheduledDateTime ? toLocalInput(reservation.scheduledDateTime) : getDefaultDateTime()
      );
    }
  }, [reservation, mode]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (isOperator && mode === "create" && !prosumerNic.trim()) {
      setError("Prosumer NIC is required when creating a reservation as an operator.");
      return;
    }

    if (!nodeId) {
      setError("Please select a microgrid node.");
      return;
    }

    if (!scheduledDateTime) {
      setError("Please select a scheduled date and time.");
      return;
    }

    if (!batterySlotId) {
      setError("Please select an available battery slot.");
      return;
    }

    setSaving(true);

    const payload = {
      nodeId,
      batterySlotId,
      type,
      scheduledDateTime: new Date(scheduledDateTime).toISOString(),
      ...(isOperator && mode === "create" ? { prosumerNic: prosumerNic.trim() } : {}),
    };

    try {
      if (mode === "create") {
        await createReservation(payload);
      } else {
        await updateReservation(reservation.id, payload);
      }
      onSaved();
      onClose();
    } catch (err) {
      setError(
        err.response?.data?.message ?? "Something went wrong. Please try again."
      );
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 px-4">
      <div className="w-full max-w-md rounded-lg border border-slate-700 bg-slate-900 p-6">
        <h2 className="mb-1 text-lg font-semibold text-slate-100">
          {mode === "create" ? "Create Reservation" : "Modify Reservation"}
        </h2>
        <p className="mb-5 text-xs text-slate-400">
          Reservations must fall within the next 7 days. Changes need at least 12 hours' notice.
        </p>

        {error && (
          <div className="mb-4 rounded border border-rose-500/40 bg-rose-500/10 px-3 py-2 text-sm text-rose-300">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* 1. Microgrid Node (locked when modifying - the node cannot be changed) */}
          <div>
            <label className="mb-1 block text-xs font-medium text-slate-400">
              Microgrid Node <span className="text-rose-400">*</span>
            </label>
            <select
              value={nodeId}
              onChange={(e) => setNodeId(e.target.value)}
              disabled={mode === "edit"}
              className="w-full rounded border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 outline-none focus:border-amber-500 disabled:cursor-not-allowed disabled:opacity-50"
            >
              <option value="">Select a node</option>
              {nodes.map((node) => (
                <option key={node.id} value={node.id}>
                  {node.nodeName}
                </option>
              ))}
            </select>
          </div>

          {/* 2. Prosumer NIC (Operator Mode) */}
          {isOperator && mode === "create" && (
            <div>
              <label className="mb-1 block text-xs font-medium text-slate-400">
                Prosumer NIC <span className="text-rose-400">*</span>
              </label>
              <input
                type="text"
                required
                value={prosumerNic}
                onChange={(e) => setProsumerNic(e.target.value)}
                placeholder="e.g. 199912345678"
                className="w-full rounded border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 outline-none focus:border-amber-500"
              />
            </div>
          )}

          {/* 3. Scheduled Date & Time */}
          <div>
            <label className="mb-1 block text-xs font-medium text-slate-400">
              Scheduled Date & Time <span className="text-rose-400">*</span>
            </label>
            <input
              type="datetime-local"
              required
              value={scheduledDateTime}
              onChange={(e) => setScheduledDateTime(e.target.value)}
              className="w-full rounded border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 outline-none focus:border-amber-500"
            />
          </div>

          {/* 4. Battery Slot Dropdown */}
          <div>
            <label className="mb-1 block text-xs font-medium text-slate-400">
              Battery Slot <span className="text-rose-400">*</span>
            </label>
            <select
              value={batterySlotId}
              onChange={(e) => setBatterySlotId(e.target.value)}
              disabled={loadingSlots || !scheduledDateTime}
              className="w-full rounded border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 outline-none focus:border-amber-500 disabled:opacity-50"
            >
              <option value="">
                {!scheduledDateTime
                  ? "Select date & time first"
                  : loadingSlots
                  ? "Loading available slots…"
                  : slots.length === 0
                  ? "No slots available at this time"
                  : "Select a slot"}
              </option>
              {slots.map((slot) => (
                <option key={slot.id} value={slot.id}>
                  {slotLabel(slot)}
                </option>
              ))}
            </select>
          </div>

          {/* 5. Type Dropdown */}
          <div>
            <label className="mb-1 block text-xs font-medium text-slate-400">Type</label>
            <select
              value={type}
              onChange={(e) => setType(e.target.value)}
              className="w-full rounded border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 outline-none focus:border-amber-500"
            >
              {RESERVATION_TYPES.map((t) => (
                <option key={t} value={t}>
                  {t}
                </option>
              ))}
            </select>
          </div>

          {/* Action Buttons */}
          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              disabled={saving}
              className="rounded border border-slate-700 px-4 py-2 text-sm text-slate-300 hover:border-slate-500 disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving || loadingSlots || !batterySlotId}
              className="rounded bg-amber-500 px-4 py-2 text-sm font-semibold text-slate-900 hover:brightness-110 disabled:opacity-50"
            >
              {saving ? "Saving…" : mode === "create" ? "Create Reservation" : "Save Changes"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}