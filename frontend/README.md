# CivicGuide — Secure MVP

CivicGuide is a citizen guidance platform for government services such as Aadhaar, passport, driving licence and certificates.

## Roles

- **USER**: creates and tracks service requests.
- **MENTOR**: reviews available requests and guides citizens. Mentor accounts require admin approval before accepting requests.
- **ADMIN**: reviews mentor applications and monitors platform activity. Admin accounts are seeded internally; public registration cannot create an ADMIN.

## Authentication and authorization

- JWT is signed by the Spring Boot backend.
- JWT is stored in an **HttpOnly cookie**; Angular never reads the token.
- JWT contains the user's role and user ID.
- `JwtAuthFilter` converts the signed role claim into a Spring Security `ROLE_*` authority.
- Angular route guards call `/api/user/me` to obtain the current authenticated user's role.
- Backend authorization is independent of Angular and is the real security boundary.
- Method-level `@PreAuthorize` checks protect service-request operations.
- No role is stored in `localStorage`.

## Dashboard flows

### Citizen

1. Register as Citizen.
2. Log in.
3. Open Citizen Dashboard.
4. Select a government service and create a request.
5. Track request status and assigned mentor.

### Mentor

1. Register as Mentor and submit qualification/profile information.
2. Wait for ADMIN approval.
3. Log in to Mentor Dashboard.
4. Review pending citizen requests.
5. Accept a request and move it through `ACCEPTED -> IN_PROGRESS -> COMPLETED`.

### Admin

Set environment variables before starting Spring Boot:

```text
CIVICGUIDE_ADMIN_EMAIL=admin@example.com
CIVICGUIDE_ADMIN_PASSWORD=use-a-strong-password
```

On first startup, the application creates the ADMIN account if the email does not already exist.

## Run locally

### Backend

1. Start PostgreSQL and create database `civicguide`.
2. Check `backend/src/main/resources/application.properties`.
3. Start Spring Boot from the `backend` folder:

```bash
./mvnw spring-boot:run
```

### Frontend

From `frontend`:

```bash
npm install
npm start
```

Angular runs on `http://localhost:4200` and Spring Boot on `http://localhost:8080`.

## Important production hardening

Before deployment:

- move the database password and JWT secret to environment/secret management;
- enable HTTPS and set the JWT cookie `Secure` flag;
- add a deliberate CSRF strategy for cookie-based authentication;
- restrict CORS to the production frontend origin;
- use a strong, rotated JWT signing key;
- add audit logging for admin and mentor actions.
