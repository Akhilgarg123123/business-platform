import { useState } from 'react';
import { downloadExport } from '../api/exports';

const today = () => new Date().toLocaleDateString('en-CA'); // YYYY-MM-DD in local time

// kind: 'customers' | 'suppliers'
// options: [{ id, label }]  for the optional "only this one" dropdown
export default function ExportPanel({ kind, options }) {
  const [mode, setMode] = useState('today');
  const [from, setFrom] = useState(today());
  const [to, setTo] = useState(today());
  const [selectedId, setSelectedId] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const noun = kind === 'customers' ? 'customer' : 'supplier';

  async function handleExport() {
    setError('');
    const f = mode === 'today' ? today() : from;
    const t = mode === 'today' ? today() : to;
    if (!f || !t) { setError('Please choose both dates'); return; }
    if (f > t) { setError('"From" date must not be after "To" date'); return; }

    setBusy(true);
    try {
      await downloadExport(kind, { from: f, to: t, id: selectedId || undefined });
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section style={{ border: '1px solid #ddd', borderRadius: 8, padding: 16, marginTop: 16 }}>
      <h3 style={{ marginTop: 0 }}>Export to Excel</h3>

      <label>
        <input type="radio" checked={mode === 'today'} onChange={() => setMode('today')} /> Today
      </label>{' '}
      <label>
        <input type="radio" checked={mode === 'custom'} onChange={() => setMode('custom')} /> Custom range
      </label>

      {mode === 'custom' && (
        <div style={{ marginTop: 8 }}>
          <label>From <input type="date" value={from} onChange={(e) => setFrom(e.target.value)} /></label>{' '}
          <label>To <input type="date" value={to} onChange={(e) => setTo(e.target.value)} /></label>
        </div>
      )}

      <div style={{ marginTop: 8 }}>
        <select value={selectedId} onChange={(e) => setSelectedId(e.target.value)}>
          <option value="">All {noun}s</option>
          {options.map((o) => (
            <option key={o.id} value={o.id}>{o.label}</option>
          ))}
        </select>
      </div>

      {error && <p style={{ color: 'crimson' }}>{error}</p>}

      <button onClick={handleExport} disabled={busy} style={{ marginTop: 8 }}>
        {busy ? 'Preparing…' : 'Download Excel'}
      </button>
    </section>
  );
}
