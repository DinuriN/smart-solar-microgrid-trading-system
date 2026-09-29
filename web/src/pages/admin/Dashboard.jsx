import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getBackOfficeQueue } from '../../api/reservationService';
import { usePageHeader } from '../../context/PageHeaderContext';

const nodes = [
  {
    name: 'Kilinochchi-North',
    location: '9.3961°N, 80.3986°E',
    capacity: '42.5 kW/h',
    slots: '6/8 slots free',
  },
  {
    name: 'Jaffna-Hub-2',
    location: '9.6685°N, 80.0074°E',
    capacity: '30.0 kW/h',
    slots: '2/6 slots free',
  },
  {
    name: 'Vavuniya-East',
    location: '8.7514°N, 80.4971°E',
    capacity: '55.0 kW/h',
    slots: '8/10 slots free',
  },
  {
    name: 'Mannar-Coastal',
    location: '8.9810°N, 79.9044°E',
    capacity: '18.0 kW/h',
    slots: '0/4 slots free',
  },
];

const statusClasses = {
  Pending: 'bg-amber-500/10 text-amber-400',
  Approved: 'bg-teal-500/10 text-teal-400',
  Cancelled: 'bg-rose-500/15 text-rose-400',
  Blocked: 'bg-rose-500/15 text-rose-400',
  Completed: 'bg-slate-500/15 text-slate-400',
};

function PanelHeader({ title, subtitle, action }) {
  return (
    <div className="flex items-center justify-between gap-4 border-b border-slate-800 px-5 py-4">
      <div>
        <h2 className="text-sm font-semibold text-white">{title}</h2>
        <p className="mt-0.5 text-xs text-slate-500">{subtitle}</p>
      </div>
      {action}
    </div>
  );
}

export default function Dashboard() {
  const { setHeader } = usePageHeader();
  const navigate = useNavigate();

  const [pendingCount, setPendingCount] = useState('...');
  const [recentReservations, setRecentReservations] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setHeader({
      title: "System Dashboard",
      breadcrumb: "solara-grid / administration / dashboard"
    });
    async function fetchDashboardData() {
      setLoading(true);
      try {
        // Fetch pending reservations for Backoffice from the API
        const response = await getBackOfficeQueue('Pending');
        const pendingData = response.data ?? [];

        setPendingCount(pendingData.length.toString());
        setRecentReservations(pendingData.slice(0, 5)); // Take top 5 for queue preview
      } catch (err) {
        setPendingCount('0');
      } finally {
        setLoading(false);
      }
    }

    fetchDashboardData();
  }, []);

  const stats = [
    {
      label: 'Active nodes',
      value: '8',
      unit: '/ 9',
      detail: '▲ all reporting normally',
      tone: 'text-teal-400',
    },
    {
      label: 'Pending reservations',
      value: pendingCount,
      detail: 'Awaiting backoffice review',
      tone: 'text-amber-400',
    },
    {
      label: 'Approved — next 7 days',
      value: '37',
      detail: '▲ 12% vs last week',
      tone: 'text-teal-400',
    },
    {
      label: 'Pending deactivations',
      value: '3',
      detail: 'awaiting backoffice action',
      tone: 'text-amber-400',
    },
  ];

  return (
    <div className="mx-auto w-full max-w-[1440px]">
      <section className="mb-7 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {stats.map((stat) => (
          <article
            key={stat.label}
            className="min-h-28 rounded border border-slate-800 bg-slate-900 px-5 py-4 shadow-sm"
          >
            <p className="text-xs text-slate-400">{stat.label}</p>
            <p className="mt-2 font-mono text-3xl font-semibold tracking-tight text-white">
              {stat.value}
              {stat.unit && (
                <span className="ml-1 text-sm font-normal text-slate-500">{stat.unit}</span>
              )}
            </p>
            <p className={`mt-1.5 font-mono text-[11px] ${stat.tone}`}>{stat.detail}</p>
          </article>
        ))}
      </section>

      <section className="grid grid-cols-1 gap-5 xl:grid-cols-[1.5fr_1fr]">
        <article className="overflow-hidden rounded border border-slate-800 bg-slate-900 shadow-sm">
          <PanelHeader
            title="Reservation queue"
            subtitle="Slots awaiting confirmation"
            action={
              <button
                type="button"
                onClick={() => navigate('/admin/reservations')}
                className="rounded border border-amber-500/50 bg-amber-500/10 px-3 py-1.5 text-xs text-amber-400 hover:bg-amber-500/20"
              >
                View all
              </button>
            }
          />

          <div className="overflow-x-auto">
            <table className="w-full min-w-[650px] border-collapse text-left text-sm">
              <thead>
                <tr className="border-b border-slate-800 text-[11px] font-medium text-slate-500">
                  <th className="px-5 py-3 font-medium">Prosumer NIC</th>
                  <th className="px-5 py-3 font-medium">Node</th>
                  <th className="px-5 py-3 font-medium">Scheduled</th>
                  <th className="px-5 py-3 font-medium">Status</th>
                </tr>
              </thead>
              <tbody>
                {loading && (
                  <tr>
                    <td colSpan={4} className="px-5 py-6 text-center text-slate-500">
                      Loading pending reservations…
                    </td>
                  </tr>
                )}

                {!loading && recentReservations.length === 0 && (
                  <tr>
                    <td colSpan={4} className="px-5 py-6 text-center text-slate-500">
                      No pending reservations found.
                    </td>
                  </tr>
                )}

                {!loading &&
                  recentReservations.map((r) => (
                    <tr
                      key={r.id}
                      className="border-b border-slate-800/90 transition-colors last:border-0 hover:bg-slate-800/45"
                    >
                      <td className="whitespace-nowrap px-5 py-3.5 font-mono text-xs text-white">
                        {r.prosumerNic}
                      </td>
                      <td className="whitespace-nowrap px-5 py-3.5 text-slate-200">{r.nodeId}</td>
                      <td className="whitespace-nowrap px-5 py-3.5 font-mono text-xs text-slate-400">
                        {new Date(r.scheduledDateTime).toLocaleString()}
                      </td>
                      <td className="whitespace-nowrap px-5 py-3.5">
                        <span
                          className={`inline-flex rounded-full px-2.5 py-1 font-mono text-[10px] ${
                            statusClasses[r.status] || 'bg-slate-800 text-slate-400'
                          }`}
                        >
                          {r.status}
                        </span>
                      </td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        </article>

        <article className="overflow-hidden rounded border border-slate-800 bg-slate-900 shadow-sm">
          <PanelHeader title="Node capacity" subtitle="Live battery availability" />

          <div>
            {nodes.map((node) => (
              <div
                key={node.name}
                className="flex items-center justify-between gap-5 border-b border-slate-800 px-5 py-4 last:border-0 hover:bg-slate-800/45"
              >
                <div className="min-w-0">
                  <p className="truncate text-sm font-medium text-white">{node.name}</p>
                  <p className="mt-1 truncate font-mono text-[11px] text-slate-500">
                    {node.location}
                  </p>
                </div>
                <div className="shrink-0 text-right">
                  <p className="font-mono text-sm text-teal-400">{node.capacity}</p>
                  <p className="mt-1 text-[11px] text-slate-500">{node.slots}</p>
                </div>
              </div>
            ))}
          </div>
        </article>
      </section>
    </div>
  );
}