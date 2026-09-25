# Changelog

All notable changes to this project are documented in this file.

## [Unreleased] - 2026-09-25

### Added
- Añadido `HistorialDominio` para registrar los eventos relevantes del ciclo de vida de un dominio.
- Añadido `HistorialDominioRepository` para consultar el historial de cada dominio ordenado por fecha.
- Añadido el evento `AVISO_RENOVACION_ENVIADO` al proceso automático de avisos.
- Añadido el registro de eventos de:
    - `CLIENTE_ACEPTA_RENOVACION`
    - `CLIENTE_RECHAZA_RENOVACION`
    - `RENOVACION_REALIZADA`
    - `FACTURACION_REALIZADA`
- Añadido registro histórico individual para cada dominio incluido en un aviso de renovación.
- Añadidos tests para comprobar que los avisos enviados correctamente generan su correspondiente registro en el historial.
- Añadidos tests para comprobar que un error en el envío del correo no genera un evento de aviso enviado.

### Changed
- Modificado `RenovacionService` para guardar un `HistorialDominio` cuando el email de renovación se envía correctamente.
- El registro `AVISO_RENOVACION_ENVIADO` se realiza únicamente después de confirmar el envío correcto del correo.
- Manteniendo la lógica existente, los dominios se marcan como `AVISO_ENVIADO` únicamente cuando el envío ha sido satisfactorio.
- `TokenService.procesarConfirmacion()` registra ahora el resultado de la respuesta del cliente en el historial.
- Las respuestas del cliente se registran como:
    - `CLIENTE_ACEPTA_RENOVACION`
    - `CLIENTE_RECHAZA_RENOVACION`
- La renovación realizada por el gestor se registra mediante `RENOVACION_REALIZADA`.
- Se mantiene separado el estado operativo del dominio de la respuesta del cliente: aceptar una renovación no marca directamente el dominio como `ACTIVO`.

### Tests
- Ampliados los tests de `RenovacionService` para comprobar la creación del historial.
- Comprobado que se genera un evento histórico por cada dominio, incluso cuando varios dominios pertenecen al mismo cliente.
- Comprobado que no se genera historial cuando el email no se puede enviar.
- Ampliados los tests de `TokenService` para cubrir:
    - todos los dominios confirmados;
    - algunos dominios confirmados;
    - ningún dominio confirmado;
    - token usado después de procesar la confirmación;
    - creación del historial correspondiente a cada respuesta.
- Corregidos los tests de `TokenService` para asignar IDs reales a los dominios y evitar que todos utilizaran el ID `0`.
- Corregidos los tests para no esperar un `tokenClienteRepository.save()` en `marcarComoUsado()`, ya que la actualización se realiza mediante dirty checking de JPA.

### Refactoring
- Limpiados y reorganizados los tests de `TokenService`.
- Eliminada duplicidad en los tests de `procesarConfirmacion()`.
- Separadas las responsabilidades entre:
    - respuesta del cliente;
    - renovación real del dominio;
    - facturación;
    - historial de eventos.

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
