import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getSuppliers, recordDelivery } from '../api/suppliers';
import { getProducts } from '../api/products';
import { errorMessage } from '../api/client';
import { money } from '../utils/format';

const today = () => new Date().toLocaleDateString('en-CA'); // YYYY-MM-DD in local time
const newRow = () => ({ key: crypto.randomUUID(), text: '', product: null, quantity: '1', price: '' });

export default function Deliveries() {
  const [suppliers, setSuppliers] = useState([]);
  const [products, setProducts] = useState([]);

  const [supplierId, setSupplierId] = useState('');
  const [date, setDate] = useState(today());
  const [description, setDescription] = useState('');
  const [rows, setRows] = useState([newRow()]);

  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    Promise.all([getSuppliers(), getProducts()])
      .then(([s, p]) => { setSuppliers(s); setProducts(p); })
      .catch((e) => setError(errorMessage(e)));
  }, []);

  function updateRow(key, changes) {
    setRows((current) => current.map((r) => (r.key === key ? { ...r, ...changes } : r)));
  }

  function onProductText(key, text) {
    const match = products.find((p) => p.name.toLowerCase() === text.trim().toLowerCase());
    updateRow(key, { text, product: match || null });
  }

  const removeRow = (key) => setRows((current) => (current.length > 1 ? current.filter((r) => r.key !== key) : current));
  const addRow = () => setRows((current) => [...current, newRow()]);

  const lineTotal = (r) => (Number(r.quantity) || 0) * (Number(r.price) || 0);
  const total = rows.reduce((sum, r) => sum + lineTotal(r), 0);

  async function handleSave() {
    setError(''); setMessage('');
    if (!supplierId) { setError('Choose a supplier'); return; }
    const valid = rows.filter((r) => r.product);
    if (valid.length === 0) { setError('Add at least one product'); return; }
    if (valid.some((r) => !Number.isInteger(Number(r.quantity)) || Number(r.quantity) < 1)) {
      setError('Quantities must be whole numbers, 1 or more'); return;
    }
    if (valid.some((r) => !(Number(r.price) > 0))) {
      setError('Enter the cost price for every item'); return;
    }

    setBusy(true);
    try {
      await recordDelivery(supplierId, {
        date,
        description: description.trim() || null,
        items: valid.map((r) => ({
          productId: r.product.id,
          quantity: Number(r.quantity),
          price: Number(r.price),
        })),
      });
      setMessage('Delivery recorded. Stock updated.');
      setRows([newRow()]);
      setDescription('');
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div style={{ maxWidth: 900 }}>
      <h2>Deliveries</h2>

      <div className="card stack">
        <h3>Record a delivery</h3>

        {products.length === 0 && (
          <p className="muted">
            You have no products yet. Add them in the <Link to="/products">Products</Link> tab first.
          </p>
        )}
        {suppliers.length === 0 && (
          <p className="muted">
            You have no suppliers yet. Add one in the <Link to="/suppliers">Suppliers</Link> tab first.
          </p>
        )}

        <div className="row">
          <select value={supplierId} onChange={(e) => setSupplierId(e.target.value)} style={{ minWidth: 240 }}>
            <option value="">Select supplier</option>
            {suppliers.map((s) => (
              <option key={s.id} value={s.id}>{s.companyName} – {s.dealerName}</option>
            ))}
          </select>
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
          <input
            placeholder="Note (optional), e.g. Weekly restock"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            style={{ flex: 1, minWidth: 200 }}
          />
        </div>

        <datalist id="delivery-products">
          {products.map((p) => <option key={p.id} value={p.name} />)}
        </datalist>

        <div className="scroll-x">
          <table>
            <thead>
              <tr><th>Product</th><th>Qty</th><th>Cost price</th><th>Line total</th><th></th></tr>
            </thead>
            <tbody>
              {rows.map((r) => (
                <tr key={r.key}>
                  <td>
                    <input
                      list="delivery-products"
                      placeholder="Type product name…"
                      value={r.text}
                      onChange={(e) => onProductText(r.key, e.target.value)}
                    />
                    {r.text && !r.product && <small className="error"> pick from the list</small>}
                  </td>
                  <td><input type="number" min="1" step="1" style={{ width: 80 }} value={r.quantity}
                             onChange={(e) => updateRow(r.key, { quantity: e.target.value })} /></td>
                  <td><input type="number" min="0" style={{ width: 100 }} value={r.price}
                             onChange={(e) => updateRow(r.key, { price: e.target.value })} /></td>
                  <td>{money(lineTotal(r))}</td>
                  <td><button className="danger" aria-label="Remove row" onClick={() => removeRow(r.key)}>×</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="row" style={{ justifyContent: 'space-between' }}>
          <button className="secondary" onClick={addRow}>+ Add item row</button>
          <strong style={{ fontSize: 18 }}>Total: {money(total)}</strong>
        </div>

        {error && <p className="error">{error}</p>}
        {message && <p className="success">{message} See it in the <Link to="/stock">Stock</Link> tab.</p>}
        <button onClick={handleSave} disabled={busy}>
          {busy ? 'Saving…' : 'Save delivery & update stock'}
        </button>
      </div>
    </div>
  );
}
