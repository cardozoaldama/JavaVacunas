import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { childrenApi } from '@/api/childrenApi';
import CreateChildModal from '@/components/CreateChildModal';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { Plus, Search, Eye } from 'lucide-react';

export default function Children() {
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  const { data: children, isLoading, error } = useQuery({
    queryKey: ['children'],
    queryFn: childrenApi.getAll,
  });

  const canManageChildren = user?.role === 'DOCTOR' || user?.role === 'NURSE';

  const filteredChildren = children?.filter((child) => {
    if (!searchQuery) return true;
    const query = searchQuery.toLowerCase();
    return (
      child.firstName.toLowerCase().includes(query) ||
      child.lastName.toLowerCase().includes(query) ||
      child.documentNumber.includes(query)
    );
  });

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-gray-500">Cargando niños...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 text-red-700 p-4 rounded-md">
        Error al cargar los niños. Por favor, intente nuevamente.
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Niños Registrados</h1>
          <p className="text-gray-600 mt-1">
            Gestión de registros de niños del sistema
          </p>
        </div>
        {canManageChildren && (
          <button
            onClick={() => setIsCreateModalOpen(true)}
            className="btn btn-primary flex items-center justify-center space-x-2"
          >
            <Plus size={20} />
            <span>Registrar Niño</span>
          </button>
        )}
      </div>

      {/* Search Bar */}
      <div className="card">
        <div className="flex items-center space-x-2">
          <Search className="text-gray-400" size={20} />
          <input
            type="text"
            placeholder="Buscar por nombre o documento..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="flex-1 border-0 focus:ring-0 text-gray-900 placeholder-gray-400"
          />
        </div>
      </div>

      {/* Children List */}
      <div className="card">
        {filteredChildren && filteredChildren.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Nombre
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Documento
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Fecha de Nacimiento
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Edad (meses)
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Género
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Tipo de Sangre
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Acciones
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredChildren.map((child) => (
                  <tr key={child.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">
                        {child.firstName} {child.lastName}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-500">{child.documentNumber}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-500">
                        {format(new Date(child.dateOfBirth), 'dd/MM/yyyy', { locale: es })}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-500">{child.ageInMonths}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="text-sm text-gray-500">
                        {child.gender === 'M' ? 'Masculino' : child.gender === 'F' ? 'Femenino' : 'Otro'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="text-sm text-gray-500">
                        {child.bloodType || '-'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <button
                        onClick={() => navigate(`/children/${child.id}`)}
                        className="text-primary-600 hover:text-primary-900 inline-flex items-center space-x-1"
                      >
                        <Eye size={16} />
                        <span>Ver Detalles</span>
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="text-center py-12">
            <p className="text-gray-500 mb-4">
              {searchQuery
                ? 'No se encontraron niños que coincidan con la búsqueda.'
                : 'No hay niños registrados en el sistema.'}
            </p>
            {canManageChildren && !searchQuery && (
              <button
                onClick={() => setIsCreateModalOpen(true)}
                className="btn btn-primary inline-flex items-center space-x-2"
              >
                <Plus size={20} />
                <span>Registrar el Primer Niño</span>
              </button>
            )}
          </div>
        )}
      </div>

      {/* Create Child Modal */}
      <CreateChildModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
      />
    </div>
  );
}
