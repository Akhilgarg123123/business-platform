import { useEffect, useState } from 'react';
import {
  getSuppliers, searchSuppliers, createSupplier, updateSupplier, deleteSupplier,
  getSupplierInsights, getSupplierDeliveries,
} from '../api/suppliers';
import { errorMessage } from '../api/client';
import { money } from '../utils/format';

const emptyForm = { companyName: '', dealerName: '', phoneNumber: '', email: '', itemsSupplied: '' };

export default function Suppliers() {
  const [suppliers, setSuppliers] = useState([]);
  const [query, setQuery] = useState('');
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null); // null = adding, number = editing
  const [selected, setSelected] = useState(null);
  const [insights, setInsights] = useState(null);
  const [deliveries, setDeliveries] = useState([]);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  async function load() {
    try {
      setSuppliers(await getSuppliers());
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
      const res = await searchSuppliers(query.trim());
      setSuppliers(Array.isArray(res) ? res : res ? [res] : []);
    } catch (err) {
      if (err.response?.status === 404) setSuppliers([]); else setError(errorMessage(err));
    }
  }

  async function handleSave(e) {
    e.preventDefault();
    setError(''); setMessage('');
    const payload = {
      companyName: form.companyName.trim(),
      dealerName: form.dealerName.trim(),
      phoneNumber: form.phoneNumber.trim(),
      email: form.email.trim() || null,
      itemsSupplied: form.itemsSupplied.trim() || null,
    };
    try {
      if (editingId) await updateSupplier(editingId, payload);
      else await createSupplier(payload);
      setMessage(editingId ? 'Supplier updated' : 'Supplier added');
      setForm(emptyForm);
      setEditingId(null);
      setQuery('');
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  }

  function startEdit(s) {
    setEditingId(s.id);
    setForm({
      companyName: s.companyName || '',
      dealerName: s.dealerName || '',
      phoneNumber: s.phoneNumber || '',
      email: s.email || '',
      itemsSupplied: s.itemsSupplied || '',
    });
    setMessage(''); setError('');
  }

  function cancelEdit() {
    setEditingId(null);
    setForm(emptyForm);
  }

  async function handleDelete(s) {
    if (!window.confirm(`Delete ${s.companyName} (${s.dealerName})?`)) return;
    setError(''); setMessage('');
    try {
      await deleteSupplier(s.id);
      if (selected?.id === s.id) { setSelected(null); setInsights(null); setDeliveries([]); }
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  }

  async function showInsights(s) {
    setError(''); setSelected(s);
    try {
      const [ins, dels] = await Promise.all([getSupplierInsights(s.id), getSupplierDeliveries(s.id)]);
      setInsights(ins);
      setDeliveries(dels);
    } catch (err) {
      setError(errorMessage(err));
    }
  }

  return (
    <div>
      <h2>Suppliers</h2>
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
                  <tr><th>Company</th><th>Dealer</th><th>Phone</th><th>Actions</th></tr>
                </thead>
                <tbody>
                  {suppliers.length === 0 && (
                    <tr><td colSpan="4" className="muted">No suppliers yet.</td></tr>
                  )}
                  {suppliers.map((s) => (
                    <tr key={s.id}>
                      <td>{s.companyName}</td>
                      <td>{s.dealerName}</td>
                      <td>{s.phoneNumber}</td>
                      <td style={{ whiteSpace: 'nowrap' }}>
                        <button className="link" onClick={() => startEdit(s)}>Edit</button>
                        <button className="link" onClick={() => showInsights(s)}>Insights</button>
                        <button className="danger" onClick={() => handleDelete(s)}>Delete</button>
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
            <h3>{editingId ? 'Edit supplier' : 'Add supplier'}</h3>
            <input placeholder="Company name" value={form.companyName} onChange={setField('companyName')} required />
            <input placeholder="Dealer name" value={form.dealerName} onChange={setField('dealerName')} required />
            <input placeholder="Phone number" value={form.phoneNumber} onChange={setField('phoneNumber')} required />
            <input placeholder="Email (optional)" value={form.email} onChange={setField('email')} />
            <input placeholder="Items supplied (optional)" value={form.itemsSupplied} onChange={setField('itemsSupplied')} />
            <div className="row">
              <button type="submit">{editingId ? 'Save changes' : 'Add supplier'}</button>
              {editingId && <button type="button" className="secondary" onClick={cancelEdit}>Cancel</button>}
            </div>
          </form>

          <div className="card">
            <h3>Supplier insights{selected ? `: ${selected.companyName}` : ''}</h3>
            {!insights && <p className="muted">Click “Insights” on a supplier to see their numbers.</p>}
            {insights && (
              <>
                <div className="stats">
                  <div className="stat"><div className="label">Total supplied</div><div className="value">{money(insights.totalSupplied)}</div></div>
                  <div className="stat"><div className="label">Deliveries</div><div className="value">{insights.deliveryCount}</div></div>
                  <div className="stat"><div className="label">Average delivery</div><div className="value">{money(insights.averageDelivery)}</div></div>
                  <div className="stat"><div className="label">Last delivery</div><div className="value">{insights.lastDeliveryDate || '-'}</div></div>
                </div>

                <h3>Delivery history</h3>
                {deliveries.length === 0 && <p className="muted">No deliveries recorded.</p>}
                {deliveries.map((d) => (
                  <div key={d.id} style={{ borderTop: '1px solid #ebe7dc', padding: '8px 0' }}>
                    <strong>{d.date}</strong> · {money(d.amount)}
                    <div className="muted">
                      {d.items?.map((i) => `${i.productName || 'Item'} ×${i.quantity}`).join(', ')}
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
