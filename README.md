# Dominios Solunet

Application for managing domain renewal notifications and processing client responses.

The system automatically detects domains approaching their expiration date, groups them by client and sends a renewal notification containing a secure confirmation link.

## Features

* Automatic detection of domains approaching expiration.
* Configurable renewal notification thresholds.
* Scheduled renewal processing with Spring Scheduler.
* Domain grouping by client.
* Secure, expiring and single-use confirmation tokens.
* Email notifications for clients.
* Web-based renewal confirmation using Thymeleaf.
* Selection of individual domains to renew.
* Ability to reject all domains.
* Client/domain ownership validation.
* Domain status updates after confirmation.
* Administrative error reporting by email.
* Development and production configuration profiles.

## Technology Stack

* Java 21
* Spring Boot 3.5
* Spring Data JPA
* Hibernate
* Thymeleaf
* Spring Mail
* H2 for development
* JUnit 5
* Mockito
* Maven

## Application Flow

```text
Scheduled task
      │
      ▼
RenovacionScheduler
      │
      ▼
RenovacionService
      │
      ├── Find domains approaching expiration
      ├── Group domains by client
      ├── Generate confirmation token
      ├── Build confirmation URL
      └── Send renewal email
                    │
                    ▼
          Client confirmation
                    │
                    ▼
          RenovacionController
                    │
                    ├── Validate token
                    ├── Validate domains
                    └── Process confirmation
```

## Renewal Process

1. The scheduler executes the renewal process according to the configured cron expression.
2. Domains approaching their expiration date are retrieved.
3. Domains are grouped by client.
4. A confirmation token is generated for each client.
5. The client receives an email containing a confirmation link.
6. The client accesses the renewal form.
7. The token is validated.
8. The client selects the domains to renew or rejects all domains.
9. The server validates that the selected domains belong to the token's client.
10. The token is marked as used.
11. The selected domains are processed and their status is updated.

## Configuration

The application uses Spring Boot profiles:

```text
application.properties
application-dev.properties
application-prod.properties
```

### Development

Activate the development profile with:

```text
SPRING_PROFILES_ACTIVE=dev
```

The development environment uses:

* H2 in-memory database
* H2 console
* Mailpit for local email testing
* Local renewal URL

H2 console:

```text
http://localhost:8080/h2-console
```

Mailpit:

```text
http://localhost:8025
```

### Production

Activate the production profile with:

```text
SPRING_PROFILES_ACTIVE=prod
```

Production database and mail configuration is provided through environment variables.

The main configuration variables include:

```text
DB_URL
DB_USERNAME
DB_PASSWORD

MAIL_HOST
MAIL_PORT
MAIL_USERNAME
MAIL_PASSWORD

APP_URL_DOMINIO
APP_EMAIL_ADMIN

RENOVACION_SCHEDULER_CRON
```

Sensitive credentials should not be committed to the repository.

## Scheduler

The renewal process is executed using Spring Scheduler.

The default schedule is:

```text
0 0 9 * * *
```

The schedule can be overridden using:

```text
RENOVACION_SCHEDULER_CRON
```

For local testing, a more frequent cron expression can be used.

## Testing

The project contains unit and integration tests covering the main application components.

Run the complete test suite with:

```bash
mvn clean test
```

The test suite covers, among other cases:

* Renewal processing.
* Renewal thresholds.
* Expired domains.
* Previously notified domains.
* Token generation and validation.
* Used and expired tokens.
* Email functionality.
* Controller behaviour.
* Confirmation processing.
* Scheduler execution.
* Client/domain isolation.
* End-to-end renewal workflow.

## Project Structure

```text
src/
├── main/
│   ├── java/com/dominiossolunet/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── scheduler/
│   │   ├── service/
│   │   └── utils/
│   │
│   └── resources/
│       ├── templates/
│       ├── application.properties
│       ├── application-dev.properties
│       └── application-prod.properties
│
└── test/
    └── java/com/dominiossolunet/
        ├── controller/
        ├── service/
        ├── scheduler/
        └── utils/
```

## Version

Current release:

**1.0.0**

See [CHANGELOG.md](CHANGELOG.md) for the release history.

## License

This project is intended as an educational and development project.
