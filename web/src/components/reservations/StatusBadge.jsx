const STATUS_STYLES = {
  Pending: "bg-amber-500/10 text-amber-400 border-amber-500/30",
  Approved: "bg-emerald-500/10 text-emerald-400 border-emerald-500/30",
  Blocked: "bg-rose-500/10 text-rose-400 border-rose-500/30",
  Completed: "bg-slate-500/10 text-slate-400 border-slate-500/30",
  Cancelled: "bg-slate-500/10 text-slate-500 border-slate-500/30",
};

export default function StatusBadge({ status }) {
  const style = STATUS_STYLES[status] ?? STATUS_STYLES.Pending;
  return (
    <span
      className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-mono ${style}`}
    >
      {status.toLowerCase()}
    </span>
  );
}