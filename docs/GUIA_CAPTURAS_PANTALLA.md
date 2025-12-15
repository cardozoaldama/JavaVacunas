# Guía de Capturas de Pantalla - Ejercicio Agenda Pediátrica

Esta guía te indica exactamente qué capturas tomar para documentar cada item del ejercicio.

---

## Item 1: Estructura del Proyecto React

### 📸 Captura 1.1 - Estructura de Carpetas
**Qué capturar:**
- Abre VS Code con el proyecto JavaVacunas
- Expande la carpeta `frontend/src/`
- Muestra las carpetas: `pages/`, `components/`, `api/`, `store/`, `types/`, `lib/`
- Asegúrate de que se vean los archivos principales en `pages/`: `Children.tsx`, `Vaccines.tsx`, `Appointments.tsx`

**Ubicación:** VS Code Explorer (panel izquierdo)

**Archivo destacado:** La estructura de carpetas del frontend

---

### 📸 Captura 1.2 - package.json
**Qué capturar:**
- Abre `frontend/package.json`
- Enfoca las líneas 1-38 (todo el archivo)
- Destaca las secciones `scripts` y `dependencies`
- Debe verse claramente: React 18.2.0, Vite, react-router-dom, @tanstack/react-query

**Archivo:** `frontend/package.json`

**Líneas importantes:** 5-9 (scripts), 11-21 (dependencies)

---

### 📸 Captura 1.3 - Vite Ejecutándose
**Qué capturar:**
- Abre una terminal en `frontend/`
- Ejecuta: `npm run dev`
- Captura la terminal mostrando:
  - "VITE v5.x.x ready in XXX ms"
  - "Local: http://localhost:5173/"
  - "ready in XX ms"

**Comando:** `npm run dev`

**Debe verse:** URL del servidor de desarrollo y tiempo de inicio

---

## Item 2: Registro de Infantes

### 📸 Captura 2.1 - Vista Children.tsx (Listado)
**Qué capturar:**
- Inicia sesión como doctor (`admin` / `admin123`)
- Navega a: http://localhost:5173/children
- Captura la página completa mostrando:
  - Título "Niños Registrados"
  - Botón "Registrar Niño"
  - Barra de búsqueda
  - Tabla con niños (si hay datos, si no, el mensaje de "No hay niños registrados")

**URL:** `/children`

**Elementos clave:** Botón "Registrar Niño", tabla de datos

---

### 📸 Captura 2.2 - Modal CreateChildModal (Vacío)
**Qué capturar:**
- En `/children`, haz clic en "Registrar Niño"
- Captura el modal completo mostrando:
  - Título "Registrar Nuevo Niño"
  - Secciones: "Información Personal" y "Medidas al Nacer (Opcional)"
  - Todos los campos vacíos
  - Botones "Cancelar" y "Registrar Niño"

**Acción:** Click en botón "Registrar Niño"

**Debe verse:** Formulario completo con todos los campos

---

### 📸 Captura 2.3 - Formulario Completado
**Qué capturar:**
- En el modal, completa el formulario con datos de ejemplo:
  - Nombre: "Juan"
  - Apellido: "Pérez"
  - Documento: "1234567"
  - Fecha de Nacimiento: (elige una fecha, ej: 15/03/2023)
  - Género: "Masculino"
  - Tipo de Sangre: "O+" (opcional)
- **NO envíes el formulario todavía**
- Captura el modal con todos los campos completados

**Debe verse:** Formulario con datos de prueba completos

---

### 📸 Captura 2.4 - Errores de Validación
**Qué capturar:**
- Abre nuevamente el modal de registro
- **Sin completar ningún campo**, haz clic en "Registrar Niño"
- Captura los mensajes de error bajo los campos:
  - "El nombre es requerido"
  - "El apellido es requerido"
  - "El número de documento es requerido"
  - "La fecha de nacimiento es requerida"
  - "El género es requerido"

**Acción:** Submit con campos vacíos

**Debe verse:** Mensajes de error en rojo bajo cada campo requerido

---

### 📸 Captura 2.5 - Tabla Actualizada
**Qué capturar:**
- Completa el formulario correctamente
- Haz clic en "Registrar Niño"
- Espera a que el modal se cierre
- Captura la tabla con el nuevo niño agregado
- Debe verse la fila nueva con: nombre, documento, fecha de nacimiento, edad, género, botón "Ver Detalles"

**Debe verse:** Nueva fila en la tabla con los datos del niño recién creado

---

### 📸 Captura 2.6 - Código del Formulario (Opcional)
**Qué capturar:**
- Abre `frontend/src/components/CreateChildModal.tsx`
- Enfoca las líneas 28-52 (configuración de react-hook-form y mutations)
- Captura el código mostrando `useForm`, `useMutation`, `onSubmit`

**Archivo:** `frontend/src/components/CreateChildModal.tsx`

**Líneas:** 28-52

---

## Item 3: Gestión de Vacunas

### 📸 Captura 3.1 - Catálogo de Vacunas
**Qué capturar:**
- Navega a: http://localhost:5173/vaccines
- Captura la página completa mostrando:
  - Título "Catálogo de Vacunas"
  - Lista de vacunas del PAI Paraguay (BCG, Pentavalente, Rotavirus, etc.)
  - Información de cada vacuna: nombre, descripción, edad recomendada, dosis

**URL:** `/vaccines`

**Debe verse:** Listado completo de vacunas con sus detalles

---

### 📸 Captura 3.2 - Sección Historial de Vacunación
**Qué capturar:**
- Navega a `/children`
- Haz clic en "Ver Detalles" de cualquier niño
- Desplázate hasta la sección "Historial de Vacunación"
- Captura la tarjeta con el mensaje "El historial de vacunación estará disponible próximamente"

**URL:** `/children/:id` (donde :id es el ID de un niño)

**Debe verse:** Tarjeta con título "Historial de Vacunación" y mensaje placeholder

---

### 📸 Captura 3.3 - Vista ChildDetails Completa
**Qué capturar:**
- En la misma página `/children/:id`
- Captura toda la vista mostrando:
  - Información personal del niño (nombre, documento, fecha de nacimiento, edad, género, tipo de sangre)
  - Medidas al nacer
  - Sección de "Historial de Vacunación"
  - Sección de "Citas Programadas"

**URL:** `/children/:id`

**Debe verse:** Vista completa de detalles del niño con todas las secciones

---

### 📸 Captura 3.4 - Código Backend (Opcional)
**Qué capturar:**
- Abre `backend/src/main/java/com/javacunas/entity/VaccinationRecord.java`
- Captura la clase completa mostrando los atributos:
  - `child` (relación con Child)
  - `vaccine` (relación con Vaccine)
  - `applicationDate`
  - `lotNumber`
  - `administeredBy`
  - `notes`

**Archivo:** `backend/.../entity/VaccinationRecord.java`

**Debe verse:** Entidad con relaciones Many-to-One a Child y Vaccine

---

## Item 4: Agenda Pediátrica

### 📸 Captura 4.1 - Vista Appointments Completa
**Qué capturar:**
- Navega a: http://localhost:5173/appointments
- Captura la página completa mostrando:
  - Título "Citas de Vacunación"
  - Botón "Agendar Cita"
  - Barra de filtros por estado (Todas, Programadas, Confirmadas, Completadas, Canceladas)
  - Lista de citas existentes (o mensaje "No hay citas programadas")

**URL:** `/appointments`

**Debe verse:** Vista completa de la agenda

---

### 📸 Captura 4.2 - Barra de Filtros
**Qué capturar:**
- En `/appointments`
- Enfoca específicamente la barra de filtros con los botones:
  - Todas (gris)
  - Programadas (azul)
  - Confirmadas (verde)
  - Completadas (gris)
  - Canceladas (rojo)
- Captura con un filtro seleccionado (ej: "Programadas")

**Elemento:** Barra de filtros de estado

**Debe verse:** Botones de filtro con colores distintivos

---

### 📸 Captura 4.3 - Tarjeta de Cita Individual
**Qué capturar:**
- En `/appointments`
- Captura una sola tarjeta de cita mostrando:
  - Icono de calendario
  - Nombre completo del niño
  - Fecha y hora formateada (ej: "15 de enero de 2025 a las 10:00")
  - Tipo de cita
  - Badge de estado (color según estado)
  - Vacunas programadas
  - Notas (si hay)
  - Botones de acción (Confirmar, Completar, Cancelar según estado)

**Elemento:** Tarjeta individual de cita

**Debe verse:** Todos los datos de la cita en formato legible

---

### 📸 Captura 4.4 - Badges de Estado
**Qué capturar:**
- Captura múltiples citas mostrando diferentes badges de estado:
  - Badge azul: "Programada"
  - Badge verde: "Confirmada"
  - Badge gris: "Completada"
  - Badge rojo: "Cancelada"
- Puede ser un montaje de varias capturas o una vista con citas en diferentes estados

**Debe verse:** Variedad de estados con colores distintivos

---

### 📸 Captura 4.5 - Botones de Acción
**Qué capturar:**
- Captura una cita en estado "Programada" mostrando botones:
  - "Confirmar" (verde)
  - "Cancelar" (rojo)
- Captura otra cita en estado "Confirmada" mostrando:
  - "Completar" (azul)
  - "Cancelar" (rojo)

**Debe verse:** Botones de acción que cambian según el estado de la cita

---

### 📸 Captura 4.6 - Modal CreateAppointmentModal
**Qué capturar:**
- En `/appointments`, haz clic en "Agendar Cita"
- Captura el modal mostrando:
  - Título del modal
  - Selector de niño
  - Date-time picker para fecha y hora
  - Selector de tipo de cita (Vacunación, Control, Seguimiento)
  - Campo de vacunas programadas
  - Campo de notas
  - Botones "Cancelar" y "Crear Cita"

**Acción:** Click en "Agendar Cita"

**Debe verse:** Formulario completo de creación de cita

---

### 📸 Captura 4.7 - Vista Filtrada
**Qué capturar:**
- En `/appointments`
- Haz clic en el filtro "Confirmadas"
- Captura la vista mostrando:
  - Botón "Confirmadas" resaltado
  - Solo citas con estado "Confirmada"
  - Mensaje "No hay citas con estado 'Confirmada'" si no hay datos

**Acción:** Aplicar filtro "Confirmadas"

**Debe verse:** Lista filtrada solo con citas confirmadas

---

## Item 5: Navegación Básica

### 📸 Captura 5.1 - Header Completo
**Qué capturar:**
- En cualquier página (excepto login)
- Captura el header mostrando:
  - Logo "JavaVacunas"
  - Menú de navegación: Inicio, Niños, Vacunas, Citas
  - Iconos al lado de cada link
  - Información del usuario (nombre, rol)
  - Botón "Salir"

**Elemento:** Header (barra superior)

**Debe verse:** Navegación completa con todos los elementos

---

### 📸 Captura 5.2 - Menú de Navegación Detallado
**Qué capturar:**
- Enfoca específicamente el menú de navegación mostrando:
  - 🏠 Inicio
  - 👥 Niños
  - 💉 Vacunas
  - 📅 Citas
- Todos con sus iconos respectivos

**Elemento:** Menú de navegación (4 links)

**Debe verse:** Links con iconos descriptivos

---

### 📸 Captura 5.3 - Hover State
**Qué capturar:**
- Pasa el mouse sobre uno de los links del menú
- Captura el cambio de estilo (hover state):
  - Color cambia a primary-600
  - Background gris claro (hover:bg-gray-50)

**Acción:** Hover sobre un link de navegación

**Debe verse:** Cambio visual al pasar el mouse

---

### 📸 Captura 5.4 - Secuencia de Navegación (3 capturas)
**Qué capturar:**

**5.4a - Página Niños:**
- URL: http://localhost:5173/children
- Captura completa mostrando URL en la barra del navegador

**5.4b - Navegación a Vacunas:**
- Haz clic en "Vacunas" en el menú
- **Observa que la página NO se recarga** (no hay spinner de carga en la pestaña)
- URL: http://localhost:5173/vaccines
- Captura completa mostrando nueva URL

**5.4c - Navegación a Citas:**
- Haz clic en "Citas" en el menú
- **Sin recarga de página**
- URL: http://localhost:5173/appointments
- Captura completa mostrando nueva URL

**Debe verse:** URLs diferentes, misma pestaña, sin recargas

---

### 📸 Captura 5.5 - React DevTools
**Qué capturar:**
- Instala React DevTools (extensión de Chrome/Firefox)
- Abre DevTools (F12)
- Ve a la pestaña "⚛️ Components"
- Expande el árbol de componentes mostrando:
  - BrowserRouter
    - Routes
      - Route (Layout)
        - Outlet
          - Children / Vaccines / Appointments (según la página actual)

**Herramienta:** React DevTools

**Debe verse:** Estructura de rutas de React Router

---

### 📸 Captura 5.6 - Código App.tsx
**Qué capturar:**
- Abre `frontend/src/App.tsx`
- Enfoca las líneas 26-50
- Captura el código mostrando:
  - `<BrowserRouter>`
  - `<Routes>`
  - `<Route path="/" element={<Layout />}>`
  - Rutas anidadas: children, vaccines, appointments

**Archivo:** `frontend/src/App.tsx`

**Líneas:** 26-50

---

### 📸 Captura 5.7 - Network Tab (Sin Recargas)
**Qué capturar:**
- Abre DevTools (F12)
- Ve a la pestaña "Network"
- Haz clic en "Preserve log"
- Navega entre páginas (Niños → Vacunas → Citas)
- Captura la pestaña Network mostrando:
  - **NO hay requests HTML** (solo XHR/Fetch para datos de API)
  - Solo se ven llamadas a `/api/v1/...`
  - No hay request a `children`, `vaccines`, `appointments` (como documentos HTML)

**Herramienta:** Chrome DevTools - Network

**Debe verse:** Solo requests de API, no de páginas HTML

---

## Resumen de Capturas por Item

| Item | Descripción | Cantidad de Capturas |
|------|-------------|----------------------|
| Item 1 | Estructura del Proyecto | 3 capturas |
| Item 2 | Registro de Infantes | 6 capturas (5 obligatorias + 1 opcional) |
| Item 3 | Gestión de Vacunas | 4 capturas (3 obligatorias + 1 opcional) |
| Item 4 | Agenda Pediátrica | 7 capturas |
| Item 5 | Navegación Básica | 7 capturas |

**Total: 27 capturas** (24 obligatorias + 3 opcionales de código)

---

## Tips para Mejores Capturas

### 1. Configuración del Navegador
- Usa modo de ventana completa o ventana grande (min 1280px ancho)
- Muestra la URL en la barra de direcciones cuando sea relevante
- Cierra pestañas innecesarias para mayor claridad

### 2. Zoom y Resolución
- Usa zoom 100% para capturas completas
- Puedes usar zoom 125% para capturas de detalles (códigos, errores)
- Asegúrate de que el texto sea legible

### 3. Datos de Prueba
- Usa nombres de ejemplo consistentes (Juan Pérez, María González, etc.)
- Usa fechas recientes para los niños (menos de 18 años)
- Asegúrate de tener varias citas en diferentes estados para Item 4

### 4. Herramientas Recomendadas
- **Captura de pantalla completa:** Windows: Win+Shift+S, macOS: Cmd+Shift+4
- **Captura de región:** Mismas teclas, arrastra para seleccionar área
- **Captura de código:** Usa screenshots desde VS Code directamente
- **Anotaciones:** Puedes usar Paint, Snagit, o herramientas online para resaltar elementos

### 5. Organización de Archivos
Organiza tus capturas con nombres descriptivos:
```
capturas/
├── item1/
│   ├── 1.1-estructura-carpetas.png
│   ├── 1.2-package-json.png
│   └── 1.3-vite-running.png
├── item2/
│   ├── 2.1-children-listado.png
│   ├── 2.2-modal-vacio.png
│   ├── 2.3-formulario-completado.png
│   ├── 2.4-errores-validacion.png
│   ├── 2.5-tabla-actualizada.png
│   └── 2.6-codigo-formulario.png (opcional)
├── item3/
│   └── ...
├── item4/
│   └── ...
└── item5/
    └── ...
```

---

## Checklist de Verificación

Antes de finalizar, verifica que tienes:

### Item 1: Estructura
- [ ] Estructura de carpetas del frontend visible
- [ ] package.json con dependencias de React 18 + Vite
- [ ] Terminal mostrando Vite ejecutándose

### Item 2: Registro de Infantes
- [ ] Vista de listado de niños (Children.tsx)
- [ ] Modal de registro vacío
- [ ] Formulario completado con datos de prueba
- [ ] Errores de validación en campos requeridos
- [ ] Tabla actualizada con nuevo niño
- [ ] (Opcional) Código de react-hook-form

### Item 3: Gestión de Vacunas
- [ ] Catálogo de vacunas del PAI Paraguay
- [ ] Sección de historial de vacunación en ChildDetails
- [ ] Vista completa de detalles del niño
- [ ] (Opcional) Código de entidad VaccinationRecord

### Item 4: Agenda Pediátrica
- [ ] Vista completa de Appointments
- [ ] Barra de filtros por estado
- [ ] Tarjeta individual de cita con todos los datos
- [ ] Diferentes badges de estado (colores)
- [ ] Botones de acción según estado
- [ ] Modal de creación de cita
- [ ] Vista filtrada por un estado específico

### Item 5: Navegación
- [ ] Header con menú completo
- [ ] Detalle del menú con iconos
- [ ] Hover state en links
- [ ] Secuencia de navegación (3 URLs diferentes)
- [ ] React DevTools mostrando estructura de rutas
- [ ] Código de App.tsx con React Router
- [ ] Network tab sin requests HTML (solo API)

---

## Preguntas Frecuentes

**P: ¿Debo crear datos de prueba antes de capturar?**
R: Sí, especialmente para Items 2, 3 y 4. Crea al menos 2-3 niños y 3-4 citas en diferentes estados.

**P: ¿Puedo usar capturas de código en lugar de screenshots?**
R: Para código, puedes usar screenshots de VS Code o copiar el código formateado. Ambos son válidos.

**P: ¿Necesito capturas de la base de datos?**
R: No es necesario, pero si quieres mostrar la persistencia de datos, puedes incluir una captura de DBeaver o SQL Developer mostrando las tablas.

**P: ¿Qué hago si no tengo datos en alguna vista?**
R: Primero registra datos de prueba usando los formularios de la aplicación. Si una funcionalidad no está implementada (como el historial de vacunación en frontend), captura el placeholder que muestra "estará disponible próximamente".

---

**¡Buena suerte con tus capturas de pantalla!**

Si tienes dudas sobre alguna captura específica, consulta el documento principal `EJERCICIO_AGENDA_PEDIATRICA.md` para más contexto sobre cada funcionalidad.
