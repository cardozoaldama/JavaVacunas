import apiClient from '@/lib/api-client';
import type { Appointment } from '@/types';

export interface CreateAppointmentRequest {
  childId: number;
  appointmentDate: string;
  appointmentType: string;
  scheduledVaccines?: string;
  assignedToId?: number;
  notes?: string;
}

export const appointmentsApi = {
  getAll: async (): Promise<Appointment[]> => {
    const response = await apiClient.get<Appointment[]>('/appointments');
    return response.data;
  },

  getById: async (id: number): Promise<Appointment> => {
    const response = await apiClient.get<Appointment>(`/appointments/${id}`);
    return response.data;
  },

  getByChildId: async (childId: number): Promise<Appointment[]> => {
    const response = await apiClient.get<Appointment[]>(`/appointments/child/${childId}`);
    return response.data;
  },

  getUpcoming: async (): Promise<Appointment[]> => {
    const response = await apiClient.get<Appointment[]>('/appointments/upcoming');
    return response.data;
  },

  getByStatus: async (status: Appointment['status']): Promise<Appointment[]> => {
    const response = await apiClient.get<Appointment[]>(`/appointments/status/${status}`);
    return response.data;
  },

  create: async (data: CreateAppointmentRequest): Promise<Appointment> => {
    const params = new URLSearchParams({
      childId: data.childId.toString(),
      appointmentDate: data.appointmentDate,
      appointmentType: data.appointmentType,
    });

    if (data.scheduledVaccines) {
      params.append('scheduledVaccines', data.scheduledVaccines);
    }
    if (data.assignedToId) {
      params.append('assignedToId', data.assignedToId.toString());
    }
    if (data.notes) {
      params.append('notes', data.notes);
    }

    const response = await apiClient.post<Appointment>(`/appointments?${params.toString()}`);
    return response.data;
  },

  updateStatus: async (id: number, status: Appointment['status']): Promise<Appointment> => {
    const response = await apiClient.put<Appointment>(
      `/appointments/${id}/status?status=${status}`
    );
    return response.data;
  },

  confirm: async (id: number): Promise<Appointment> => {
    const response = await apiClient.put<Appointment>(`/appointments/${id}/confirm`);
    return response.data;
  },

  complete: async (id: number): Promise<Appointment> => {
    const response = await apiClient.put<Appointment>(`/appointments/${id}/complete`);
    return response.data;
  },

  cancel: async (id: number): Promise<Appointment> => {
    const response = await apiClient.put<Appointment>(`/appointments/${id}/cancel`);
    return response.data;
  },

  getMyAppointments: async (): Promise<Appointment[]> => {
    const response = await apiClient.get<Appointment[]>('/appointments/my-appointments');
    return response.data;
  },

  getAssignedToMe: async (): Promise<Appointment[]> => {
    const response = await apiClient.get<Appointment[]>('/appointments/assigned-to-me');
    return response.data;
  },

  getRoleBasedAppointments: async (): Promise<Appointment[]> => {
    const response = await apiClient.get<Appointment[]>('/appointments/role-based');
    return response.data;
  },
};
