import { useQuery } from '@tanstack/react-query';
import { vaccinesApi } from '@/api/vaccinesApi';
import { Syringe } from 'lucide-react';

export default function Vaccines() {
  const { data: vaccines, isLoading, error } = useQuery({
    queryKey: ['vaccines'],
    queryFn: vaccinesApi.getAll,
  });

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-gray-500">Cargando vacunas...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 text-red-700 p-4 rounded-md">
        Error al cargar las vacunas. Por favor, intente nuevamente.
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Catálogo de Vacunas</h1>
        <p className="text-gray-600 mt-1">
          Vacunas disponibles en el Programa Ampliado de Inmunizaciones (PAI)
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {vaccines && vaccines.length > 0 ? (
          vaccines.map((vaccine) => (
            <div key={vaccine.id} className="card hover:shadow-lg transition-shadow">
              <div className="flex items-start space-x-3">
                <div className="bg-primary-100 p-2 rounded-lg">
                  <Syringe className="text-primary-600" size={24} />
                </div>
                <div className="flex-1">
                  <h3 className="text-lg font-semibold text-gray-900">
                    {vaccine.name}
                  </h3>
                  <p className="text-sm text-gray-600 mt-1">
                    {vaccine.description}
                  </p>
                </div>
              </div>

              <div className="mt-4 space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-gray-500">Previene:</span>
                  <span className="font-medium text-gray-900">{vaccine.diseasePrevented}</span>
                </div>
                {vaccine.manufacturer && (
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Fabricante:</span>
                    <span className="font-medium text-gray-900">{vaccine.manufacturer}</span>
                  </div>
                )}
                <div className="flex justify-between text-sm">
                  <span className="text-gray-500">Dosis:</span>
                  <span className="font-medium text-gray-900">{vaccine.doseCount}</span>
                </div>
                {vaccine.minimumAgeMonths !== null && (
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Edad mínima:</span>
                    <span className="font-medium text-gray-900">
                      {vaccine.minimumAgeMonths} {vaccine.minimumAgeMonths === 1 ? 'mes' : 'meses'}
                    </span>
                  </div>
                )}
              </div>

              <div className="mt-4 pt-4 border-t">
                <span
                  className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${
                    vaccine.isActive
                      ? 'bg-green-100 text-green-800'
                      : 'bg-gray-100 text-gray-800'
                  }`}
                >
                  {vaccine.isActive ? 'Activa' : 'Inactiva'}
                </span>
              </div>
            </div>
          ))
        ) : (
          <div className="col-span-3 text-center py-12">
            <p className="text-gray-500">No hay vacunas disponibles.</p>
          </div>
        )}
      </div>
    </div>
  );
}
