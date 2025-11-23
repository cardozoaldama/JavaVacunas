import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { childrenApi } from '@/api/childrenApi';
import { X } from 'lucide-react';
import type { Child } from '@/types';

interface CreateChildFormData {
  firstName: string;
  lastName: string;
  documentNumber: string;
  dateOfBirth: string;
  gender: 'M' | 'F' | 'O';
  bloodType?: string;
  birthWeight?: number;
  birthHeight?: number;
}

interface CreateChildModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function CreateChildModal({ isOpen, onClose }: CreateChildModalProps) {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<CreateChildFormData>();

  const createMutation = useMutation({
    mutationFn: (data: CreateChildFormData) => {
      return childrenApi.create(data as Omit<Child, 'id' | 'ageInMonths' | 'createdAt' | 'updatedAt'>);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['children'] });
      reset();
      setError(null);
      onClose();
    },
    onError: (err: any) => {
      setError(err.response?.data?.message || 'Error al crear el niño. Por favor, intente nuevamente.');
    },
  });

  const onSubmit = (data: CreateChildFormData) => {
    setError(null);
    createMutation.mutate(data);
  };

  const handleClose = () => {
    reset();
    setError(null);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex justify-between items-center p-6 border-b">
          <h2 className="text-2xl font-bold text-gray-900">Registrar Nuevo Niño</h2>
          <button
            onClick={handleClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
            type="button"
          >
            <X size={24} />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="p-6 space-y-6">
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md">
              {error}
            </div>
          )}

          {/* Personal Information */}
          <div className="space-y-4">
            <h3 className="text-lg font-semibold text-gray-900">Información Personal</h3>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* First Name */}
              <div>
                <label htmlFor="firstName" className="block text-sm font-medium text-gray-700 mb-1">
                  Nombre <span className="text-red-500">*</span>
                </label>
                <input
                  id="firstName"
                  type="text"
                  {...register('firstName', {
                    required: 'El nombre es requerido',
                    minLength: { value: 2, message: 'El nombre debe tener al menos 2 caracteres' },
                    maxLength: { value: 100, message: 'El nombre no puede exceder 100 caracteres' },
                  })}
                  className="input"
                  placeholder="Ej: Juan"
                />
                {errors.firstName && (
                  <p className="mt-1 text-sm text-red-600">{errors.firstName.message}</p>
                )}
              </div>

              {/* Last Name */}
              <div>
                <label htmlFor="lastName" className="block text-sm font-medium text-gray-700 mb-1">
                  Apellido <span className="text-red-500">*</span>
                </label>
                <input
                  id="lastName"
                  type="text"
                  {...register('lastName', {
                    required: 'El apellido es requerido',
                    minLength: { value: 2, message: 'El apellido debe tener al menos 2 caracteres' },
                    maxLength: { value: 100, message: 'El apellido no puede exceder 100 caracteres' },
                  })}
                  className="input"
                  placeholder="Ej: Pérez"
                />
                {errors.lastName && (
                  <p className="mt-1 text-sm text-red-600">{errors.lastName.message}</p>
                )}
              </div>

              {/* Document Number */}
              <div>
                <label htmlFor="documentNumber" className="block text-sm font-medium text-gray-700 mb-1">
                  Número de Documento <span className="text-red-500">*</span>
                </label>
                <input
                  id="documentNumber"
                  type="text"
                  {...register('documentNumber', {
                    required: 'El número de documento es requerido',
                    pattern: {
                      value: /^[0-9]{1,20}$/,
                      message: 'Solo se permiten números (máximo 20 dígitos)',
                    },
                  })}
                  className="input"
                  placeholder="Ej: 1234567"
                />
                {errors.documentNumber && (
                  <p className="mt-1 text-sm text-red-600">{errors.documentNumber.message}</p>
                )}
              </div>

              {/* Date of Birth */}
              <div>
                <label htmlFor="dateOfBirth" className="block text-sm font-medium text-gray-700 mb-1">
                  Fecha de Nacimiento <span className="text-red-500">*</span>
                </label>
                <input
                  id="dateOfBirth"
                  type="date"
                  {...register('dateOfBirth', {
                    required: 'La fecha de nacimiento es requerida',
                    validate: (value) => {
                      const birthDate = new Date(value);
                      const today = new Date();
                      if (birthDate > today) {
                        return 'La fecha de nacimiento no puede ser futura';
                      }
                      const maxAge = new Date();
                      maxAge.setFullYear(maxAge.getFullYear() - 18);
                      if (birthDate < maxAge) {
                        return 'El niño no puede tener más de 18 años';
                      }
                      return true;
                    },
                  })}
                  className="input"
                  max={new Date().toISOString().split('T')[0]}
                />
                {errors.dateOfBirth && (
                  <p className="mt-1 text-sm text-red-600">{errors.dateOfBirth.message}</p>
                )}
              </div>

              {/* Gender */}
              <div>
                <label htmlFor="gender" className="block text-sm font-medium text-gray-700 mb-1">
                  Género <span className="text-red-500">*</span>
                </label>
                <select
                  id="gender"
                  {...register('gender', { required: 'El género es requerido' })}
                  className="input"
                >
                  <option value="">Seleccione...</option>
                  <option value="M">Masculino</option>
                  <option value="F">Femenino</option>
                  <option value="O">Otro</option>
                </select>
                {errors.gender && (
                  <p className="mt-1 text-sm text-red-600">{errors.gender.message}</p>
                )}
              </div>

              {/* Blood Type */}
              <div>
                <label htmlFor="bloodType" className="block text-sm font-medium text-gray-700 mb-1">
                  Tipo de Sangre
                </label>
                <select id="bloodType" {...register('bloodType')} className="input">
                  <option value="">Seleccione...</option>
                  <option value="A+">A+</option>
                  <option value="A-">A-</option>
                  <option value="B+">B+</option>
                  <option value="B-">B-</option>
                  <option value="AB+">AB+</option>
                  <option value="AB-">AB-</option>
                  <option value="O+">O+</option>
                  <option value="O-">O-</option>
                </select>
              </div>
            </div>
          </div>

          {/* Birth Measurements */}
          <div className="space-y-4">
            <h3 className="text-lg font-semibold text-gray-900">Medidas al Nacer (Opcional)</h3>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Birth Weight */}
              <div>
                <label htmlFor="birthWeight" className="block text-sm font-medium text-gray-700 mb-1">
                  Peso al Nacer (kg)
                </label>
                <input
                  id="birthWeight"
                  type="number"
                  step="0.01"
                  {...register('birthWeight', {
                    min: { value: 0.5, message: 'El peso mínimo es 0.5 kg' },
                    max: { value: 10, message: 'El peso máximo es 10 kg' },
                  })}
                  className="input"
                  placeholder="Ej: 3.5"
                />
                {errors.birthWeight && (
                  <p className="mt-1 text-sm text-red-600">{errors.birthWeight.message}</p>
                )}
              </div>

              {/* Birth Height */}
              <div>
                <label htmlFor="birthHeight" className="block text-sm font-medium text-gray-700 mb-1">
                  Altura al Nacer (cm)
                </label>
                <input
                  id="birthHeight"
                  type="number"
                  step="0.1"
                  {...register('birthHeight', {
                    min: { value: 30, message: 'La altura mínima es 30 cm' },
                    max: { value: 70, message: 'La altura máxima es 70 cm' },
                  })}
                  className="input"
                  placeholder="Ej: 50"
                />
                {errors.birthHeight && (
                  <p className="mt-1 text-sm text-red-600">{errors.birthHeight.message}</p>
                )}
              </div>
            </div>
          </div>

          {/* Actions */}
          <div className="flex justify-end space-x-3 pt-4 border-t">
            <button
              type="button"
              onClick={handleClose}
              className="btn btn-secondary"
              disabled={createMutation.isPending}
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={createMutation.isPending}
            >
              {createMutation.isPending ? 'Guardando...' : 'Registrar Niño'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
