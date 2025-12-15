export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: 'DOCTOR' | 'NURSE' | 'PARENT';
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  userId: number;
  username: string;
  email: string;
  role: 'DOCTOR' | 'NURSE' | 'PARENT';
  firstName: string;
  lastName: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  email: string;
  role: 'DOCTOR' | 'NURSE' | 'PARENT';
  licenseNumber?: string;
}

export interface Child {
  id: number;
  firstName: string;
  lastName: string;
  documentNumber: string;
  dateOfBirth: string;
  gender: 'M' | 'F' | 'O';
  bloodType?: string;
  birthWeight?: number;
  birthHeight?: number;
  ageInMonths: number;
  createdAt: string;
  updatedAt: string;
}

export interface Vaccine {
  id: number;
  name: string;
  description?: string;
  manufacturer?: string;
  diseasePrevented: string;
  doseCount: number;
  minimumAgeMonths?: number;
  isActive: boolean;
}

export interface VaccinationRecord {
  id: number;
  childId: number;
  childName: string;
  vaccineId: number;
  vaccineName: string;
  doseNumber: number;
  administrationDate: string;
  batchNumber: string;
  expirationDate: string;
  administeredById: number;
  administeredByName: string;
  administrationSite?: string;
  notes?: string;
  nextDoseDate?: string;
  createdAt: string;
}

export interface Appointment {
  id: number;
  childId: number;
  childName: string;
  appointmentDate: string;
  appointmentType: string;
  status: 'SCHEDULED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW';
  scheduledVaccines?: string;
  assignedToId?: number;
  assignedToName?: string;
  notes?: string;
  createdById?: number;
  createdByName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface VaccineInventory {
  id: number;
  vaccine: Vaccine;
  batchNumber: string;
  quantity: number;
  manufactureDate: string;
  expirationDate: string;
  storageLocation?: string;
  status: 'AVAILABLE' | 'RESERVED' | 'DEPLETED' | 'EXPIRED' | 'RECALLED';
}

export interface VaccinationSchedule {
  id: number;
  vaccine: Vaccine;
  countryCode: string;
  doseNumber: number;
  recommendedAgeMonths: number;
  ageRangeStartMonths?: number;
  ageRangeEndMonths?: number;
  isMandatory: boolean;
  notes?: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  validationErrors?: Record<string, string>;
}
