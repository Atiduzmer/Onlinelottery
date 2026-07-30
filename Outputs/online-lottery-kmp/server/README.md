# Online Lottery Server

Kotlin/Spring Boot backend for the Android client. PostgreSQL schema changes are managed by Flyway.

```powershell
docker compose up -d postgres redis
.\gradlew.bat :server:bootRun
```

Available baseline endpoints:

- `GET http://localhost:8080/actuator/health`
- `GET http://localhost:8080/api/v1/public/lottery-games`
- `GET http://localhost:8080/api/v1/public/modules`

Production deployments must replace all development passwords, terminate TLS at the gateway, and provide encryption keys through a managed secret store.

For a zero-install local API session, run:

```powershell
.\gradlew.bat :server:runLocalServer
```

This development task starts an embedded PostgreSQL instance, applies all Flyway migrations, and then listens on port `8080`. Its database is temporary and is not packaged into the production JAR.
