import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getDashboardStats } from '../../api/dashboardService';
import { usePageHeader } from '../../context/PageHeaderContext';

export default function OperatorDashboard() {
  const { setHeader } = usePageHeader();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function refreshStats() {
    setLoading(true);
    setError('');
    try {
      const { data } = await getDashboardStats();
      if (typeof data?.pendingReservations !== 'number' ||
          typeof data?.approvedFutureReservations !== 'number') {
        throw new Error('Unexpected dashboard response');
      }
      setStats(data);
    } catch {
      setError('Could not load reservation totals. Check the API and try again.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    setHeader({
      title: 'Grid Operations Center',
      breadcrumb: 'solara-grid / operator / dashboard',
    });
    refreshStats();
  }, []);

  const cards = [
    {
      label: 'Pending reservations',
      value: stats?.pendingReservations,
      detail: 'Awaiting BackOffice approval',
      tone: 'text-amber-400',
    },
    {
      label: 'Upcoming approved reservations',
      value: stats?.approvedFutureReservations,
      detail: 'Scheduled after the current time',
      tone: 'text-teal-400',
    },
  ];

  return (
    <div className="mx-auto w-full max-w-5xl space-y-6">
      <section className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-white">Grid operations</h1>
          <p className="mt-1 text-sm text-slate-400">
            System reservation totals across all nodes.
          </p>
        </div>
        <button
          type="button"
          onClick={refreshStats}
          disabled={loading}
          className="rounded border border-amber-500/50 bg-amber-500/10 px-4 py-2 text-sm text-amber-400 hover:bg-amber-500/20 disabled:opacity-50"
        >
          {loading ? 'Refreshing…' : 'Refresh stats'}
        </button>
      </section>

      {error && <p role="alert" className="rounded border border-rose-700 bg-rose-950/40 p-4 text-sm text-rose-300">{error}</p>}

      <section className="grid grid-cols-1 gap-4 sm:grid-cols-2" aria-label="Reservation statistics">
        {cards.map((card) => (
          <article key={card.label} className="rounded border border-slate-800 bg-slate-900 p-6 shadow-sm">
            <p className="text-sm text-slate-400">{card.label}</p>
            <p className={`mt-3 font-mono text-4xl font-semibold ${card.tone}`}>
              {loading || error ? '—' : card.value}
            </p>
            <p className="mt-2 text-xs text-slate-500">{card.detail}</p>
          </article>
        ))}
      </section>

      <section className="rounded border border-slate-800 bg-slate-900 p-6">
        <h2 className="text-lg font-semibold text-white">Operator reservations</h2>
        <p className="mt-2 text-sm text-slate-400">
          View bookings, search by NIC or node, and check their current status.
        </p>
        <Link
          to="/operator/reservations"
          className="mt-5 inline-flex rounded bg-amber-500 px-4 py-2 text-sm font-medium text-slate-950 hover:bg-amber-400"
        >
          View reservations
        </Link>
      </section>
    </div>
  );
}
