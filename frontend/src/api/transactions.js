import api from './client';

export const createTransaction = (payload) => api.post('/transactions', payload).then((r) => r.data);
