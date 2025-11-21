export default function Appointments() {
  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Citas de Vacunación</h1>
          <p className="text-gray-600 mt-1">
            Gestión de citas programadas para vacunación
          </p>
        </div>
        <button className="btn btn-primary">
          Agendar Cita
        </button>
      </div>

      <div className="card">
        <div className="text-center py-12">
          <p className="text-gray-500">No hay citas programadas.</p>
          <button className="btn btn-primary mt-4">
            Crear Primera Cita
          </button>
        </div>
      </div>
    </div>
  );
}
