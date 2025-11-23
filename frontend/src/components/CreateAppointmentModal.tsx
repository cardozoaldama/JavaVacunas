import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { appointmentsApi } from '@/api/appointmentsApi';
import { childrenApi } from '@/api/childrenApi';
import { X } from 'lucide-react';

interface CreateAppointmentFormData {
  childId: number;
  appointmentDate: string;
  appointmentTime: string;
  appointmentType: string;
  scheduledVaccines?: string;
  notes?: string;
}

interface CreateAppointmentModalProps {
  isOpen: boolean;
  onClose: () => void;
  preselectedChildId?: number;
}

export default function CreateAppointmentModal({
  isOpen,
  onClose,
  preselectedChildId,
}: CreateAppointmentModalProps) {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const { data: children } = useQuery({
    queryKey: ['children'],
    queryFn: childrenApi.getAll,
    enabled: isOpen,
  });

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
  } = useForm<CreateAppointmentFormData>({
    defaultValues: {
      childId: preselectedChildId,
    },
  });

  const createMutation = useMutation({
    mutationFn: (data: CreateAppointmentFormData) => {
      const appointmentDateTime = `${data.appointmentDate}T${data.appointmentTime}:00`;
      return appointmentsApi.create({
        childId: data.childId,
        appointmentDate: appointmentDateTime,
        appointmentType: data.appointmentType,
        scheduledVaccines: data.scheduledVaccines,
        notes: data.notes,
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
      queryClient.invalidateQueries({ queryKey: ['upcoming-appointments'] });
      reset();
      setError(null);
      onClose();
    },
    onError: (err: any) => {
      setError(err.response?.data?.message || 'Error al crear la cita. Por favor, intente nuevamente.');
    },
  });

  const onSubmit = (data: CreateAppointmentFormData) => {
    setError(null);
    createMutation.mutate(data);
  };

  const handleClose = () => {
    reset();
    setError(null);
    onClose();
  };

  if (!isOpen) return null;

  const selectedChildId = watch('childId');
  const selectedChild = children?.find((c) => c.id === Number(selectedChildId));

  const getTodayDate = () => {
    const today = new Date();
    return today.toISOString().split('T')[0];
  };

  const getMinTime = (date: string) => {
    const today = new Date();
    const selectedDate = new Date(date);

    if (selectedDate.toDateString() === today.toDateString()) {
      const hours = today.getHours().toString().padStart(2, '0');
      const minutes = today.getMinutes().toString().padStart(2, '0');
      return `${hours}:${minutes}`;
    }
    return '08:00';
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex justify-between items-center p-6 border-b">
          <h2 className="text-2xl font-bold text-gray-900">Agendar Nueva Cita</h2>
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

          {/* Child Selection */}
          <div>
            <label htmlFor="childId" className="block text-sm font-medium text-gray-700 mb-1">
              Niño <span className="text-red-500">*</span>
            </label>
            <select
              id="childId"
              {...register('childId', {
                required: 'Debe seleccionar un niño',
                valueAsNumber: true,
              })}
              className="input"
              disabled={!!preselectedChildId}
            >
              <option value="">Seleccione un niño...</option>
              {children?.map((child) => (
                <option key={child.id} value={child.id}>
                  {child.firstName} {child.lastName} - {child.documentNumber}
                </option>
              ))}
            </select>
            {errors.childId && (
              <p className="mt-1 text-sm text-red-600">{errors.childId.message}</p>
            )}
            {selectedChild && (
              <div className="mt-2 p-3 bg-blue-50 rounded-md text-sm text-blue-900">
                <p>
                  <strong>Edad:</strong> {selectedChild.ageInMonths} meses
                </p>
                <p>
                  <strong>Fecha de nacimiento:</strong>{' '}
                  {new Date(selectedChild.dateOfBirth).toLocaleDateString('es-PY')}
                </p>
              </div>
            )}
          </div>

          {/* Appointment Date and Time */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label htmlFor="appointmentDate" className="block text-sm font-medium text-gray-700 mb-1">
                Fecha <span className="text-red-500">*</span>
              </label>
              <input
                id="appointmentDate"
                type="date"
                {...register('appointmentDate', {
                  required: 'La fecha es requerida',
                  validate: (value) => {
                    const selectedDate = new Date(value);
                    const today = new Date();
                    today.setHours(0, 0, 0, 0);
                    if (selectedDate < today) {
                      return 'La fecha no puede ser anterior a hoy';
                    }
                    return true;
                  },
                })}
                className="input"
                min={getTodayDate()}
              />
              {errors.appointmentDate && (
                <p className="mt-1 text-sm text-red-600">{errors.appointmentDate.message}</p>
              )}
            </div>

            <div>
              <label htmlFor="appointmentTime" className="block text-sm font-medium text-gray-700 mb-1">
                Hora <span className="text-red-500">*</span>
              </label>
              <input
                id="appointmentTime"
                type="time"
                {...register('appointmentTime', {
                  required: 'La hora es requerida',
                  validate: (value, formValues) => {
                    if (!formValues.appointmentDate) return true;

                    const selectedDate = new Date(formValues.appointmentDate);
                    const today = new Date();

                    if (selectedDate.toDateString() === today.toDateString()) {
                      const [hours, minutes] = value.split(':');
                      const selectedTime = new Date();
                      selectedTime.setHours(parseInt(hours), parseInt(minutes), 0, 0);

                      if (selectedTime <= today) {
                        return 'La hora debe ser futura';
                      }
                    }
                    return true;
                  },
                })}
                className="input"
                min={watch('appointmentDate') ? getMinTime(watch('appointmentDate')) : '08:00'}
              />
              {errors.appointmentTime && (
                <p className="mt-1 text-sm text-red-600">{errors.appointmentTime.message}</p>
              )}
            </div>
          </div>

          {/* Appointment Type */}
          <div>
            <label htmlFor="appointmentType" className="block text-sm font-medium text-gray-700 mb-1">
              Tipo de Cita <span className="text-red-500">*</span>
            </label>
            <select
              id="appointmentType"
              {...register('appointmentType', { required: 'El tipo de cita es requerido' })}
              className="input"
            >
              <option value="">Seleccione...</option>
              <option value="Vacunación">Vacunación</option>
              <option value="Control de niño sano">Control de niño sano</option>
              <option value="Seguimiento">Seguimiento</option>
              <option value="Consulta médica">Consulta médica</option>
              <option value="Otro">Otro</option>
            </select>
            {errors.appointmentType && (
              <p className="mt-1 text-sm text-red-600">{errors.appointmentType.message}</p>
            )}
          </div>

          {/* Scheduled Vaccines */}
          <div>
            <label htmlFor="scheduledVaccines" className="block text-sm font-medium text-gray-700 mb-1">
              Vacunas Programadas
            </label>
            <input
              id="scheduledVaccines"
              type="text"
              {...register('scheduledVaccines')}
              className="input"
              placeholder="Ej: BCG, Hepatitis B, Pentavalente"
            />
            <p className="mt-1 text-sm text-gray-500">
              Ingrese las vacunas separadas por comas (opcional)
            </p>
          </div>

          {/* Notes */}
          <div>
            <label htmlFor="notes" className="block text-sm font-medium text-gray-700 mb-1">
              Notas Adicionales
            </label>
            <textarea
              id="notes"
              {...register('notes')}
              className="input"
              rows={3}
              placeholder="Información adicional sobre la cita..."
            />
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
              {createMutation.isPending ? 'Agendando...' : 'Agendar Cita'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
