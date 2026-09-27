-- ============================================================
-- DATOS DE PRUEBA - dominiossolunet
-- ============================================================
-- Todos los datos son ficticios y están pensados para H2 local.
--
-- Casos incluidos:
--   1. Dominio con cliente CONFIRMADO -> se puede marcar como RENOVADO
--   2. Dominio RENOVADO -> pendiente de facturar
--   3. Dominio RENOVADO + FACTURADO
--   4. Dominio RECHAZADO
--   5. Dominio PENDIENTE de respuesta
--   6. Dominio EXPIRADO
--
-- IMPORTANTE:
-- Registrador NO usa @Enumerated(EnumType.STRING) en Dominio,
-- por lo que Hibernate lo almacena como ordinal:
--   DOMITECA = 0
--   GANDI = 1
--   NOMINALIA = 2
--   OTRO = 3
-- El resto de enums usados aquí están almacenados como STRING.
-- ============================================================


-- ------------------------------------------------------------
-- CLIENTES
-- ------------------------------------------------------------

INSERT INTO cliente (id, nombre, email)
VALUES (1, 'Empresa Demo S.L.', 'demo@empresa.test');

INSERT INTO cliente (id, nombre, email)
VALUES (2, 'Estudio Ficticio', 'contacto@estudio.test');

INSERT INTO cliente (id, nombre, email)
VALUES (3, 'Tienda Ejemplo', 'hola@tienda.test');


-- ------------------------------------------------------------
-- DOMINIOS
-- ------------------------------------------------------------

-- 1. CLIENTE CONFIRMÓ -> se puede marcar como RENOVADO
INSERT INTO dominio
    (id, estado, estado_renovacion, fecha_expiracion,
     id_cliente, nombre_dominio, registrador, ultimo_aviso,
     ultimo_umbral_avisado)
VALUES
    (1, 'AVISO_ENVIADO', 'PENDIENTE_RENOVACION',
     '2026-10-02', 1, 'demo-renovable.es', 0,
     '2026-09-27', 5);

-- 2. RENOVADO -> todavía NO facturado
INSERT INTO dominio
    (id, estado, estado_renovacion, fecha_expiracion,
     id_cliente, nombre_dominio, registrador, ultimo_aviso,
     ultimo_umbral_avisado)
VALUES
    (2, 'ACTIVO', 'RENOVADO',
     '2026-11-15', 1, 'demo-renovado.com', 1,
     '2026-09-10', 30);

-- 3. RENOVADO + FACTURADO
INSERT INTO dominio
    (id, estado, estado_renovacion, fecha_expiracion,
     id_cliente, nombre_dominio, registrador, ultimo_aviso,
     ultimo_umbral_avisado)
VALUES
    (3, 'ACTIVO', 'RENOVADO',
     '2027-01-20', 2, 'estudio-facturado.es', 2,
     '2026-09-20', 30);

-- 4. CLIENTE RECHAZÓ LA RENOVACIÓN
INSERT INTO dominio
    (id, estado, estado_renovacion, fecha_expiracion,
     id_cliente, nombre_dominio, registrador, ultimo_aviso,
     ultimo_umbral_avisado)
VALUES
    (4, 'AVISO_ENVIADO', 'RECHAZADO',
     '2026-10-20', 2, 'estudio-rechazado.com', 1,
     '2026-09-20', 30);

-- 5. AVISO ENVIADO PERO SIN RESPUESTA
INSERT INTO dominio
    (id, estado, estado_renovacion, fecha_expiracion,
     id_cliente, nombre_dominio, registrador, ultimo_aviso,
     ultimo_umbral_avisado)
VALUES
    (5, 'AVISO_ENVIADO', 'PENDIENTE_RENOVACION',
     '2026-10-27', 3, 'tienda-pendiente.es', 0,
     '2026-09-27', 30);

-- 6. DOMINIO EXPIRADO
INSERT INTO dominio
    (id, estado, estado_renovacion, fecha_expiracion,
     id_cliente, nombre_dominio, registrador, ultimo_aviso,
     ultimo_umbral_avisado)
VALUES
    (6, 'EXPIRADO_SIN_RESPUESTA', 'SIN_RENOVACION',
     '2026-09-20', 3, 'tienda-expirada.com', 3,
     '2026-09-15', 5);


-- ------------------------------------------------------------
-- FACTURACIÓN
-- ------------------------------------------------------------

-- Dominio 1: pendiente de facturar
INSERT INTO facturacion
    (id, estado_facturacion, fecha_ultima_factura, id_dominio, nota)
VALUES
    (1, 'PENDIENTE_FACTURAR', NULL, 1,
     'Factura pendiente después de renovar en el registrador');

-- Dominio 2: pendiente de facturar
INSERT INTO facturacion
    (id, estado_facturacion, fecha_ultima_factura, id_dominio, nota)
VALUES
    (2, 'PENDIENTE_FACTURAR', NULL, 2,
     'Cliente con facturación trimestral');

-- Dominio 3: ya facturado
INSERT INTO facturacion
    (id, estado_facturacion, fecha_ultima_factura, id_dominio, nota)
VALUES
    (3, 'FACTURADO', '2026-09-25', 3,
     'Factura de prueba ya realizada');

-- Dominio 4: tiene registro de facturación pero el cliente rechazó
INSERT INTO facturacion
    (id, estado_facturacion, fecha_ultima_factura, id_dominio, nota)
VALUES
    (4, 'PENDIENTE_FACTURAR', NULL, 4,
     'No facturar hasta revisar el rechazo del cliente');

-- Dominio 5: pendiente de respuesta
INSERT INTO facturacion
    (id, estado_facturacion, fecha_ultima_factura, id_dominio, nota)
VALUES
    (5, 'PENDIENTE_FACTURAR', NULL, 5,
     'Pendiente de respuesta del cliente');


-- ------------------------------------------------------------
-- TOKENS / RESPUESTAS DEL CLIENTE
-- ------------------------------------------------------------

-- Token del dominio 1: CONFIRMADO
INSERT INTO token_cliente
    (id, fecha_creacion, fecha_expiracion, id_cliente, token, usado)
VALUES
    (1, '2026-09-27T09:00:00', '2026-10-04T09:00:00',
     1, 'TOKEN-DEMO-CONFIRMADO-001', TRUE);

INSERT INTO token_dominio
    (id, id_token, id_dominio, estado_aviso_renovacion,
     fecha_interaccion_cliente)
VALUES
    (1, 1, 1, 'CONFIRMADO', '2026-09-27T10:30:00');


-- Token del dominio 4: RECHAZADO
INSERT INTO token_cliente
    (id, fecha_creacion, fecha_expiracion, id_cliente, token, usado)
VALUES
    (2, '2026-09-20T09:00:00', '2026-09-27T09:00:00',
     2, 'TOKEN-DEMO-RECHAZADO-002', TRUE);

INSERT INTO token_dominio
    (id, id_token, id_dominio, estado_aviso_renovacion,
     fecha_interaccion_cliente)
VALUES
    (2, 2, 4, 'RECHAZADO', '2026-09-21T16:45:00');


-- Token del dominio 5: todavía pendiente
INSERT INTO token_cliente
    (id, fecha_creacion, fecha_expiracion, id_cliente, token, usado)
VALUES
    (3, '2026-09-27T09:00:00', '2026-10-04T09:00:00',
     3, 'TOKEN-DEMO-PENDIENTE-003', FALSE);

INSERT INTO token_dominio
    (id, id_token, id_dominio, estado_aviso_renovacion,
     fecha_interaccion_cliente)
VALUES
    (3, 3, 5, 'PENDIENTE', NULL);


-- ------------------------------------------------------------
-- HISTORIAL
-- ------------------------------------------------------------

INSERT INTO historial_dominio
    (id, id_dominio, tipo_evento, fecha, detalle)
VALUES
    (1, 1, 'AVISO_RENOVACION_ENVIADO',
     '2026-09-27T09:00:00',
     'Aviso de renovación enviado al cliente');

INSERT INTO historial_dominio
    (id, id_dominio, tipo_evento, fecha, detalle)
VALUES
    (2, 1, 'CLIENTE_ACEPTA_RENOVACION',
     '2026-09-27T10:30:00',
     'El cliente confirma la renovación');

INSERT INTO historial_dominio
    (id, id_dominio, tipo_evento, fecha, detalle)
VALUES
    (3, 2, 'AVISO_RENOVACION_ENVIADO',
     '2026-09-10T09:00:00',
     'Aviso de renovación enviado al cliente');

INSERT INTO historial_dominio
    (id, id_dominio, tipo_evento, fecha, detalle)
VALUES
    (4, 2, 'CLIENTE_ACEPTA_RENOVACION',
     '2026-09-11T11:20:00',
     'El cliente confirma la renovación');

INSERT INTO historial_dominio
    (id, id_dominio, tipo_evento, fecha, detalle)
VALUES
    (5, 2, 'RENOVACION_REALIZADA',
     '2026-09-12T09:15:00',
     'Renovación realizada en el registrador');

INSERT INTO historial_dominio
    (id, id_dominio, tipo_evento, fecha, detalle)
VALUES
    (6, 3, 'RENOVACION_REALIZADA',
     '2026-09-20T09:15:00',
     'Renovación realizada en el registrador');

INSERT INTO historial_dominio
    (id, id_dominio, tipo_evento, fecha, detalle)
VALUES
    (7, 3, 'FACTURACION_REALIZADA',
     '2026-09-25T12:00:00',
     'Facturación realizada');

INSERT INTO historial_dominio
    (id, id_dominio, tipo_evento, fecha, detalle)
VALUES
    (8, 4, 'AVISO_RENOVACION_ENVIADO',
     '2026-09-20T09:00:00',
     'Aviso de renovación enviado al cliente');

INSERT INTO historial_dominio
    (id, id_dominio, tipo_evento, fecha, detalle)
VALUES
    (9, 4, 'CLIENTE_RECHAZA_RENOVACION',
     '2026-09-21T16:45:00',
     'El cliente rechaza la renovación');

INSERT INTO historial_dominio
    (id, id_dominio, tipo_evento, fecha, detalle)
VALUES
    (10, 5, 'AVISO_RENOVACION_ENVIADO',
     '2026-09-27T09:00:00',
     'Aviso de renovación enviado al cliente');

INSERT INTO historial_dominio
    (id, id_dominio, tipo_evento, fecha, detalle)
VALUES
    (11, 6, 'AVISO_RENOVACION_ENVIADO',
     '2026-09-15T09:00:00',
     'Último aviso de renovación enviado');

-- ------------------------------------------------------------
-- REINICIAR SECUENCIAS DE IDENTIDAD
-- ------------------------------------------------------------

ALTER TABLE cliente
    ALTER COLUMN id RESTART WITH 4;

ALTER TABLE dominio
    ALTER COLUMN id RESTART WITH 7;

ALTER TABLE facturacion
    ALTER COLUMN id RESTART WITH 6;

ALTER TABLE token_cliente
    ALTER COLUMN id RESTART WITH 4;

ALTER TABLE token_dominio
    ALTER COLUMN id RESTART WITH 4;

ALTER TABLE historial_dominio
    ALTER COLUMN id RESTART WITH 12;