import { useEffect, useState } from 'react';
import { searchCustomers, createCustomer } from '../api/customers';
import { getProducts } from '../api/products';
import { createTransaction } from '../api/transactions';
import { getPreferences } from '../api/preferences';
import { errorMessage } from '../api/client';
import { money } from '../utils/format';

const today = () => new Date().toLocaleDateString('en-CA'); // YYYY-MM-DD in local time
const newRow = () => ({ key: crypto.randomUUID(), text: '', product: null, quantity: '1' });

export default function Billing() {
  const [phone, setPhone] = useState('');
  const [customer, setCustomer] = useState(null);
  const [notFound, setNotFound] = useState(false);
  const [newName, setNewName] = useState('');

  const [products, setProducts] = useState([]);
  const [rows, setRows] = useState([newRow()]);
  const [prefs, setPrefs] = useState(null);
  const [skipSend, setSkipSend] = useState(false);

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  // Load product list + preferences once when the page opens.
  useEffect(() => {
    Promise.all([getProducts(), getPreferences()])
        .then(([p, pr]) => { setProducts(p); setPrefs(pr); })
        .catch((e) => setError(errorMessage(e)));
  }, []);

  // ---- customer lookup ----
  async function findCustomer() {
    setError(''); setMessage(''); setCustomer(null); setNotFound(false);
    if (!phone.trim()) return;
    try {
      const res = await searchCustomers(phone.trim());
      const found = Array.isArray(res) ? res[0] : res;
      if (found) setCustomer(found); else setNotFound(true);
    } catch (e) {
      if (e.response?.status === 404) setNotFound(true); else setError(errorMessage(e));
    }
  }

  async function quickAdd() {
    setError('');
    try {
      const created = await createCustomer({ name: newName.trim(), phoneNumber: phone.trim(), email: null });
      setCustomer(created); setNotFound(false); setNewName('');
    } catch (e) {
      setError(errorMessage(e));
    }
  }

  // ---- item rows ----
  function updateRow(key, changes) {
    setRows((current) => current.map((r) => (r.key === key ? { ...r, ...changes } : r)));
  }

  function onProductText(key, text) {
    const match = products.find((p) => p.name.toLowerCase() === text.trim().toLowerCase());
    updateRow(key, { text, product: match || null });
  }

  const removeRow = (key) => setRows((current) => (current.length > 1 ? current.filter((r) => r.key !== key) : current));
  const addRow = () => setRows((current) => [...current, newRow()]);

  // The price is read from the product. It cannot be edited while billing.
  const priceOf = (r) => (r.product ? Number(r.product.defaultPrice) || 0 : 0);
  const lineTotal = (r) => (Number(r.quantity) || 0) * priceOf(r);
  const total = rows.reduce((sum, r) => sum + lineTotal(r), 0);

  // ---- submit ----
  async function completeSale() {
    setError(''); setMessage('');
    if (!customer) { setError('Find or add the customer first'); return; }
    const valid = rows.filter((r) => r.product);
    if (valid.length === 0) { setError('Add at least one product'); return; }
    if (valid.some((r) => !Number.isInteger(Number(r.quantity)) || Number(r.quantity) < 1)) {
      setError('Quantities must be whole numbers, 1 or more');
      return;
    }
    const unpriced = valid.find((r) => !(priceOf(r) > 0));
    if (unpriced) {
      setError(`“${unpriced.product.name}” has no selling price. Set it in the Products tab first.`);
      return;
    }

    setBusy(true);
    try {
      const res = await createTransaction({
        customerId: customer.id,
        date: today(),
        items: valid.map((r) => ({
          productId: r.product.id,
          quantity: Number(r.quantity),
          // no price is sent: the server always uses the product's saved price
        })),
        sendBill: skipSend ? false : null, // null = follow the saved preference
      });
      setMessage(
          `Sale saved. Bill #${res.billNumber}` + (res.billSendRequested ? ' (bill number sent to customer)' : '')
      );
      setRows([newRow()]);
      setSkipSend(false);
      setProducts(await getProducts()); // stock changed, refresh
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  }

  const willSend = prefs?.sendBillMode === 'ALWAYS';

  return (
      <div>
        <h2>Regular Billing</h2>

        <div className="columns">
          <div className="main">
            <div className="card stack">
              {/* Customer */}
              <div className="row">
                <input placeholder="Customer phone" value={phone} onChange={(e) => setPhone(e.target.value)} />
                <button onClick={findCustomer}>Find</button>
              </div>
              {customer && <p>Billing to: <strong>{customer.name}</strong> · {customer.phoneNumber}</p>}
              {notFound && (
                  <div className="row">
                    <span className="muted">No customer with this number.</span>
                    <input placeholder="New customer name" value={newName} onChange={(e) => setNewName(e.target.value)} />
                    <button onClick={quickAdd} disabled={!newName.trim()}>Add &amp; use</button>
                  </div>
              )}

              {/* Items */}
              <datalist id="product-suggestions">
                {products.map((p) => <option key={p.id} value={p.name} />)}
              </datalist>

              <div className="scroll-x">
                <table>
                  <thead>
                  <tr><th>Product</th><th>Qty</th><th>Price</th><th>Line total</th><th></th></tr>
                  </thead>
                  <tbody>
                  {rows.map((r) => (
                      <tr key={r.key}>
                        <td>
                          <input
                              list="product-suggestions"
                              placeholder="Type product name…"
                              value={r.text}
                              onChange={(e) => onProductText(r.key, e.target.value)}
                          />
                          {r.text && !r.product && <small className="error"> pick from the list</small>}
                          {r.product && <small className="muted"> {r.product.currentStock} in stock</small>}
                        </td>
                        <td><input type="number" min="1" step="1" style={{ width: 80 }} value={r.quantity}
                                   onChange={(e) => updateRow(r.key, { quantity: e.target.value })} /></td>
                        <td>
                          {r.product
                              ? (priceOf(r) > 0 ? money(priceOf(r)) : <span className="error">not set</span>)
                              : '-'}
                        </td>
                        <td>{money(lineTotal(r))}</td>
                        <td><button className="danger" aria-label="Remove row" onClick={() => removeRow(r.key)}>×</button></td>
                      </tr>
                  ))}
                  </tbody>
                </table>
              </div>
              <div>
                <button className="secondary" onClick={addRow}>+ Add item</button>
              </div>
            </div>
          </div>

          <div className="side">
            <div className="card stack">
              <h3>Bill total</h3>
              <div style={{ fontSize: 34, fontWeight: 700 }}>{money(total)}</div>

              {prefs && (
                  <div>
                    {willSend
                        ? <p className="muted">Bill number will be sent via {prefs.billChannel === 'SMS' ? 'SMS' : 'WhatsApp'}.</p>
                        : <p className="muted">Bill number will not be sent (change in Preferences).</p>}
                    {willSend && (
                        <label className="row">
                          <input type="checkbox" checked={skipSend} onChange={(e) => setSkipSend(e.target.checked)} />
                          Don't send for this bill
                        </label>
                    )}
                  </div>
              )}

              {error && <p className="error">{error}</p>}
              {message && <p className="success">{message}</p>}
              <button onClick={completeSale} disabled={busy}>{busy ? 'Saving…' : 'Complete sale'}</button>
            </div>
          </div>
        </div>
      </div>
  );
}