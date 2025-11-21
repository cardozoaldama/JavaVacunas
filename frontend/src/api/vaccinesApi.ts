import apiClient from '@/lib/api-client';
import type { Vaccine, VaccinationSchedule } from '@/types';

export const vaccinesApi = {
  getAll: async (): Promise<Vaccine[]> => {
    const response = await apiClient.get<Vaccine[]>('/vaccines');
    return response.data;
  },

  getById: async (id: number): Promise<Vaccine> => {
    const response = await apiClient.get<Vaccine>(`/vaccines/${id}`);
    return response.data;
  },

  searchByDisease: async (disease: string): Promise<Vaccine[]> => {
    const response = await apiClient.get<Vaccine[]>('/vaccines/search', {
      params: { disease },
    });
    return response.data;
  },
};

export const schedulesApi = {
  getParaguaySchedule: async (): Promise<VaccinationSchedule[]> => {
    const response = await apiClient.get<VaccinationSchedule[]>('/schedules/paraguay');
    return response.data;
  },

  getByVaccineId: async (vaccineId: number): Promise<VaccinationSchedule[]> => {
    const response = await apiClient.get<VaccinationSchedule[]>(`/schedules/vaccine/${vaccineId}`);
    return response.data;
  },

  getMandatoryUpToAge: async (ageInMonths: number): Promise<VaccinationSchedule[]> => {
    const response = await apiClient.get<VaccinationSchedule[]>('/schedules/mandatory', {
      params: { ageInMonths },
    });
    return response.data;
  },
};
