import { useEffect, useState } from 'react';
import {
  getCustomers, searchCustomers, createCustomer, updateCustomer, deleteCustomer,
  getCustomerInsights, getCustomerTransactions,
} from '../api/customers';
import { errorMessage } from '../api/client';
import { money } from '../utils/format';

const emptyForm = { name: '', phoneNumber: '', email: '' };

export default function Customers() {
  const [customers, setCustomers] = useState([]);
  const [query, setQuery] = useState('');
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null); // null = adding, number = editing
  const [selected, setSelected] = useState(null);
  const [insights, setInsights] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  async function load() {
    try {
      setCustomers(await getCustomers());
    } catch (e) {
      setError(errorMessage(e));
    }
  }

  // Runs once when the page opens.
  useEffect(() => { load(); }, []);

  const setField = (name) => (e) => setForm({ ...form, [name]: e.target.value });

  async function handleSearch(e) {
    e.preventDefault();
    setError(''); setMessage('');
    if (!query.trim()) { load(); return; }
    try {
      const res = await searchCustomers(query.trim());
      setCustomers(Array.isArray(res) ? res : res ? [res] : []);
    } catch (err) {
      if (err.response?.status === 404) setCustomers([]); else setError(errorMessage(err));
    }
  }

  async function handleSave(e) {
    e.preventDefault();
    setError(''); setMessage('');
    const payload = {
      name: form.name.trim(),
      phoneNumber: form.phoneNumber.trim(),
      email: form.email.trim() || null,
    };
    try {
      if (editingId) await updateCustomer(editingId, payload);
      else await createCustomer(payload);
      setMessage(editingId ? 'Customer updated' : 'Customer added');
      setForm(emptyForm);
      setEditingId(null);
      setQuery('');
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  }

  function startEdit(c) {
    setEditingId(c.id);
    setForm({ name: c.name || '', phoneNumber: c.phoneNumber || '', email: c.email || '' });
    setMessage(''); setError('');
  }

  function cancelEdit() {
    setEditingId(null);
    setForm(emptyForm);
  }

  async function handleDelete(c) {
    if (!window.confirm(`Delete ${c.name}? Their transactions will be removed too.`)) return;
    setError(''); setMessage('');
    try {
      await deleteCustomer(c.id);
      if (selected?.id === c.id) { setSelected(null); setInsights(null); setTransactions([]); }
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  }

  async function showInsights(c) {
    setError(''); setSelected(c);
    try {
      const [ins, txs] = await Promise.all([getCustomerInsights(c.id), getCustomerTransactions(c.id)]);
      setInsights(ins);
      setTransactions(txs);
    } catch (err) {
      setError(errorMessage(err));
    }
  }

  // The backend field names can differ slightly, so read them defensively.
  const avgOrder = insights && (insights.averageOrderValue ?? insights.avgOrderValue ?? 0);
  const trend = (insights && (insights.monthlySpendTrend ?? insights.monthlyTrend ?? insights.monthlySpend)) || [];
  const isRegular = (c) => c.isRegular ?? c.regular ?? false;

  return (
    <div>
      <h2>Customers</h2>
      {error && <p className="error">{error}</p>}
      {message && <p className="success">{message}</p>}

      <div className="columns">
        <div className="main">
          <div className="card">
            <form onSubmit={handleSearch} className="row">
              <input
                placeholder="Search by phone number"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
              />
              <button type="submit">Search</button>
              <button type="button" className="secondary" onClick={() => { setQuery(''); load(); }}>
                Show all
              </button>
            </form>

            <div className="scroll-x" style={{ marginTop: 12 }}>
              <table>
                <thead>
                  <tr><th>Name</th><th>Phone</th><th>Status</th><th>Actions</th></tr>
                </thead>
                <tbody>
                  {customers.length === 0 && (
                    <tr><td colSpan="4" className="muted">No customers yet.</td></tr>
                  )}
                  {customers.map((c) => (
                    <tr key={c.id}>
                      <td>{c.name}</td>
                      <td>{c.phoneNumber}</td>
                      <td>{isRegular(c) ? 'Regular' : 'One-time'}</td>
                      <td style={{ whiteSpace: 'nowrap' }}>
                        <button className="link" onClick={() => startEdit(c)}>Edit</button>
                        <button className="link" onClick={() => showInsights(c)}>Insights</button>
                        <button className="danger" onClick={() => handleDelete(c)}>Delete</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div className="side">
          <form className="card stack" onSubmit={handleSave}>
            <h3>{editingId ? 'Edit customer' : 'Add customer'}</h3>
            <input placeholder="Name" value={form.name} onChange={setField('name')} required />
            <input placeholder="Phone number" value={form.phoneNumber} onChange={setField('phoneNumber')} required />
            <input placeholder="Email (optional)" value={form.email} onChange={setField('email')} />
            <div className="row">
              <button type="submit">{editingId ? 'Save changes' : 'Add customer'}</button>
              {editingId && <button type="button" className="secondary" onClick={cancelEdit}>Cancel</button>}
            </div>
          </form>

          <div className="card">
            <h3>Customer insights{selected ? `: ${selected.name}` : ''}</h3>
            {!insights && <p className="muted">Click “Insights” on a customer to see their numbers.</p>}
            {insights && (
              <>
                <div className="stats">
                  <div className="stat"><div className="label">Total spend</div><div className="value">{money(insights.totalSpend)}</div></div>
                  <div className="stat"><div className="label">Transactions</div><div className="value">{insights.transactionCount ?? 0}</div></div>
                  <div className="stat"><div className="label">Average order</div><div className="value">{money(avgOrder)}</div></div>
                  <div className="stat"><div className="label">Days since last purchase</div><div className="value">{insights.daysSinceLastPurchase ?? '-'}</div></div>
                </div>
                {insights.status && <p className="muted">Status: {insights.status}</p>}

                {trend.length > 0 && (
                  <>
                    <h3>Monthly spend</h3>
                    {trend.map((m) => (
                      <div key={m.month} className="row" style={{ justifyContent: 'space-between' }}>
                        <span>{m.month}</span>
                        <strong>{money(m.amount ?? m.spend ?? m.total)}</strong>
                      </div>
                    ))}
                  </>
                )}

                <h3 style={{ marginTop: 16 }}>Purchase history</h3>
                {transactions.length === 0 && <p className="muted">No purchases yet.</p>}
                {transactions.map((t) => (
                  <div key={t.id} style={{ borderTop: '1px solid #ebe7dc', padding: '8px 0' }}>
                    <strong>{t.date}</strong>
                    {t.billNumber ? ` · Bill #${t.billNumber}` : ''} · {money(t.amount)}
                    <div className="muted">
                      {t.items?.map((i) => `${i.productName || 'Item'} ×${i.quantity}`).join(', ')}
                    </div>
                  </div>
                ))}
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
