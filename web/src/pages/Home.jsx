import React from 'react';
import { Link } from 'react-router-dom';

const features = [
  {
    title: 'Manage microgrid nodes',
    description: 'Keep node locations, capacity, and battery slots organised in one workspace.',
  },
  {
    title: 'Review reservations',
    description: 'Track pending requests and upcoming energy transfers.',
  },
  {
    title: 'Verify transfers',
    description: 'Use reservation QR codes to confirm transfers at the grid node.',
  },
];

export default function Home() {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100">
      <header className="mx-auto flex max-w-6xl items-center justify-between px-6 py-6">
        <Link to="/" className="flex items-center gap-3" aria-label="Solara Grid home">
          <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-500 text-xl font-bold text-slate-950">
            ⚡
          </span>
          <span className="text-lg font-bold tracking-wide">Solara Grid</span>
        </Link>

        <Link
          to="/login"
          className="rounded-lg border border-slate-700 px-4 py-2 text-sm font-medium hover:border-amber-500 hover:text-amber-400"
        >
          Sign in
        </Link>
      </header>

      <main>
        <section className="mx-auto grid max-w-6xl gap-12 px-6 py-20 md:grid-cols-2 md:items-center md:py-28">
          <div>
            <p className="mb-5 text-sm font-semibold uppercase tracking-[0.2em] text-amber-400">
              Smart solar microgrid management
            </p>

            <h1 className="max-w-xl text-4xl font-bold leading-tight text-white sm:text-5xl">
              Local energy, managed with clarity.
            </h1>

            <p className="mt-6 max-w-lg text-lg leading-relaxed text-slate-400">
              Solara Grid brings microgrid nodes, energy reservations, and
              transfer verification into one connected system.
            </p>

            <div className="mt-9 flex flex-wrap gap-4">
              <Link
                to="/login"
                className="rounded-xl bg-amber-500 px-6 py-3 font-semibold text-slate-950 hover:bg-amber-400"
              >
                Open workspace
              </Link>
              <a
                href="#features"
                className="rounded-xl border border-slate-700 px-6 py-3 font-semibold text-slate-200 hover:border-slate-500"
              >
                Explore features
              </a>
            </div>
          </div>

          <div className="rounded-3xl border border-slate-800 bg-slate-900 p-7 shadow-2xl">
            <div className="mb-6 flex items-center justify-between">
              <span className="text-sm font-medium text-slate-400">How it works</span>
              <span className="rounded-full bg-teal-400/10 px-3 py-1 text-xs text-teal-300">
                Connected workflow
              </span>
            </div>

            {[
              ['01', 'Find a microgrid node'],
              ['02', 'Request an energy slot'],
              ['03', 'Review and approve the reservation'],
              ['04', 'Verify the transfer with a QR code'],
            ].map(([number, label]) => (
              <div
                key={number}
                className="flex items-center gap-4 border-t border-slate-800 py-5"
              >
                <span className="font-mono text-sm text-amber-400">{number}</span>
                <span className="text-sm text-slate-200">{label}</span>
              </div>
            ))}
          </div>
        </section>

        <section id="features" className="border-t border-slate-800 bg-slate-900/50">
          <div className="mx-auto max-w-6xl px-6 py-20">
            <h2 className="text-2xl font-bold text-white">One connected workflow</h2>
            <p className="mt-3 text-slate-400">
              Tools for the people who operate and use the microgrid.
            </p>

            <div className="mt-9 grid gap-5 md:grid-cols-3">
              {features.map((feature) => (
                <article
                  key={feature.title}
                  className="rounded-2xl border border-slate-800 bg-slate-900 p-6"
                >
                  <h3 className="font-semibold text-amber-400">{feature.title}</h3>
                  <p className="mt-3 text-sm leading-relaxed text-slate-400">
                    {feature.description}
                  </p>
                </article>
              ))}
            </div>
          </div>
        </section>
      </main>

      <footer className="border-t border-slate-800 px-6 py-7 text-center text-sm text-slate-500">
        Solara Grid · Smart Solar Microgrid Trading System
      </footer>
    </div>
  );
}