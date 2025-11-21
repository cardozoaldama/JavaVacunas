import { useAuthStore } from '@/store/authStore';
import { Users, Syringe, Calendar, Package } from 'lucide-react';

export default function Dashboard() {
  const user = useAuthStore((state) => state.user);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">
          Bienvenido, {user?.firstName}
        </h1>
        <p className="text-gray-600 mt-1">
          Sistema de Gestión de Vacunación Infantil - Paraguay
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">Niños Registrados</p>
              <p className="text-3xl font-bold text-gray-900 mt-2">-</p>
            </div>
            <div className="bg-primary-100 p-3 rounded-full">
              <Users className="text-primary-600" size={24} />
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">Vacunas Aplicadas</p>
              <p className="text-3xl font-bold text-gray-900 mt-2">-</p>
            </div>
            <div className="bg-green-100 p-3 rounded-full">
              <Syringe className="text-green-600" size={24} />
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">Citas Pendientes</p>
              <p className="text-3xl font-bold text-gray-900 mt-2">-</p>
            </div>
            <div className="bg-yellow-100 p-3 rounded-full">
              <Calendar className="text-yellow-600" size={24} />
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">Stock Total</p>
              <p className="text-3xl font-bold text-gray-900 mt-2">-</p>
            </div>
            <div className="bg-purple-100 p-3 rounded-full">
              <Package className="text-purple-600" size={24} />
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            Esquema de Vacunación PAI Paraguay
          </h2>
          <div className="space-y-3">
            <div className="flex justify-between items-center py-2 border-b">
              <span className="text-sm font-medium text-gray-700">Al nacer</span>
              <span className="text-sm text-gray-600">BCG, Hepatitis B</span>
            </div>
            <div className="flex justify-between items-center py-2 border-b">
              <span className="text-sm font-medium text-gray-700">2 meses</span>
              <span className="text-sm text-gray-600">Pentavalente, IPV, Rotavirus, Neumococo</span>
            </div>
            <div className="flex justify-between items-center py-2 border-b">
              <span className="text-sm font-medium text-gray-700">4 meses</span>
              <span className="text-sm text-gray-600">Pentavalente, IPV, Rotavirus, Neumococo</span>
            </div>
            <div className="flex justify-between items-center py-2 border-b">
              <span className="text-sm font-medium text-gray-700">6 meses</span>
              <span className="text-sm text-gray-600">Pentavalente, IPV, Neumococo</span>
            </div>
            <div className="flex justify-between items-center py-2 border-b">
              <span className="text-sm font-medium text-gray-700">12 meses</span>
              <span className="text-sm text-gray-600">SPR, Fiebre Amarilla</span>
            </div>
            <div className="flex justify-between items-center py-2">
              <span className="text-sm font-medium text-gray-700">18 meses</span>
              <span className="text-sm text-gray-600">Varicela, SPR (refuerzo), DPT, bOPV</span>
            </div>
          </div>
        </div>

        <div className="card">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            Acceso Rápido
          </h2>
          <div className="space-y-3">
            <a
              href="/children"
              className="block p-4 bg-gray-50 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <h3 className="font-medium text-gray-900">Gestionar Niños</h3>
              <p className="text-sm text-gray-600 mt-1">
                Registrar y consultar información de niños
              </p>
            </a>
            <a
              href="/vaccines"
              className="block p-4 bg-gray-50 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <h3 className="font-medium text-gray-900">Catálogo de Vacunas</h3>
              <p className="text-sm text-gray-600 mt-1">
                Ver vacunas disponibles en el sistema
              </p>
            </a>
            <a
              href="/appointments"
              className="block p-4 bg-gray-50 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <h3 className="font-medium text-gray-900">Agendar Citas</h3>
              <p className="text-sm text-gray-600 mt-1">
                Programar citas de vacunación
              </p>
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
