# Estado actual (WIP)

```text
                    ┌─────────────────┐
                    │ RenovacionService│
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │   TokenService  │
                    └────────┬────────┘
                             │
                ┌────────────┴────────────┐
                ▼                         ▼
         TokenCliente              TokenDominio
                │                         │
                └────────────┬────────────┘
                             ▼
                    RenovacionController
                             │
                             ▼
                        Thymeleaf
```

# 1. Cerrar el flujo de renovación

Ahora:

```text
RenovacionController
       ↓
TokenService
       ↓
TokenDominio
       ↓
CONFIRMADO / RECHAZADO
```

Pero todavía hay una diferencia entre:

> "el cliente ha confirmado que quiere renovar"

y

> "el dominio se ha renovado realmente"

Actualmente `procesarConfirmacion()` marca los `TokenDominio` como confirmados/rechazados y marca el token como usado.

Eso **no debería confundirse todavía con realizar la renovación/facturación**.

**establecer claramente esta separación:**

```text
CLIENTE
   ↓
CONFIRMA
   ↓
TokenDominio
   ↓
CONFIRMADO
   ↓
PROCESO INTERNO
   ↓
RENOVACIÓN
   ↓
FACTURACIÓN
```


---

# 3. Después `RenovacionService`


Actualmente hace:

```text
buscar dominios próximos a caducar
        ↓
calcular umbral
        ↓
cambiar estado
        ↓
generar TokenCliente
```

pero termina aquí:

```java
TokenCliente token = tokenService.generarToken(dominio);
```

y después

```java
//TODO:EmailService
```

Siguiente paso: completar TODO de renovacionService

---

# 4. Integrar EmailService

El flujo debería quedar:

```text
RenovacionService
       │
       ├── encuentra dominio
       │
       ├── calcula umbral
       │
       ├── genera TokenCliente
       │
       └── EmailService
                │
                ▼
        email al cliente
                │
                ▼
       enlace de renovación
```

decision!

## Un token por dominio o un token por cliente

se mantendría

**1 TokenCliente → N TokenDominio**

Es decir:

```text
Cliente Ana
    │
    └── TokenCliente ABC123
            │
            ├── dominio1.es
            ├── dominio2.com
            └── dominio3.net
```

---

# Timeline actualizado


## 🟢 BLOQUE 1 — Renovación cliente

### #17 — RenovacionController

Terminar:

* `RenovacionControllerTest`
* tests de todos los estados
* test de POST
* test sin dominios
* verificar `procesarConfirmacion()`

Después:

**PR → master**

---

## 🟢 BLOQUE 2 — Token

### Semana 1-2

Terminar la batería de:

```text
TokenServiceTest
TokenServiceIntegrationTest
```

Casos mínimos:

```text
generar token
    ✓ genera UUID
    ✓ fecha creación
    ✓ fecha expiración
    ✓ usado = false

validar token
    ✓ válido
    ✓ inexistente
    ✓ expirado
    ✓ usado

procesarConfirmacion
    ✓ todos confirmados
    ✓ algunos confirmados
    ✓ ninguno confirmado
    ✓ token usado
```

---

# 🟢 BLOQUE 3 — Emails



Aquí atacar la integración pendiente de `RenovacionService`.

Actualmente:

```text
RenovacionService
       ↓
TokenService
       ↓
Token generado
```

Queremos:

```text
RenovacionService
       ↓
TokenService
       ↓
Token generado
       ↓
EmailService
       ↓
email
```

Y crear tests para esto.

---

# 🟢 BLOQUE 4 — Scheduler



Una vez que el envío manual funciona:

```text
@Scheduled
    ↓
RenovacionService.procesarAvisos()
```

Aquí es donde el proyecto empieza a funcionar realmente como aplicación de producción.

El proceso completo será:

```text
             SCHEDULER
                 ↓
       RenovacionService
                 ↓
        ¿Qué dominios?
                 ↓
          ¿Qué umbral?
                 ↓
          generar token
                 ↓
           enviar email
```

---

# 🟠 BLOQUE 5 — Respuesta del cliente




```text
Email
  ↓
Cliente abre enlace
  ↓
RenovacionController
  ↓
TokenService.validarToken()
  ↓
Formulario
  ↓
Cliente selecciona
  ↓
POST
  ↓
procesarConfirmacion()
```

Y aquí implementar la **issue relacionada con los avisos después de renovar/cancelar**.

La idea sería:

```text
CONFIRMADO
    ↓
registrar renovación

RECHAZADO
    ↓
registrar cancelación
```

---

# 🟠 BLOQUE 6 — Facturación


Ahora sí.


```text
qué cliente
qué dominio
qué ha decidido
cuándo
qué token
```

Entonces tiene sentido trabajar con:

```text
Facturacion
```

y su historial.

El flujo:

```text
TokenDominio
     │
     ├── CONFIRMADO
     │       ↓
     │   Facturación
     │
     └── RECHAZADO
             ↓
        No facturar
```

---

# 🟡 BLOQUE 7 — Logging


Después:

**#14 Logger**


```text
INFO
WARN
ERROR
```

Por ejemplo:

```text
INFO  Token generado
INFO  Aviso enviado
INFO  Cliente ha confirmado dominio
INFO  Cliente ha rechazado dominio
WARN  Token expirado
WARN  Token ya utilizado
ERROR Error enviando email
ERROR Error procesando facturación
```

---

# 🟡 BLOQUE 8 — Seguridad


Antes de producción:

* secretos fuera de Git
* configuración por entorno
* SMTP
* tokens
* validación de parámetros
* protección de endpoints
* logs sin información sensible
* manejo de excepciones

 **obligatorio antes del despliegue**.

---

# 🟢 BLOQUE 9 — Testing final

### Semana 8

Prueba de integración del proceso

```text
BD
 ↓
RenovacionService
 ↓
TokenService
 ↓
EmailService
 ↓
Controller
 ↓
POST
 ↓
TokenDominio
 ↓
Facturacion
```

Y algunos escenarios completos:

### Caso 1

```text
Dominio caduca en 30 días
→ email
→ cliente acepta
→ CONFIRMADO
→ facturación
```

### Caso 2

```text
Dominio caduca en 30 días
→ email
→ cliente rechaza
→ RECHAZADO
→ no facturar
```

### Caso 3

```text
Token expirado
→ no permite operación
```

### Caso 4

```text
Token usado
→ no permite operación
```

### Caso 5

```text
Dominio ya avisado en ese umbral
→ no vuelve a enviar
```

---

### Pasos próximos:

Yo haría exactamente esto ahora:

**1. Terminar `RenovacionControllerTest`**
**2. Revisar `TokenServiceTest` y `TokenServiceIntegrationTest` contra la implementación actual**
**3. Ejecutar todos los tests**
**4. Corregir lo que falle**
**5. Hacer el PR de `17-renovacioncontroller` a `master`**
**6. Pasar a `RenovacionService + EmailService`**

Y especialmente revisaría antes de hacer el PR una cosa del código actual: `generarToken(Dominio dominio)` recibe un `Dominio`, pero en la implementación que veo **no está asociando el token generado con ese dominio/cliente**; simplemente crea el `TokenCliente` y lo guarda.

Eso es potencialmente importante para el siguiente bloque y **yo lo solucionaría antes de integrar el email**, porque de lo contrario puedes acabar generando tokens que luego no tienen correctamente asociados sus `TokenDominio`.
