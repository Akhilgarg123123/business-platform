import api from './client';

export const getSuppliers = () => api.get('/suppliers').then((r) => r.data);
export const searchSuppliers = (phone) =>
  api.get('/suppliers/search', { params: { phone } }).then((r) => r.data);
export const createSupplier = (s) => api.post('/suppliers', s).then((r) => r.data);
export const updateSupplier = (id, s) => api.put(`/suppliers/${id}`, s).then((r) => r.data);
export const deleteSupplier = (id) => api.delete(`/suppliers/${id}`);
export const getSupplierInsights = (id) => api.get(`/suppliers/${id}/insights`).then((r) => r.data);
export const getSupplierDeliveries = (id) => api.get(`/suppliers/${id}/deliveries`).then((r) => r.data);
export const recordDelivery = (id, payload) =>
  api.post(`/suppliers/${id}/deliveries`, payload).then((r) => r.data);
