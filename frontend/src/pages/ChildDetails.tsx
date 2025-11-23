import { useQuery } from '@tanstack/react-query';
import { useParams, useNavigate } from 'react-router-dom';
import { childrenApi } from '@/api/childrenApi';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { ArrowLeft, User, Calendar, Droplet, Baby } from 'lucide-react';

export default function ChildDetails() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const { data: child, isLoading, error } = useQuery({
    queryKey: ['children', id],
    queryFn: () => childrenApi.getById(Number(id)),
    enabled: !!id,
  });

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-gray-500">Cargando información del niño...</div>
      </div>
    );
  }

  if (error || !child) {
    return (
      <div className="space-y-4">
        <button
          onClick={() => navigate('/children')}
          className="btn btn-secondary inline-flex items-center space-x-2"
        >
          <ArrowLeft size={20} />
          <span>Volver a la lista</span>
        </button>
        <div className="bg-red-50 text-red-700 p-4 rounded-md">
          Error al cargar la información del niño. Por favor, intente nuevamente.
        </div>
      </div>
    );
  }

  const getGenderLabel = (gender: string) => {
    switch (gender) {
      case 'M': return 'Masculino';
      case 'F': return 'Femenino';
      case 'O': return 'Otro';
      default: return gender;
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <button
          onClick={() => navigate('/children')}
          className="btn btn-secondary inline-flex items-center space-x-2"
        >
          <ArrowLeft size={20} />
          <span>Volver a la lista</span>
        </button>
      </div>

      {/* Child Information Card */}
      <div className="card">
        <div className="flex items-start justify-between mb-6">
          <div className="flex items-center space-x-4">
            <div className="bg-primary-100 rounded-full p-4">
              <User className="text-primary-600" size={32} />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">
                {child.firstName} {child.lastName}
              </h1>
              <p className="text-gray-500 mt-1">Documento: {child.documentNumber}</p>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Personal Information */}
          <div className="space-y-4">
            <h2 className="text-lg font-semibold text-gray-900 border-b pb-2">
              Información Personal
            </h2>

            <div className="space-y-3">
              <div className="flex items-start space-x-3">
                <Calendar className="text-gray-400 mt-0.5" size={20} />
                <div>
                  <p className="text-sm font-medium text-gray-500">Fecha de Nacimiento</p>
                  <p className="text-base text-gray-900">
                    {format(new Date(child.dateOfBirth), "dd 'de' MMMM 'de' yyyy", { locale: es })}
                  </p>
                </div>
              </div>

              <div className="flex items-start space-x-3">
                <Baby className="text-gray-400 mt-0.5" size={20} />
                <div>
                  <p className="text-sm font-medium text-gray-500">Edad</p>
                  <p className="text-base text-gray-900">
                    {child.ageInMonths} meses
                    {child.ageInMonths >= 12 && (
                      <span className="text-gray-500 ml-1">
                        ({Math.floor(child.ageInMonths / 12)} año{Math.floor(child.ageInMonths / 12) !== 1 ? 's' : ''})
                      </span>
                    )}
                  </p>
                </div>
              </div>

              <div className="flex items-start space-x-3">
                <User className="text-gray-400 mt-0.5" size={20} />
                <div>
                  <p className="text-sm font-medium text-gray-500">Género</p>
                  <p className="text-base text-gray-900">{getGenderLabel(child.gender)}</p>
                </div>
              </div>

              <div className="flex items-start space-x-3">
                <Droplet className="text-gray-400 mt-0.5" size={20} />
                <div>
                  <p className="text-sm font-medium text-gray-500">Tipo de Sangre</p>
                  <p className="text-base text-gray-900">{child.bloodType || 'No especificado'}</p>
                </div>
              </div>
            </div>
          </div>

          {/* Birth Measurements */}
          <div className="space-y-4">
            <h2 className="text-lg font-semibold text-gray-900 border-b pb-2">
              Medidas al Nacer
            </h2>

            <div className="space-y-3">
              <div>
                <p className="text-sm font-medium text-gray-500">Peso</p>
                <p className="text-base text-gray-900">
                  {child.birthWeight ? `${child.birthWeight} kg` : 'No especificado'}
                </p>
              </div>

              <div>
                <p className="text-sm font-medium text-gray-500">Altura</p>
                <p className="text-base text-gray-900">
                  {child.birthHeight ? `${child.birthHeight} cm` : 'No especificado'}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Metadata */}
        <div className="mt-6 pt-6 border-t">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm text-gray-500">
            <div>
              <span className="font-medium">Registrado el:</span>{' '}
              {format(new Date(child.createdAt), "dd/MM/yyyy 'a las' HH:mm", { locale: es })}
            </div>
            <div>
              <span className="font-medium">Última actualización:</span>{' '}
              {format(new Date(child.updatedAt), "dd/MM/yyyy 'a las' HH:mm", { locale: es })}
            </div>
          </div>
        </div>
      </div>

      {/* Vaccination History Placeholder */}
      <div className="card">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Historial de Vacunación</h2>
        <div className="text-center py-8 text-gray-500">
          <p>El historial de vacunación estará disponible próximamente.</p>
        </div>
      </div>

      {/* Appointments Placeholder */}
      <div className="card">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Citas Programadas</h2>
        <div className="text-center py-8 text-gray-500">
          <p>Las citas programadas estarán disponibles próximamente.</p>
        </div>
      </div>
    </div>
  );
}
