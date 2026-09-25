import { useState } from "react";
import { cancelReservation } from "../../api/reservationService";

export default function CancelConfirmModal({ reservation, onClose, onCancelled }) {
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleConfirm = async () => {
    setError("");
    setLoading(true);
    try {
      await cancelReservation(reservation.id);
      onCancelled();
      onClose();
    } catch (err) {
      setError(err.response?.data?.message ?? "Could not cancel this reservation.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 px-4">
      <div className="w-full max-w-sm rounded-lg border border-slate-700 bg-slate-900 p-6">
        <h2 className="mb-2 text-lg font-semibold text-slate-100">Cancel reservation?</h2>
        <p className="mb-4 text-sm text-slate-400">
          {reservation.id} for {reservation.prosumerNic} at {reservation.nodeId} will be cancelled.
          This cannot be undone.
        </p>

        {error && (
          <div className="mb-4 rounded border border-rose-500/40 bg-rose-500/10 px-3 py-2 text-sm text-rose-300">
            {error}
          </div>
        )}

        <div className="flex justify-end gap-2">
          <button
            onClick={onClose}
            className="rounded border border-slate-700 px-4 py-2 text-sm text-slate-300 hover:border-slate-500"
          >
            Keep reservation
          </button>
          <button
            onClick={handleConfirm}
            disabled={loading}
            className="rounded bg-rose-600 px-4 py-2 text-sm font-semibold text-white hover:brightness-110 disabled:opacity-50"
          >
            {loading ? "Cancelling…" : "Cancel reservation"}
          </button>
        </div>
      </div>
    </div>
  );
}