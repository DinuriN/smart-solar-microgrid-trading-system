import React from 'react';

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
    value: '14',
    detail: '5 need review within 12h',
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

const reservations = [
  {
    name: 'H. Fernando',
    nic: '951xxxxxxxV',
    node: 'Kilinochchi-North',
    scheduled: 'Sep 21, 14:00',
    status: 'pending',
  },
  {
    name: 'N. Silva',
    nic: '882xxxxxxxV',
    node: 'Jaffna-Hub-2',
    scheduled: 'Sep 20, 09:30',
    status: 'pending',
  },
  {
    name: 'R. Kumar',
    nic: '970xxxxxxxV',
    node: 'Vavuniya-East',
    scheduled: 'Sep 22, 16:00',
    status: 'approved',
  },
  {
    name: 'S. Jayasuriya',
    nic: '915xxxxxxxV',
    node: 'Kilinochchi-North',
    scheduled: 'Sep 19, 11:00',
    status: 'expiring',
  },
];

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
  pending: 'bg-amber-500/10 text-amber-400',
  approved: 'bg-teal-500/10 text-teal-400',
  expiring: 'bg-rose-500/15 text-rose-400',
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
            action={(
              <button
                type="button"
                disabled
                title="The full reservations page will be connected next"
                className="cursor-not-allowed rounded border border-slate-700 px-3 py-1.5 text-xs text-slate-500"
              >
                View all
              </button>
            )}
          />

          <div className="overflow-x-auto">
            <table className="w-full min-w-[650px] border-collapse text-left text-sm">
              <thead>
                <tr className="border-b border-slate-800 text-[11px] font-medium text-slate-500">
                  <th className="px-5 py-3 font-medium">Prosumer</th>
                  <th className="px-5 py-3 font-medium">Node</th>
                  <th className="px-5 py-3 font-medium">Scheduled</th>
                  <th className="px-5 py-3 font-medium">Status</th>
                </tr>
              </thead>
              <tbody>
                {reservations.map((reservation) => (
                  <tr
                    key={`${reservation.nic}-${reservation.scheduled}`}
                    className="border-b border-slate-800/90 transition-colors last:border-0 hover:bg-slate-800/45"
                  >
                    <td className="whitespace-nowrap px-5 py-3.5 text-white">
                      <span className="font-medium">{reservation.name}</span>{' '}
                      <span className="font-mono text-xs text-slate-400">{reservation.nic}</span>
                    </td>
                    <td className="whitespace-nowrap px-5 py-3.5 text-slate-200">{reservation.node}</td>
                    <td className="whitespace-nowrap px-5 py-3.5 font-mono text-xs text-slate-400">
                      {reservation.scheduled}
                    </td>
                    <td className="whitespace-nowrap px-5 py-3.5">
                      <span
                        className={`inline-flex rounded-full px-2.5 py-1 font-mono text-[10px] ${statusClasses[reservation.status]}`}
                      >
                        {reservation.status}
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
                  <p className="mt-1 truncate font-mono text-[11px] text-slate-500">{node.location}</p>
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