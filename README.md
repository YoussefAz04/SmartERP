# SmartERP - Intelligent ERP

SmartERP is a web-based ERP application built as a final-year project. It helps small and medium-sized businesses manage their daily operations while using business data to support better decision-making.

The project includes user management, roles and permissions, customers, suppliers, purchase products, sales products, purchase orders, sales orders, invoices, payments, returns, credit notes, stock management, notifications, audit logs, after-sales service, and an AI intelligence module for sales analysis, forecasting, and purchase recommendations.

## Project Objective

The main goal of SmartERP is to provide a modern, secure, and intelligent ERP platform. The application is not limited to storing business operations. It also tracks stock activity, keeps a history of sensitive actions, and analyzes business data to help managers anticipate future activity.

SmartERP allows users to:

- Manage several companies from the same platform.
- Create administrators and employees with precise permissions.
- Manage customers, suppliers, purchase products, and sales products.
- Create purchase orders and sales orders.
- Generate purchase and sales invoices.
- Record partial payments and track customer and supplier balances.
- Process returns and issue credit notes with automatic stock corrections.
- Track stock quantities and stock movements.
- Receive important notifications such as low-stock alerts.
- Review audit logs for traceability.
- Manage after-sales service tickets.
- Analyze sales and forecast future performance.
- Generate purchase recommendations based on real ERP data.

## General Architecture

The project is organized into two main parts:

- `backend`: REST API developed with Spring Boot.
- `frontend`: web interface developed with Angular.

The database used by the project is Microsoft SQL Server.

The application follows a layered architecture:

- Presentation layer: Angular displays pages, forms, tables, dashboards, charts, and AI results.
- Controller layer: Spring Boot exposes REST endpoints and receives requests from the frontend.
- Service layer: contains business logic such as stock rules, invoices, permissions, notifications, SAV workflow, and AI processing.
- Repository layer: communicates with SQL Server using Spring Data JPA.
- Security layer: manages JWT authentication, roles, permissions, and protected access.

## Technologies Used

### Backend

- Java 17
- Spring Boot 3
- Spring Security
- Spring Data JPA
- SQL Server
- JWT authentication
- Lombok
- Springdoc OpenAPI
- SMILE for some machine learning features

### Frontend

- Angular 17
- TypeScript
- Angular Material
- Bootstrap
- PrimeNG
- ApexCharts
- SweetAlert2
- jsPDF / pdf-lib
- ngx-translate for multilingual support

## Backend Structure

The backend is located in the `backend` directory.

Important folders:

- `controllers`: REST controllers. Each controller receives HTTP requests and calls the matching service.
- `service`: business logic of the application.
- `service/ml`: machine learning and forecasting logic.
- `repository`: Spring Data JPA interfaces used to access the database.
- `entity`: persisted business entities.
- `dto`: data transfer objects exchanged between the frontend and backend.
- `security`: security configuration, JWT handling, and access rules.
- `config`: technical configuration classes.
- `exception`: application error handling.

Important controllers:

- `AuthController`: login, authentication, and token management.
- `UserController`: user management.
- `EntrepriseController`: company management.
- `ClientController`: customer management.
- `FournisseurController`: supplier management.
- `ArticleAchatController` and `ArticleVenteController`: product management.
- `CommandeAchatController` and `CommandeVenteController`: purchase and sales order management.
- `FactureController` and `FactureDeVenteController`: invoice management.
- `StockController` and `StockMouvementController`: stock and stock movements.
- `NotificationController`: notifications.
- `AuditLogController`: audit log history.
- `AnalyticsController`: analytics and AI data.
- `SavTicketController`: after-sales service ticket workflow.
- `FinancialController` and `ReturnController`: payments, deadlines, returns, credit notes, and account statements.

## Frontend Structure

The frontend is located in the `frontend` directory.

Important folders:

- `auth`: authentication pages and logic.
- `core`: services, guards, interceptors, and shared technical logic.
- `core-component`: main application pages.
- `common-component`: reusable layout components such as header and sidebars.
- `model`: TypeScript models used by the interface.
- `shared`: shared modules, utilities, notifications, and reusable helpers.
- `assets/i18n`: translation files used by the multilingual system.

The frontend communicates with the backend through Angular services. The browser sends the secure `HttpOnly` authentication cookie with API requests so the backend can identify the connected user and verify permissions without exposing the access token to JavaScript.

## Global Workflow

The application works as follows:

1. The user logs in from the Angular interface.
2. The backend validates the credentials.
3. If the credentials are correct, the backend generates a JWT token and places it in an `HttpOnly` cookie.
4. The browser includes this cookie automatically in the next API requests.
5. For each request, Spring Boot checks the token, role, permissions, and user company.
6. The service layer executes the required business operation.
7. Data is read from or saved into SQL Server.
8. The result is returned to the frontend and displayed to the user.

This workflow secures module access and ensures that each company can only access its own data.

## Roles and Permissions

SmartERP mainly uses three types of users:

- Super Admin: manages companies and assigns administrators to companies.
- Administrator: manages users inside their company and configures permissions.
- Employee: accesses only the modules authorized by the administrator.

Permissions control access to modules such as customers, suppliers, products, orders, invoices, stock, after-sales service, and other business features.

## Business Management

The business part covers:

- Customers
- Suppliers
- Categories
- Purchase products
- Sales products
- Purchase orders
- Sales orders
- Purchase invoices
- Sales invoices

When an order or invoice is created, the backend validates the data, calculates the required amounts, applies the business rules, and saves the operation in the database.

## Stock and Stock Movements

The stock module tracks the available quantity of products.

Each stock record is linked to a product and a company. The system can display:

- Incoming quantity
- Available quantity
- Reserved quantity
- Sold quantity
- Minimum threshold
- Stock status

Stock movements explain why quantities changed. For example, a purchase operation can increase stock, while a sales operation can decrease stock depending on the configured business rule.

The system can also generate a notification when a product reaches a low-stock threshold.

## Finance, Payments, and Returns

The finance module completes the invoice lifecycle after an invoice is issued. It provides:

- Partial and full customer or supplier payments.
- Outstanding and overdue balance tracking.
- Configurable payment deadlines.
- Customer and supplier account statements with CSV export.
- Customer and supplier returns linked to the original invoice lines.
- Credit notes and automatic invoice balance recalculation.
- Automatic stock corrections and traceable stock movements after return validation.
- A daily overdue-invoice scan that creates deduplicated notifications for every active company.

Payment, return, invoice, and stock rows are locked during sensitive updates. This prevents concurrent requests from validating the same return twice, creating an overpayment, or applying conflicting stock corrections.

## Notifications and Audit Logs

Notifications inform users about important events such as low stock, order changes, new products, or other useful alerts.

Audit logs keep a history of sensitive actions. They show who performed an action, when it happened, in which company, and on which module. This improves traceability and security.

## After-Sales Service Module

The SAV module manages customer support and after-sales operations.

It can be used to:

- Create support, repair, return, warranty, or maintenance tickets.
- Link tickets to customers and sold products.
- Track ticket priority, status, due date, estimated cost, and resolution.
- Add interventions and ticket history.
- Follow a realistic workflow from open ticket to final resolution.

This module makes the ERP more complete because it covers the customer relationship after the sale, not only the sale itself.

## AI Intelligence Module

The AI module uses sales and stock data to produce decision-support indicators.

It can:

- Analyze sales.
- Calculate total revenue.
- Calculate monthly averages.
- Identify the best month.
- Calculate growth rate.
- Generate sales forecasts.
- Classify products by performance.
- Evaluate suppliers.
- Generate purchase recommendations.

The backend retrieves the required data from SQL Server, prepares it, then applies statistical or machine learning processing depending on the amount and quality of available data.

Classes inside `service/ml` participate in this logic:

- `SeriesCleaner`: cleans time-series data.
- `BoxCox`: transforms values to stabilize variation.
- `LoessSmoother`: smooths noisy trends.
- `FeatureScaler`: normalizes features.
- `CalendarFeatureService`: adds calendar-based features.
- `MlForecastEngine`: manages sales forecasting.
- `MlRecommendationEngine`: produces recommendation data.
- `SupplierRatingEngine`: evaluates supplier performance.

If there is enough historical data, the system can use a machine learning approach. If the data is limited, it falls back to simpler statistical methods to still produce a usable estimate.

## Configuration

The backend reads configuration from environment variables. The tracked defaults are suitable only for local development and contain no private email or production credentials.

| Variable | Purpose | Local default |
| --- | --- | --- |
| `DB_URL` | SQL Server JDBC URL | `jdbc:sqlserver://localhost:1433;databaseName=BusinessManager;...` |
| `DB_USERNAME` | SQL Server user | `sa` |
| `DB_PASSWORD` | SQL Server password | `1234` |
| `JWT_SECRET` | JWT signing key, at least 32 characters | Development-only key |
| `FRONTEND_URL` | Allowed frontend origin and reset-password URL | `http://localhost:4200` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated frontend origins allowed by the API | `http://localhost:4200` |
| `BOOTSTRAP_ADMIN_USERNAME` | Initial super-admin username for a new database | `admin` |
| `BOOTSTRAP_ADMIN_EMAIL` | Initial super-admin email for a new database | `admin@gmail.com` |
| `BOOTSTRAP_ADMIN_PASSWORD` | Initial super-admin password; change it outside local development | `admin123` |
| `COOKIE_SECURE` | Sends authentication cookies over HTTPS only | `false` |
| `LOGIN_MAX_ATTEMPTS` | Failed login attempts allowed before temporary lockout | `5` |
| `LOGIN_LOCK_DURATION_MINUTES` | Temporary login lockout duration | `15` |
| `FINANCE_OVERDUE_SCAN_ENABLED` | Enables the daily overdue-payment scan | `true` |
| `FINANCE_OVERDUE_SCAN_CRON` | Cron schedule for overdue-payment notifications | `0 15 6 * * *` |
| `FINANCE_OVERDUE_SCAN_ZONE` | Time zone used by the finance scheduler | `Africa/Tunis` |
| `API_URL` | Backend URL loaded by the containerized frontend at runtime | `http://localhost:8020` |
| `MAIL_USERNAME` | SMTP account used for password-reset emails | Empty |
| `MAIL_PASSWORD` | SMTP app password | Empty |

The complete list is available in `backend/.env.example`. Spring Boot does not load `.env` files automatically; provide the values through your shell, IDE run configuration, or hosting platform. For example, in PowerShell:

```powershell
$env:DB_PASSWORD = "your-sql-server-password"
$env:JWT_SECRET = "replace-this-with-a-long-random-secret"
$env:MAIL_USERNAME = "your-email@example.com"
$env:MAIL_PASSWORD = "your-smtp-app-password"
```

For production, also activate the hardened profile with `SPRING_PROFILES_ACTIVE=prod`. It validates the database schema, requires HTTPS cookies, hides internal error details, and disables public Swagger endpoints.

Never commit real passwords, JWT secrets, SMTP app passwords, access tokens, or `.env` files.

## Running the Backend

Prerequisites:

- Java 17 installed.
- SQL Server running.
- Database created.

Commands on Windows:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Commands on macOS or Linux:

```bash
cd backend
./mvnw spring-boot:run
```

By default, the backend starts at:

```text
http://localhost:8020
```

If Springdoc is enabled, API documentation can be opened at:

```text
http://localhost:8020/swagger-ui/index.html
```

The backend health endpoint is available at:

```text
http://localhost:8020/actuator/health
```

Only health information is publicly accessible. Other application endpoints remain protected by JWT authentication.

## Running with Docker

The Docker setup starts Microsoft SQL Server, creates the `BusinessManager` database, starts a local MailDev inbox, builds the Spring Boot API, and serves the Angular application with Nginx.

1. Create your local environment file and replace the example database and JWT secrets:

```powershell
cd backend
Copy-Item .env.example .env
```

2. Start the complete stack:

```powershell
docker compose up --build -d
```

3. Open the services:

| Service | URL |
| --- | --- |
| SmartERP | `http://localhost:4200` |
| Backend health | `http://localhost:8020/actuator/health` |
| Swagger, local profile only | `http://localhost:8020/swagger-ui/index.html` |
| MailDev inbox | `http://localhost:1080` |

On a newly created Docker database, sign in with `admin` / `admin123`. Set
`BOOTSTRAP_ADMIN_USERNAME`, `BOOTSTRAP_ADMIN_EMAIL`, and
`BOOTSTRAP_ADMIN_PASSWORD` in `backend/.env` before the first startup to use
different bootstrap credentials. Existing accounts from a SQL Server installed
directly on Windows are not copied into the Docker database automatically.

Load or refresh the complete demonstration enterprise with:

```powershell
docker compose --profile demo run --rm demo-seed
```

The demonstration logins are `admin.soutenance` / `1234`, `vente.demo` /
`demo123`, `stock.demo` / `demo123`, `admin.technova` / `demo123`, and
`admin.atlas` / `demo123`. The seed includes three companies; the main company
contains customers, suppliers, twenty products with stock, purchase and sales
orders, invoices, and twelve months of activity. Rerunning the command replaces
only the demonstration company data.

View container status and logs with:

```powershell
docker compose ps
docker compose logs -f backend
```

Stop the stack without deleting database data:

```powershell
docker compose down
```

The frontend image reads `API_URL` when its container starts, so the same image can target a different backend without rebuilding Angular. For deployment, configure `API_URL`, `FRONTEND_URL`, `CORS_ALLOWED_ORIGINS`, `COOKIE_SECURE=true`, a strong `JWT_SECRET`, and production database credentials through the hosting platform.

## Publishing and Deploying from GitHub

GitHub stores the source code and publishes the application images, but GitHub
Pages cannot run the Spring Boot API or SQL Server. Deploy the published images
on a Docker-capable server or cloud container platform.

1. Push the repository to GitHub and confirm that **SmartERP CI** passes.
2. Create and push a version tag such as `v1.0.0`.
3. The **Publish SmartERP Images** workflow builds the backend and frontend and
   publishes them under `ghcr.io/<owner>/<repository>/backend` and
   `ghcr.io/<owner>/<repository>/frontend`.
4. In each package's GitHub settings, either make the package public or connect
   Docker on the deployment host with a fine-grained token that has read access:

   ```bash
   echo "$GHCR_TOKEN" | docker login ghcr.io -u <github-user> --password-stdin
   ```

5. On the deployment host, copy `backend/.env.production.example` to an
   untracked `.env.production` file and replace every example value.
6. For a new empty database, keep `JPA_DDL_AUTO=update` for the first successful
   startup. Then change it to `validate` so later deployments cannot modify the
   schema implicitly.
7. Start the production stack behind an HTTPS reverse proxy:

```bash
cd backend
docker compose --env-file .env.production -f docker-compose.production.yaml up -d
```

The production Compose file keeps SQL Server on an internal Docker network and
binds the web ports to `127.0.0.1`. Configure a reverse proxy such as Nginx,
Caddy, or the hosting platform's ingress to expose the frontend and API over
HTTPS. The local `docker-compose.yaml`, MailDev service, demo seed, and default
passwords are intended only for development.

## Automated Checks

GitHub Actions runs the backend tests, verifies that the English and French translation files contain the same keys, and builds the Angular production application on every pull request and push to `main` or `master`. Dependabot checks npm, Maven, Docker, and GitHub Actions dependencies on a regular schedule and opens focused update pull requests.

Run the same checks locally before pushing:

```powershell
cd backend
.\mvnw.cmd test

cd ..\frontend
npm ci
npm run check:i18n
npm run build
```

## Running the Frontend

Prerequisites:

- Node.js 20 LTS installed. Newer unsupported majors such as Node 24 may not
  work with Angular 17's build tools.
- npm 10 installed.

Commands:

```bash
cd frontend
npm ci
npm start
```

By default, the Angular application starts at:

```text
http://localhost:4200
```

## Database

The project uses SQL Server.

The file `scriptYoussef.sql` can be used to prepare the database when needed. Depending on the project configuration, Hibernate can also update the schema automatically.

The database used in local configuration is usually:

```text
BusinessManager
```

## Build

To build the backend:

```bash
cd backend
mvn clean package
```

To build the frontend:

```bash
cd frontend
npm run build
```

## Suggested Demonstration Scenario

A clear project demonstration can follow this flow:

1. Log in to the platform.
2. Present the roles: Super Admin, Administrator, and Employee.
3. Create or view a company.
4. Create a user and assign permissions.
5. Add customers, suppliers, and products.
6. Create a purchase order.
7. Create a sales order.
8. View invoices.
9. View stock and stock movements.
10. Trigger or view a notification.
11. View the audit log.
12. Create or follow an SAV ticket.
13. Open the AI dashboard.
14. Present forecasts and purchase recommendations.

This scenario shows the complete cycle: business management, security, traceability, stock tracking, after-sales service, and decision support.

## Important Points to Explain

- The Angular frontend is separated from the Spring Boot backend.
- The backend exposes REST APIs secured with JWT.
- Permissions control access to modules and actions.
- Data is isolated by company.
- Stock is updated according to business operations.
- Stock movements explain the origin of stock changes.
- Notifications alert users about important events.
- Audit logs keep the history of sensitive actions.
- The SAV module manages customer issues after sales.
- The AI module uses real ERP data to generate analytics, forecasts, and recommendations.

## Author

Project created as part of a Final Year Project.

Project name: SmartERP - Intelligent ERP
Type: ERP web application with artificial intelligence
Stack: Angular, Spring Boot, SQL Server, JWT, SMILE
