import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuthStore } from '@/store/authStore';
import { appointmentsApi } from '@/api/appointmentsApi';
import CreateAppointmentModal from '@/components/CreateAppointmentModal';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { Plus, Calendar, CheckCircle, XCircle, Clock, Filter } from 'lucide-react';
import type { Appointment } from '@/types';

export default function Appointments() {
  const user = useAuthStore((state) => state.user);
  const queryClient = useQueryClient();
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [statusFilter, setStatusFilter] = useState<Appointment['status'] | 'ALL'>('ALL');

  const { data: appointments, isLoading, error } = useQuery({
    queryKey: ['appointments'],
    queryFn: appointmentsApi.getAll,
  });

  const confirmMutation = useMutation({
    mutationFn: appointmentsApi.confirm,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
    },
  });

  const completeMutation = useMutation({
    mutationFn: appointmentsApi.complete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
    },
  });

  const cancelMutation = useMutation({
    mutationFn: appointmentsApi.cancel,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
    },
  });

  const canManageAppointments = user?.role === 'DOCTOR' || user?.role === 'NURSE';

  const filteredAppointments = appointments?.filter((apt) => {
    if (statusFilter === 'ALL') return true;
    return apt.status === statusFilter;
  });

  const getStatusBadgeClass = (status: Appointment['status']) => {
    const baseClass = 'px-2 py-1 text-xs font-semibold rounded-full';
    switch (status) {
      case 'SCHEDULED':
        return `${baseClass} bg-blue-100 text-blue-800`;
      case 'CONFIRMED':
        return `${baseClass} bg-green-100 text-green-800`;
      case 'COMPLETED':
        return `${baseClass} bg-gray-100 text-gray-800`;
      case 'CANCELLED':
        return `${baseClass} bg-red-100 text-red-800`;
      case 'NO_SHOW':
        return `${baseClass} bg-yellow-100 text-yellow-800`;
      default:
        return baseClass;
    }
  };

  const getStatusLabel = (status: Appointment['status']) => {
    switch (status) {
      case 'SCHEDULED': return 'Programada';
      case 'CONFIRMED': return 'Confirmada';
      case 'COMPLETED': return 'Completada';
      case 'CANCELLED': return 'Cancelada';
      case 'NO_SHOW': return 'No asistió';
      default: return status;
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-gray-500">Cargando citas...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 text-red-700 p-4 rounded-md">
        Error al cargar las citas. Por favor, intente nuevamente.
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Citas de Vacunación</h1>
          <p className="text-gray-600 mt-1">
            Gestión de citas programadas para vacunación
          </p>
        </div>
        <button
          onClick={() => setIsCreateModalOpen(true)}
          className="btn btn-primary flex items-center justify-center space-x-2"
        >
          <Plus size={20} />
          <span>Agendar Cita</span>
        </button>
      </div>

      {/* Status Filter */}
      <div className="card">
        <div className="flex items-center space-x-4">
          <Filter className="text-gray-400" size={20} />
          <div className="flex flex-wrap gap-2">
            <button
              onClick={() => setStatusFilter('ALL')}
              className={`px-4 py-2 rounded-md text-sm font-medium transition-colors ${
                statusFilter === 'ALL'
                  ? 'bg-primary-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              Todas
            </button>
            <button
              onClick={() => setStatusFilter('SCHEDULED')}
              className={`px-4 py-2 rounded-md text-sm font-medium transition-colors ${
                statusFilter === 'SCHEDULED'
                  ? 'bg-blue-600 text-white'
                  : 'bg-blue-100 text-blue-700 hover:bg-blue-200'
              }`}
            >
              Programadas
            </button>
            <button
              onClick={() => setStatusFilter('CONFIRMED')}
              className={`px-4 py-2 rounded-md text-sm font-medium transition-colors ${
                statusFilter === 'CONFIRMED'
                  ? 'bg-green-600 text-white'
                  : 'bg-green-100 text-green-700 hover:bg-green-200'
              }`}
            >
              Confirmadas
            </button>
            <button
              onClick={() => setStatusFilter('COMPLETED')}
              className={`px-4 py-2 rounded-md text-sm font-medium transition-colors ${
                statusFilter === 'COMPLETED'
                  ? 'bg-gray-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              Completadas
            </button>
            <button
              onClick={() => setStatusFilter('CANCELLED')}
              className={`px-4 py-2 rounded-md text-sm font-medium transition-colors ${
                statusFilter === 'CANCELLED'
                  ? 'bg-red-600 text-white'
                  : 'bg-red-100 text-red-700 hover:bg-red-200'
              }`}
            >
              Canceladas
            </button>
          </div>
        </div>
      </div>

      {/* Appointments List */}
      <div className="card">
        {filteredAppointments && filteredAppointments.length > 0 ? (
          <div className="space-y-4">
            {filteredAppointments.map((appointment) => (
              <div
                key={appointment.id}
                className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
              >
                <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                  {/* Appointment Info */}
                  <div className="flex-1 space-y-2">
                    <div className="flex items-center space-x-3">
                      <Calendar className="text-primary-600" size={20} />
                      <div>
                        <p className="font-semibold text-gray-900">
                          {appointment.child.firstName} {appointment.child.lastName}
                        </p>
                        <p className="text-sm text-gray-500">
                          {format(new Date(appointment.appointmentDate), "dd 'de' MMMM 'de' yyyy 'a las' HH:mm", {
                            locale: es,
                          })}
                        </p>
                      </div>
                    </div>

                    <div className="flex flex-wrap gap-2 items-center text-sm text-gray-600">
                      <span className="font-medium">Tipo:</span>
                      <span>{appointment.appointmentType}</span>
                      <span className="mx-2">•</span>
                      <span className={getStatusBadgeClass(appointment.status)}>
                        {getStatusLabel(appointment.status)}
                      </span>
                    </div>

                    {appointment.scheduledVaccines && (
                      <div className="text-sm text-gray-600">
                        <span className="font-medium">Vacunas programadas:</span>{' '}
                        {appointment.scheduledVaccines}
                      </div>
                    )}

                    {appointment.notes && (
                      <div className="text-sm text-gray-600">
                        <span className="font-medium">Notas:</span> {appointment.notes}
                      </div>
                    )}
                  </div>

                  {/* Actions */}
                  {canManageAppointments && appointment.status !== 'COMPLETED' && appointment.status !== 'CANCELLED' && (
                    <div className="flex flex-col sm:flex-row gap-2">
                      {appointment.status === 'SCHEDULED' && (
                        <button
                          onClick={() => confirmMutation.mutate(appointment.id)}
                          disabled={confirmMutation.isPending}
                          className="btn btn-secondary inline-flex items-center justify-center space-x-1 text-sm"
                        >
                          <CheckCircle size={16} />
                          <span>Confirmar</span>
                        </button>
                      )}

                      {appointment.status === 'CONFIRMED' && (
                        <button
                          onClick={() => completeMutation.mutate(appointment.id)}
                          disabled={completeMutation.isPending}
                          className="btn btn-primary inline-flex items-center justify-center space-x-1 text-sm"
                        >
                          <CheckCircle size={16} />
                          <span>Completar</span>
                        </button>
                      )}

                      <button
                        onClick={() => cancelMutation.mutate(appointment.id)}
                        disabled={cancelMutation.isPending}
                        className="btn btn-secondary inline-flex items-center justify-center space-x-1 text-sm text-red-600 hover:bg-red-50"
                      >
                        <XCircle size={16} />
                        <span>Cancelar</span>
                      </button>
                    </div>
                  )}

                  {!canManageAppointments && appointment.status === 'SCHEDULED' && (
                    <div className="flex gap-2">
                      <button
                        onClick={() => confirmMutation.mutate(appointment.id)}
                        disabled={confirmMutation.isPending}
                        className="btn btn-primary inline-flex items-center justify-center space-x-1 text-sm"
                      >
                        <CheckCircle size={16} />
                        <span>Confirmar</span>
                      </button>
                      <button
                        onClick={() => cancelMutation.mutate(appointment.id)}
                        disabled={cancelMutation.isPending}
                        className="btn btn-secondary inline-flex items-center justify-center space-x-1 text-sm text-red-600 hover:bg-red-50"
                      >
                        <XCircle size={16} />
                        <span>Cancelar</span>
                      </button>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-12">
            <Clock className="mx-auto text-gray-400 mb-4" size={48} />
            <p className="text-gray-500 mb-4">
              {statusFilter === 'ALL'
                ? 'No hay citas programadas.'
                : `No hay citas con estado "${getStatusLabel(statusFilter)}".`}
            </p>
            <button
              onClick={() => setIsCreateModalOpen(true)}
              className="btn btn-primary inline-flex items-center space-x-2"
            >
              <Plus size={20} />
              <span>Crear Primera Cita</span>
            </button>
          </div>
        )}
      </div>

      {/* Create Appointment Modal */}
      <CreateAppointmentModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
      />
    </div>
  );
}
