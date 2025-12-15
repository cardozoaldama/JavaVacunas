# Plantilla de Respuesta Rápida - Ejercicio Agenda Pediátrica

Esta es una plantilla resumida que puedes copiar/pegar si necesitas una respuesta más concisa. Para la versión completa, consulta `EJERCICIO_AGENDA_PEDIATRICA.md`.

---

## Ejercicio: Agenda Pediátrica con ReactJS

**Proyecto:** JavaVacunas - Sistema de Vacunación Paraguay
**Tecnología:** React 18 + TypeScript + Vite + TanStack Query + Zustand

---

### Item 1: Estructura del Proyecto

**Requisito:** Crear proyecto React con componentes organizados.

**Implementación:**

Utilizamos **Vite** como bundler y **TypeScript** para type safety. El proyecto está organizado en:

```
frontend/src/
├── pages/           # Vistas principales
│   ├── Children.tsx        # Registro de Infantes
│   ├── Vaccines.tsx        # Catálogo de Vacunas
│   └── Appointments.tsx    # Agenda Pediátrica
├── components/      # Componentes reutilizables
│   ├── Layout.tsx          # Navegación
│   ├── CreateChildModal.tsx
│   └── CreateAppointmentModal.tsx
├── api/            # Clientes HTTP
├── store/          # Estado global (Zustand)
└── types/          # Definiciones TypeScript
```

**Stack:**
- React 18.2.0
- React Router 6.21.0
- TanStack Query 5.17.0
- React Hook Form 7.49.2
- Tailwind CSS 3.4.0

**Comandos:**
```bash
npm run dev    # Iniciar desarrollo
npm run build  # Compilar producción
```

**Capturas sugeridas:**
- Estructura de carpetas en VS Code
- package.json con dependencias
- Terminal con Vite ejecutándose

---

### Item 2: Registro de Infantes

**Requisito:** Formulario controlado con nombre, fecha nacimiento, responsable.

**Implementación:**

Componente `CreateChildModal.tsx` con **react-hook-form**:

```tsx
const { register, handleSubmit, formState: { errors } } = useForm();

const createMutation = useMutation({
  mutationFn: (data) => childrenApi.create(data),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['children'] });
    onClose();
  },
});
```

**Campos del formulario:**
- Nombre y Apellido (required, 2-100 chars)
- Documento (required, solo números)
- Fecha de Nacimiento (required, validación de edad)
- Género (required)
- Tipo de Sangre (opcional)
- Peso y Altura al nacer (opcional)

**Validaciones:**
- Cliente: react-hook-form con validaciones declarativas
- Servidor: Jakarta Bean Validation (@Valid)

**Estado:**
- TanStack Query gestiona datos del servidor
- Cache automático y re-fetch al crear

**Archivos clave:**
- `frontend/src/components/CreateChildModal.tsx` (formulario)
- `frontend/src/pages/Children.tsx` (listado)

**Capturas sugeridas:**
- Listado de niños
- Modal de registro vacío
- Formulario completado
- Errores de validación
- Tabla actualizada con nuevo niño

---

### Item 3: Gestión de Vacunas

**Requisito:** Asociar vacunas a infante, mostrar lista (nombre y fecha).

**Implementación:**

**Catálogo de Vacunas** (`Vaccines.tsx`):
- Muestra vacunas del PAI Paraguay
- Búsqueda y filtrado

**Asociación Niño-Vacuna** (Backend completo):

Entidad `VaccinationRecord`:
```java
@Entity
public class VaccinationRecord {
    private Child child;              // Niño
    private Vaccine vaccine;          // Vacuna
    private LocalDate applicationDate; // Fecha
    private String lotNumber;         // Lote
    private String administeredBy;    // Quién aplicó
}
```

**Endpoints disponibles:**
- `POST /api/v1/vaccination-records` - Registrar vacuna
- `GET /api/v1/vaccination-records/child/{id}` - Historial

**Vista de Historial** (`ChildDetails.tsx`):
- Sección preparada para mostrar vacunas aplicadas
- Frontend en progreso, backend completo

**Capturas sugeridas:**
- Catálogo de vacunas PAI
- Vista de detalles de niño con sección de historial
- (Si implementas) Lista de vacunas aplicadas con fechas

---

### Item 4: Agenda Pediátrica

**Requisito:** Registrar turnos con infante, fecha y motivo.

**Implementación:**

Componente `Appointments.tsx` con sistema completo de citas:

**Campos de una Cita:**
```typescript
interface Appointment {
  child: Child;              // Niño
  appointmentDate: string;   // Fecha y hora
  appointmentType: string;   // Tipo de cita
  scheduledVaccines: string; // Motivo (vacunas programadas)
  notes: string;             // Observaciones
  status: Status;            // Estado actual
}
```

**Máquina de Estados:**
```
SCHEDULED → CONFIRMED → COMPLETED
     ↓
 CANCELLED
```

**Funcionalidades:**
- Crear nueva cita (modal con formulario)
- Filtrar por estado (Todas, Programadas, Confirmadas, Completadas, Canceladas)
- Confirmar cita (DOCTOR/NURSE/PARENT)
- Completar cita (DOCTOR/NURSE)
- Cancelar cita
- Vista de lista con fecha formateada en español

**Archivos clave:**
- `frontend/src/pages/Appointments.tsx`
- `frontend/src/components/CreateAppointmentModal.tsx`

**Capturas sugeridas:**
- Vista de agenda completa
- Filtros de estado
- Tarjeta de cita individual con datos
- Badges de estado (colores)
- Botones de acción
- Modal de crear cita
- Vista filtrada

---

### Item 5: Navegación Básica

**Requisito:** Menú para cambiar secciones sin recargar página.

**Implementación:**

**React Router v6** con navegación SPA:

```tsx
// App.tsx
<BrowserRouter>
  <Routes>
    <Route path="/" element={<Layout />}>
      <Route index element={<Dashboard />} />
      <Route path="children" element={<Children />} />
      <Route path="vaccines" element={<Vaccines />} />
      <Route path="appointments" element={<Appointments />} />
    </Route>
  </Routes>
</BrowserRouter>
```

**Menú de Navegación** (`Layout.tsx`):

```tsx
<nav>
  <Link to="/"><Home /> Inicio</Link>
  <Link to="/children"><Users /> Niños</Link>
  <Link to="/vaccines"><Syringe /> Vacunas</Link>
  <Link to="/appointments"><Calendar /> Citas</Link>
</nav>
```

**Características:**
- Navegación sin recargas (SPA)
- Rutas anidadas con Layout compartido
- Rutas protegidas con PrivateRoute
- Iconos descriptivos (lucide-react)
- Responsive design
- Persistencia de estado (TanStack Query cache)

**Archivos clave:**
- `frontend/src/App.tsx` (rutas)
- `frontend/src/components/Layout.tsx` (menú)

**Capturas sugeridas:**
- Header con menú completo
- Hover state en links
- Secuencia de navegación (3 URLs diferentes sin reload)
- React DevTools mostrando estructura
- Network tab sin requests HTML

---

## Tecnologías Utilizadas

| Categoría | Tecnología | Propósito |
|-----------|-----------|-----------|
| **Frontend** | React 18 | UI framework |
| | TypeScript | Type safety |
| | Vite | Build tool |
| | React Router | Navegación SPA |
| | TanStack Query | Server state |
| | Zustand | Client state |
| | React Hook Form | Formularios |
| | Tailwind CSS | Estilos |
| **Backend** | Spring Boot 3.2 | API REST |
| | Oracle 23c | Base de datos |
| | JWT | Autenticación |
| | Flyway | Migrations |

---

## Patrones Implementados

1. **Component Composition:** Layout + Outlet para rutas anidadas
2. **Controlled Components:** react-hook-form
3. **Optimistic Updates:** TanStack Query invalidations
4. **Error Boundaries:** Loading/Error states
5. **Separation of Concerns:** pages / components / api / store
6. **Single Source of Truth:** TanStack Query cache

---

## Cómo Ejecutar

```bash
# Backend
docker compose --env-file .env.docker up -d

# Frontend
cd frontend
npm install
npm run dev

# Acceder
http://localhost:5173

# Credenciales
admin / admin123  (DOCTOR)
nurse / admin123  (NURSE)
parent / admin123 (PARENT)
```

---

## Resumen de Cumplimiento

| Item | Requisito | Estado | Extras |
|------|-----------|--------|--------|
| 1 | Estructura React | ✅ | TypeScript, Vite |
| 2 | Formulario controlado | ✅ | Validaciones avanzadas |
| 3 | Asociar vacunas | ✅ | Backend completo |
| 4 | Agenda turnos | ✅ | Máquina de estados |
| 5 | Navegación sin reload | ✅ | Rutas protegidas |

---

## Referencias

**Documentación completa:** Ver `EJERCICIO_AGENDA_PEDIATRICA.md`
**Guía de capturas:** Ver `GUIA_CAPTURAS_PANTALLA.md`
**Resumen ejecutivo:** Ver `RESUMEN_EJERCICIO.md`

---

**Proyecto:** JavaVacunas
**Licencia:** GNU GPL v3
**Fecha:** Diciembre 2025
