# Changelog

All notable changes to this project are documented in this file.

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
