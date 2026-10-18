import { useEffect, useState } from 'react';
import { getPreferences, savePreferences } from '../api/preferences';
import { errorMessage } from '../api/client';

export default function Preferences() {
  const [sendBillMode, setSendBillMode] = useState('NEVER');
  const [billChannel, setBillChannel] = useState('SMS');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  // Runs once when the page opens: load the saved values.
  useEffect(() => {
    getPreferences()
      .then((p) => { setSendBillMode(p.sendBillMode); setBillChannel(p.billChannel); })
      .catch((e) => setError(errorMessage(e)))
      .finally(() => setLoading(false));
  }, []);

  async function handleSave(e) {
    e.preventDefault();
    setMessage(''); setError('');
    try {
      await savePreferences({ sendBillMode, billChannel });
      setMessage('Preferences saved');
    } catch (err) {
      setError(errorMessage(err));
    }
  }

  if (loading) return <p>Loading…</p>;

  return (
    <form onSubmit={handleSave} style={{ maxWidth: 480 }}>
      <h2>Preferences</h2>

      <h3>Send bill number to customers</h3>
      <label><input type="radio" checked={sendBillMode === 'ALWAYS'} onChange={() => setSendBillMode('ALWAYS')} /> Always</label><br />
      <label><input type="radio" checked={sendBillMode === 'NEVER'} onChange={() => setSendBillMode('NEVER')} /> Never</label>

      <h3>Send via</h3>
      <label><input type="radio" checked={billChannel === 'SMS'} onChange={() => setBillChannel('SMS')} /> SMS</label>{' '}
      <label><input type="radio" checked={billChannel === 'WHATSAPP'} onChange={() => setBillChannel('WHATSAPP')} /> WhatsApp</label>

      <p style={{ fontSize: 13, color: '#555' }}>
        Applies to every bill. You can skip it for a single bill on the Billing screen.
      </p>

      {error && <p style={{ color: 'crimson' }}>{error}</p>}
      {message && <p style={{ color: 'green' }}>{message}</p>}
      <button type="submit">Save preferences</button>
    </form>
  );
}
