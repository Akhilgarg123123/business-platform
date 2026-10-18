import api from './client';

export const getCustomers = () => api.get('/customers').then((r) => r.data);
export const searchCustomers = (phone) =>
  api.get('/customers/search', { params: { phone } }).then((r) => r.data);
export const createCustomer = (c) => api.post('/customers', c).then((r) => r.data);
export const updateCustomer = (id, c) => api.put(`/customers/${id}`, c).then((r) => r.data);
export const deleteCustomer = (id) => api.delete(`/customers/${id}`);
export const getCustomerInsights = (id) => api.get(`/customers/${id}/insights`).then((r) => r.data);
export const getCustomerTransactions = (id) =>
  api.get(`/customers/${id}/transactions`).then((r) => r.data);
