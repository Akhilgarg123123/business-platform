import { useEffect, useState } from 'react';
import { getProducts } from '../api/products';
import { errorMessage } from '../api/client';
import { money } from '../utils/format';

export default function Stock() {
  const [products, setProducts] = useState([]);
  const [filter, setFilter] = useState('');
  const [threshold, setThreshold] = useState(5);
  const [sort, setSort] = useState('name');
  const [error, setError] = useState('');

  async function load() {
    try {
      setProducts(await getProducts());
    } catch (e) {
      setError(errorMessage(e));
    }
  }

  // Runs once when the page opens.
  useEffect(() => { load(); }, []);

  const low = Number(threshold) || 0;
  const stockOf = (p) => Number(p.currentStock) || 0;
  const status = (p) => (stockOf(p) <= 0 ? 'out' : stockOf(p) <= low ? 'low' : 'ok');

  const visible = products
    .filter((p) => p.name.toLowerCase().includes(filter.trim().toLowerCase()))
    .sort((a, b) => (sort === 'stock' ? stockOf(a) - stockOf(b) : a.name.localeCompare(b.name)));

  const outCount = products.filter((p) => status(p) === 'out').length;
  const lowCount = products.filter((p) => status(p) === 'low').length;
  const stockValue = products.reduce((sum, p) => sum + stockOf(p) * (Number(p.defaultPrice) || 0), 0);

  const badge = (p) => {
    const s = status(p);
    if (s === 'out') return <span style={{ color: '#b3261e', fontWeight: 700 }}>Out of stock</span>;
    if (s === 'low') return <span style={{ color: '#b26a00', fontWeight: 700 }}>Low</span>;
    return <span style={{ color: '#1f7a5c', fontWeight: 600 }}>OK</span>;
  };

  return (
    <div>
      <h2>Stock</h2>
      {error && <p className="error">{error}</p>}

      <div className="stats" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))' }}>
        <div className="stat"><div className="label">Products</div><div className="value">{products.length}</div></div>
        <div className="stat"><div className="label">Low stock</div><div className="value">{lowCount}</div></div>
        <div className="stat"><div className="label">Out of stock</div><div className="value">{outCount}</div></div>
        <div className="stat"><div className="label">Stock value (at selling price)</div><div className="value">{money(stockValue)}</div></div>
      </div>

      <div className="card">
        <div className="row">
          <input placeholder="Search products…" value={filter} onChange={(e) => setFilter(e.target.value)} />
          <label className="row">
            Low stock at or below
            <input type="number" min="0" style={{ width: 80 }} value={threshold}
                   onChange={(e) => setThreshold(e.target.value)} />
          </label>
          <select value={sort} onChange={(e) => setSort(e.target.value)}>
            <option value="name">Sort by name</option>
            <option value="stock">Sort by lowest stock</option>
          </select>
          <button className="secondary" onClick={load}>Refresh</button>
        </div>

        <div className="scroll-x" style={{ marginTop: 12 }}>
          <table>
            <thead>
              <tr><th>Product</th><th>In stock</th><th>Unit</th><th>Status</th></tr>
            </thead>
            <tbody>
              {visible.length === 0 && (
                <tr><td colSpan="4" className="muted">
                  {products.length === 0 ? 'No products yet. Add some in the Products tab.' : 'No products match your search.'}
                </td></tr>
              )}
              {visible.map((p) => (
                <tr key={p.id}>
                  <td>{p.name}</td>
                  <td><strong>{stockOf(p)}</strong></td>
                  <td>{p.unit || '-'}</td>
                  <td>{badge(p)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <p className="muted">Stock goes up when you record a delivery and down with every sale.</p>
      </div>
    </div>
  );
}
