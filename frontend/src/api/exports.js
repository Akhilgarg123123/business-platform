import api, { errorMessage } from './client';

// Errors come back as a Blob when responseType is 'blob', so read the JSON out of it.
async function readError(err) {
  const data = err.response?.data;
  if (data instanceof Blob) {
    try {
      const json = JSON.parse(await data.text());
      return json.message || (json.errors && Object.values(json.errors).join(', ')) || 'Export failed';
    } catch {
      return 'Export failed';
    }
  }
  return errorMessage(err);
}

// kind: 'customers' | 'suppliers'
export async function downloadExport(kind, { from, to, id }) {
  const params = { from, to };
  if (id) params[kind === 'customers' ? 'customerId' : 'supplierId'] = id;

  try {
    const res = await api.get(`/export/${kind}`, { params, responseType: 'blob' });
    const disposition = res.headers['content-disposition'] || '';
    const match = /filename="?([^";]+)"?/.exec(disposition);
    const filename = match ? match[1] : `${kind}_${from}_to_${to}.xlsx`;

    // Turn the bytes into a temporary link and click it to trigger the browser download.
    const url = URL.createObjectURL(res.data);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  } catch (err) {
    throw new Error(await readError(err));
  }
}
