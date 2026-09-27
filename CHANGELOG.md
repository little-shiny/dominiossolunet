# Changelog

All notable changes to this project are documented in this file.

## [Unreleased]

### Added

* Added a management dashboard for monitoring the domain renewal process.
* Added client overview and client detail pages.
* Added domain detail views.
* Added dashboard metrics for the current domain and renewal status.
* Added `ClienteResumen` to provide summarized client information for the management interface.
* Added `DominioGestionDto` for transferring domain management data to the web interface.
* Added filtering capabilities for domain management.
* Added management views using Thymeleaf.
* Added test coverage for the new dashboard, client management and domain management functionality.
* Added development data in `data.sql` for testing the dashboard and management views.

### Changed

* Extended `GestionDominioController` to support dashboard and domain management views.
* Extended `GestionDominioService` with the operations required by the management dashboard.
* Added `ClienteController` to handle client listing and client detail views.
* Added `ClienteService` to provide client-related management operations.
* Updated the domain management templates to support the new dashboard workflow.
* Added client and domain navigation between overview and detail pages.

### Tests

* Added tests for the dashboard metrics service.
* Added tests for client management functionality.
* Added tests for client detail and domain detail functionality.
* Updated `GestionDominioService` tests for the new management operations.
* Updated controller tests for the dashboard and domain management views.

## [1.1.0] - 2026-09-27

### Added

* Added `HistorialDominio` to record relevant events throughout the lifecycle of a domain.
* Added `HistorialDominioRepository` to query the history of each domain ordered by date.
* Added the `AVISO_RENOVACION_ENVIADO` event to the automated renewal notification process.
* Added history events for:

  * `CLIENTE_ACEPTA_RENOVACION`
  * `CLIENTE_RECHAZA_RENOVACION`
  * `RENOVACION_REALIZADA`
  * `FACTURACION_REALIZADA`
* Added individual history tracking for each domain included in a renewal notification.
* Added support for recording the effective renewal of a domain independently from the client's response.
* Added separation between domain renewal and billing, allowing both operations to be managed independently.
* Added tests to verify that successfully sent renewal notifications create the corresponding history entries.
* Added tests to verify that email delivery failures do not create an `AVISO_RENOVACION_ENVIADO` event.

### Changed

* Updated `RenovacionService` to save a `HistorialDominio` entry when a renewal email is successfully sent.
* The `AVISO_RENOVACION_ENVIADO` event is now recorded only after successful email delivery has been confirmed.
* Domains continue to be marked as `AVISO_ENVIADO` only when the email is successfully sent.
* Updated `TokenService.procesarConfirmacion()` to record the client's response in the domain history.
* Client responses are recorded as:

  * `CLIENTE_ACEPTA_RENOVACION`
  * `CLIENTE_RECHAZA_RENOVACION`
* Domain renewals performed by the manager are recorded through the `RENOVACION_REALIZADA` event.
* The operational state of a domain remains independent from the client's renewal response.
* Accepting a renewal no longer directly changes the domain's operational state to `ACTIVO`.
* Updated `GestionDominioService` and related domain management logic to work with the new renewal state model.
* Refactored `ErrorEnvioEmail` and `ResultadoEnvioEmail` into records.

### Tests

* Expanded `RenovacionService` tests to verify domain history creation.
* Verified that one history event is created for each domain, even when multiple domains belong to the same client.
* Verified that no `AVISO_RENOVACION_ENVIADO` history entry is created when an email cannot be sent.
* Expanded `TokenService` tests to cover:

  * all domains being accepted;
  * some domains being accepted;
  * no domains being accepted;
  * token being marked as used after confirmation processing;
  * history creation for each client response.
* Added and updated integration tests covering domain history and the new renewal state model.
* Corrected `TokenService` tests to assign real IDs to domains instead of using `0` for all test domains.
* Updated tests to avoid expecting `tokenClienteRepository.save()` in `marcarComoUsado()`, as the update is handled through JPA dirty checking.
* Cleaned up duplicated and outdated tests after the service implementation changes.

### Refactoring

* Separated the responsibilities of:

  * client response;
  * actual domain renewal;
  * billing;
  * domain event history.
* Cleaned up and reorganized `TokenService` tests.
* Removed duplicated tests for `procesarConfirmacion()`.
* Refactored affected services and repositories to support the new domain state and history model.

## [1.0.0] - 2026-09-25

### Added

* Automated domain renewal notification system.
* Scheduled execution of the renewal process using Spring Scheduler.
* Configurable renewal notification thresholds.
* Grouping of domains by client when generating renewal notifications.
* Secure token generation for client renewal confirmations.
* Token expiration and single-use validation.
* Renewal confirmation web interface using Thymeleaf.
* Individual domain selection for renewal confirmation.
* Ability to reject all domains from the confirmation form.
* Protection against confirming domains belonging to another client.
* Domain status updates after processing renewal confirmations.
* Email notifications for upcoming domain expirations.
* Administrative email report for renewal processing errors.
* Configurable renewal URLs.
* Separate development and production configuration profiles.
* Local development environment using H2 and Mailpit.
* Integration tests covering the main renewal workflow.
* Unit tests for services, controllers, URL building and scheduler execution.
* Integration tests for token and email functionality.
* Manual end-to-end validation of the renewal workflow.

### Configuration

* Renewal scheduler cron expression can be configured through `RENOVACION_SCHEDULER_CRON`.
* Development configuration uses H2 and Mailpit.
* Production configuration supports external database and mail server settings through environment variables.
* Renewal application URL can be configured independently for development and production.

### Security

* Renewal tokens are validated for existence, expiration and previous use.
* Token ownership is checked when processing selected domains.
* Domains belonging to another client cannot be processed through a manipulated request.
* Used tokens cannot be reused.
