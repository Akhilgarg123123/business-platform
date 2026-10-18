import { useEffect, useState } from 'react';
import { getProducts, createProduct, updateProduct, deleteProduct } from '../api/products';
import { errorMessage } from '../api/client';
import { money } from '../utils/format';

const emptyForm = { name: '', unit: '', defaultPrice: '', barcode: '' };

export default function Products() {
  const [products, setProducts] = useState([]);
  const [filter, setFilter] = useState('');
  const [form, setForm] = useState(emptyForm);
  const [editing, setEditing] = useState(null); // the product being edited, or null when adding
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);

  async function load() {
    try {
      setProducts(await getProducts());
    } catch (e) {
      setError(errorMessage(e));
    }
  }

  // Runs once when the page opens.
  useEffect(() => { load(); }, []);

  const setField = (name) => (e) => setForm({ ...form, [name]: e.target.value });

  async function handleSave(e) {
    e.preventDefault();
    setError(''); setMessage('');
    setBusy(true);
    const payload = {
      name: form.name.trim(),
      unit: form.unit.trim() || null,
      defaultPrice: form.defaultPrice === '' ? null : Number(form.defaultPrice),
      barcode: form.barcode.trim() || null, // blank must be null: barcodes are unique
      currentStock: editing ? Number(editing.currentStock) || 0 : 0, // ignored by the server on edit
    };
    try {
      if (editing) {
        await updateProduct(editing.id, payload);
        setMessage('Product updated');
      } else {
        await createProduct(payload);
        setMessage('Product added. Record a delivery to bring it into stock.');
      }
      setForm(emptyForm);
      setEditing(null);
      load();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  }

  function startEdit(p) {
    setEditing(p);
    setForm({
      name: p.name || '',
      unit: p.unit || '',
      defaultPrice: p.defaultPrice != null ? String(p.defaultPrice) : '',
      barcode: p.barcode || '',
    });
    setMessage(''); setError('');
  }

  function cancelEdit() {
    setEditing(null);
    setForm(emptyForm);
  }

  async function handleDelete(p) {
    if (!window.confirm(`Delete “${p.name}”?`)) return;
    setError(''); setMessage('');
    try {
      await deleteProduct(p.id);
      if (editing?.id === p.id) cancelEdit();
      setMessage('Product deleted');
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  }

  const visible = products.filter((p) => p.name.toLowerCase().includes(filter.trim().toLowerCase()));

  return (
    <div>
      <h2>Products</h2>

      <div className="columns">
        <div className="main">
          <div className="card">
            <input
              placeholder="Search products…"
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
              style={{ width: '100%', maxWidth: 360 }}
            />

            <div className="scroll-x" style={{ marginTop: 12 }}>
              <table>
                <thead>
                  <tr><th>Product</th><th>Unit</th><th>Selling price</th><th>Barcode</th><th>Actions</th></tr>
                </thead>
                <tbody>
                  {visible.length === 0 && (
                    <tr><td colSpan="5" className="muted">
                      {products.length === 0 ? 'No products yet. Add your first one.' : 'No products match your search.'}
                    </td></tr>
                  )}
                  {visible.map((p) => (
                    <tr key={p.id}>
                      <td>{p.name}</td>
                      <td>{p.unit || '-'}</td>
                      <td>{p.defaultPrice != null ? money(p.defaultPrice) : <span className="muted">not set</span>}</td>
                      <td>{p.barcode || '-'}</td>
                      <td style={{ whiteSpace: 'nowrap' }}>
                        <button className="link" onClick={() => startEdit(p)}>Edit</button>
                        <button className="danger" onClick={() => handleDelete(p)}>Delete</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <p className="muted">{products.length} product{products.length === 1 ? '' : 's'} in total</p>
          </div>
        </div>

        <div className="side">
          <form className="card stack" onSubmit={handleSave}>
            <h3>{editing ? `Edit: ${editing.name}` : 'Add a new product'}</h3>
            <p className="muted">
              Put the size in the name, e.g. “Apple Juice 1L” and “Apple Juice 500 ml” are separate products.
              {editing && ' Stock is not changed here. Use Deliveries to add stock.'}
            </p>
            <input placeholder="Product name" value={form.name} onChange={setField('name')} required />
            <input placeholder="Unit (kg, pcs, bottles)" value={form.unit} onChange={setField('unit')} />
            <input type="number" min="0" step="0.01" placeholder="Selling price"
                   value={form.defaultPrice} onChange={setField('defaultPrice')} />
            <input placeholder="Barcode (optional)" value={form.barcode} onChange={setField('barcode')} />
            {error && <p className="error">{error}</p>}
            {message && <p className="success">{message}</p>}
            <div className="row">
              <button type="submit" disabled={busy}>
                {busy ? 'Saving…' : editing ? 'Save changes' : 'Add product'}
              </button>
              {editing && <button type="button" className="secondary" onClick={cancelEdit}>Cancel</button>}
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
