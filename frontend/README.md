# JavaVacunas Frontend

React frontend application for the JavaVacunas vaccination management system.

## Tech Stack

- React 18
- TypeScript
- Vite
- React Router
- TanStack Query (React Query)
- Zustand (State Management)
- Tailwind CSS
- Axios
- React Hook Form
- Zod
- date-fns

## Getting Started

### Prerequisites

- Node.js 18+ and npm

### Installation

```bash
npm install
```

### Configuration

Create a `.env` file based on `.env.example`:

```bash
cp .env.example .env
```

Update the API URL if needed:

```
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

### Development

```bash
npm run dev
```

The application will be available at `http://localhost:5173`

### Build

```bash
npm run build
```

The production build will be in the `dist` directory.

## Project Structure

```
src/
├── api/              # API service functions
├── components/       # Reusable components
├── features/         # Feature modules
├── lib/              # Utility libraries
├── pages/            # Page components
├── store/            # Zustand stores
├── types/            # TypeScript type definitions
└── utils/            # Helper functions
```

## Features

- **Authentication**: JWT-based authentication with role-based access
- **Child Management**: Register and manage children records
- **Vaccine Catalog**: View available vaccines
- **Appointments**: Schedule vaccination appointments
- **Dashboard**: Overview of system statistics

## UI Language

The user interface is in Spanish (Español) as it's designed for use in Paraguay.

## License

GNU General Public License v3.0
