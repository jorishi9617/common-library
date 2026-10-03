# JWT Token Service

Standalone Spring Boot service for issuing and validating signed JWTs. The
application targets Java 25 and requires a shared API key for all endpoints
except the health check.

## Configuration

Set these environment variables before starting the service:

| Variable | Description |
| --- | --- |
| `API_KEY` | Shared secret sent in the `X-API-Key` header |
| `JWT_SECRET` | Base64-encoded signing secret that decodes to at least 32 bytes |
| `JWT_EXPIRATION_SECONDS` | Token lifetime in seconds (default: `900`) |
| `PORT` | HTTP port (default: `8080`) |

## Run

With Java 25 and Maven installed:

```sh
mvn spring-boot:run
```

The service can also be packaged as an executable JAR with `mvn package`.

## API

All API requests require the `X-API-Key` header. The health check at
`GET /actuator/health` is public.

Issue a token with `POST /api/tokens`:

```json
{
  "userId": "5d424701-0c6c-48e3-a5ce-b88d89797c24",
  "email": "user@example.com"
}
```

The response contains the signed token and its lifetime. Validate a token with
`POST /api/tokens/validate` and a body of `{"token":"<signed-token>"}`. The
response reports whether it is valid and, if so, its user ID and email.
