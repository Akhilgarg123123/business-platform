import api from './client';

export const getPreferences = () => api.get('/preferences').then((r) => r.data);
export const savePreferences = (prefs) => api.put('/preferences', prefs).then((r) => r.data);
