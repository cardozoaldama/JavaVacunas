import apiClient from '@/lib/api-client';

export interface UserDto {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  role: 'DOCTOR' | 'NURSE' | 'PARENT';
  licenseNumber?: string;
  isActive: string;
  lastLogin?: string;
  createdAt: string;
  updatedAt: string;
}

export const usersApi = {
  getById: async (id: number): Promise<UserDto> => {
    const response = await apiClient.get<UserDto>(`/users/${id}`);
    return response.data;
  },

  getAll: async (): Promise<UserDto[]> => {
    const response = await apiClient.get<UserDto[]>('/users');
    return response.data;
  },

  getByRole: async (role: 'DOCTOR' | 'NURSE' | 'PARENT'): Promise<UserDto[]> => {
    const response = await apiClient.get<UserDto[]>(`/users/role/${role}`);
    return response.data;
  },

  getMedicalStaff: async (): Promise<UserDto[]> => {
    const response = await apiClient.get<UserDto[]>('/users/medical-staff');
    return response.data;
  },

  getDoctors: async (): Promise<UserDto[]> => {
    const response = await apiClient.get<UserDto[]>('/users/doctors');
    return response.data;
  },

  getNurses: async (): Promise<UserDto[]> => {
    const response = await apiClient.get<UserDto[]>('/users/nurses');
    return response.data;
  },
};
