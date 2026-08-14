# SmartERP - Intelligent ERP

SmartERP is a web-based ERP application built as a final-year project. It helps small and medium-sized businesses manage their daily operations while using business data to support better decision-making.

The project includes user management, roles and permissions, customers, suppliers, purchase products, sales products, purchase orders, sales orders, invoices, stock management, notifications, audit logs, after-sales service, and an AI intelligence module for sales analysis, forecasting, and purchase recommendations.

## Project Objective

The main goal of SmartERP is to provide a modern, secure, and intelligent ERP platform. The application is not limited to storing business operations. It also tracks stock activity, keeps a history of sensitive actions, and analyzes business data to help managers anticipate future activity.

SmartERP allows users to:

- Manage several companies from the same platform.
- Create administrators and employees with precise permissions.
- Manage customers, suppliers, purchase products, and sales products.
- Create purchase orders and sales orders.
- Generate purchase and sales invoices.
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

The frontend communicates with the backend through Angular services. Requests include the JWT token so the backend can identify the connected user and verify permissions.

## Global Workflow

The application works as follows:

1. The user logs in from the Angular interface.
2. The backend validates the credentials.
3. If the credentials are correct, the backend generates a JWT token.
4. The frontend stores and uses this token for the next requests.
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

The main backend configuration file is:

```text
backend/src/main/resources/application.properties
```

Before running the project, configure:

- SQL Server URL.
- Database name.
- SQL Server username and password.
- Mail settings if email sending is enabled.
- JWT secret.
- Frontend URL.

Important: values stored in this file are intended for local development or demonstration. For a real production environment, passwords and secrets should be replaced with secure environment variables.

## Running the Backend

Prerequisites:

- Java 17 installed.
- Maven installed.
- SQL Server running.
- Database created.

Commands:

```bash
cd backend
mvn spring-boot:run
```

By default, the backend starts at:

```text
http://localhost:8020
```

If Springdoc is enabled, API documentation can be opened at:

```text
http://localhost:8020/swagger-ui/index.html
```

## Running the Frontend

Prerequisites:

- Node.js installed.
- npm installed.

Commands:

```bash
cd frontend
npm install
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
