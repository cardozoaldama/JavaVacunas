import apiClient from '@/lib/api-client';
import type { Child } from '@/types';

export const childrenApi = {
  getAll: async (): Promise<Child[]> => {
    const response = await apiClient.get<Child[]>('/children');
    return response.data;
  },

  getById: async (id: number): Promise<Child> => {
    const response = await apiClient.get<Child>(`/children/${id}`);
    return response.data;
  },

  search: async (query: string): Promise<Child[]> => {
    const response = await apiClient.get<Child[]>('/children/search', {
      params: { query },
    });
    return response.data;
  },

  getMyChildren: async (): Promise<Child[]> => {
    const response = await apiClient.get<Child[]>('/children/my-children');
    return response.data;
  },

  create: async (data: Omit<Child, 'id' | 'ageInMonths' | 'createdAt' | 'updatedAt'>): Promise<Child> => {
    const response = await apiClient.post<Child>('/children', data);
    return response.data;
  },

  update: async (id: number, data: Partial<Child>): Promise<Child> => {
    const response = await apiClient.put<Child>(`/children/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/children/${id}`);
  },
};
