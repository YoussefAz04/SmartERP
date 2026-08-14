# SmartERP Frontend

This folder contains the Angular frontend of SmartERP.

The frontend is responsible for the user interface, dashboards, forms, charts, multilingual labels, notifications, and communication with the Spring Boot backend API.

## Technology Stack

- Angular 17
- TypeScript
- Angular Router
- Angular Forms
- Angular Material
- Bootstrap
- PrimeNG
- ApexCharts
- SweetAlert2
- ngx-translate

## Project Structure

- `src/app/auth`: authentication pages and login logic.
- `src/app/common-component`: shared layout components such as header and sidebars.
- `src/app/core`: Angular services, guards, helpers, and technical logic.
- `src/app/core-component`: main ERP pages and business modules.
- `src/app/model`: TypeScript models used by the frontend.
- `src/app/shared`: reusable modules, utilities, notifications, and shared helpers.
- `src/assets/i18n`: English and French translation files.

## Development Server

Install dependencies:

```bash
npm install
```

Start the Angular development server:

```bash
npm start
```

Open the application at:

```text
http://localhost:4200
```

The application reloads automatically when source files change.

## Build

Generate a production build:

```bash
npm run build
```

The build output is generated in the `dist/` directory.

## Tests

Run unit tests:

```bash
npm test
```

## Backend Connection

The frontend communicates with the Spring Boot backend through Angular services located mainly in:

```text
src/app/core/service
```

Authenticated requests include the JWT token so the backend can verify the connected user, role, permissions, and company.
