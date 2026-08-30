# Security Policy

## Supported Version

Security fixes are applied to the latest version on the `main` branch.

## Reporting a Vulnerability

Do not open a public issue for a suspected vulnerability or exposed credential.
Use the repository's **Security** tab and select **Report a vulnerability** to
create a private security advisory for the maintainers.

Include the affected endpoint or component, reproduction steps, expected impact,
and any suggested mitigation. Do not include real customer data, production
credentials, or access tokens in the report.

## Deployment Requirements

- Replace every example database, administrator, SMTP, and JWT credential.
- Run the backend with `SPRING_PROFILES_ACTIVE=prod`.
- Serve the frontend and backend through HTTPS and keep `COOKIE_SECURE=true`.
- Do not expose SQL Server directly to the public internet.
- Restrict `CORS_ALLOWED_ORIGINS` to the deployed frontend origin.
- Keep `.env`, backups, database files, and generated documents outside Git.
