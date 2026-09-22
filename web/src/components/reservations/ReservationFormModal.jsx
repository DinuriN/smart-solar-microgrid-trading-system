import { useState, useEffect } from "react";
import { createReservation, updateReservation } from "../../api/reservationService";

const RESERVATION_TYPES = ["Charging", "EnergyDropOff"];

export default function ReservationFormModal({
  mode,          // "create" or "edit"
  reservation,   // required when mode === "edit"
  onClose,
  onSaved,
}) {
  const [nodeId, setNodeId] = useState(reservation?.nodeId ?? "");
  const [batterySlotId, setBatterySlotId] = useState(reservation?.batterySlotId ?? "");
  const [type, setType] = useState(reservation?.type ?? RESERVATION_TYPES[0]);
  const [scheduledDateTime, setScheduledDateTime] = useState(
    reservation?.scheduledDateTime?.slice(0, 16) ?? ""
  );
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    // Reset fields if a different reservation is passed in while the modal is open
    if (mode === "edit" && reservation) {
      setNodeId(reservation.nodeId);
      setBatterySlotId(reservation.batterySlotId);
      setType(reservation.type);
      setScheduledDateTime(reservation.scheduledDateTime?.slice(0, 16) ?? "");
    }
  }, [reservation, mode]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSaving(true);

    const payload = {
      nodeId,
      batterySlotId,
      type,
      scheduledDateTime: new Date(scheduledDateTime).toISOString(),
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
      // Surfaces the backend's rule violation message directly
      // (e.g. "Reservations must be scheduled within the next 7 days.")
      setError(err.response?.data?.message ?? "Something went wrong. Please try again.");
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
          <div>
            <label className="mb-1 block text-xs text-slate-400">Node ID</label>
            <input
              value={nodeId}
              onChange={(e) => setNodeId(e.target.value)}
              required
              className="w-full rounded border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 outline-none focus:border-emerald-500"
              placeholder="e.g. Kilinochchi-North"
            />
          </div>

          <div>
            <label className="mb-1 block text-xs text-slate-400">Battery Slot ID</label>
            <input
              value={batterySlotId}
              onChange={(e) => setBatterySlotId(e.target.value)}
              required
              className="w-full rounded border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 outline-none focus:border-emerald-500"
              placeholder="e.g. slot-04"
            />
          </div>

          <div>
            <label className="mb-1 block text-xs text-slate-400">Type</label>
            <select
              value={type}
              onChange={(e) => setType(e.target.value)}
              className="w-full rounded border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 outline-none focus:border-emerald-500"
            >
              {RESERVATION_TYPES.map((t) => (
                <option key={t} value={t}>{t}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="mb-1 block text-xs text-slate-400">Scheduled Date &amp; Time</label>
            <input
              type="datetime-local"
              value={scheduledDateTime}
              onChange={(e) => setScheduledDateTime(e.target.value)}
              required
              className="w-full rounded border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-slate-100 outline-none focus:border-emerald-500"
            />
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="rounded border border-slate-700 px-4 py-2 text-sm text-slate-300 hover:border-slate-500"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
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