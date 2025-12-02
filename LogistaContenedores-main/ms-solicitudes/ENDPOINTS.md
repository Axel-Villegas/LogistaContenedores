# Endpoints para pruebas - ms-solicitudes

Este documento describe las rutas principales del microservicio `ms-solicitudes`, ejemplos de petición y notas de autenticación para realizar pruebas manuales.

Base URL (por defecto en `application.yml`):
- http://localhost:8000

Swagger (UI):
- http://localhost:8000/swagger-ui.html
- OpenAPI JSON: http://localhost:8000/v3/api-docs

Notas generales:
- El servicio usa `oauth2ResourceServer` con JWT (issuer: `http://localhost:8081/realms/TPI`).
- Las rutas están protegidas. Para probarlas necesitarás un token JWT válido con los roles apropiados (ver "Autenticación" más abajo).
- El controlador principal es `SolicitudesController` con prefijo `/api/v1/solicitudes`.

Rutas principales

1) Crear nueva solicitud
- Método: POST
- URL: `/api/v1/solicitudes`
- Roles requeridos: `ROLE_CLIENTE` (en `SecurityConfig` se pide `hasRole("CLIENTE")`)
- Body (JSON) de ejemplo:

```json
{
  "idCliente": "cliente-123",
  "idContenedor": "contenedor-99",
  "origen": { "dir": "Puerto A", "lat": -34.6, "lon": -58.4 },
  "destino": { "dir": "Puerto B", "lat": -34.7, "lon": -58.5 }
}
```

- Respuesta esperada: `200 OK` con JSON tipo `SolicitudResponseDTO`:
```json
{
  "nroSolicitud": "xxxxxxxx",
  "estado": "BORRADOR"
}
```

- Curl (reemplazar `<TOKEN>`):

```bash
curl -X POST http://localhost:8000/api/v1/solicitudes \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"idCliente":"cliente-123","idContenedor":"cont-1","origen":{"dir":"A","lat":-34.6,"lon":-58.4},"destino":{"dir":"B","lat":-34.7,"lon":-58.5}}'
```

2) Obtener solicitud por número
- Método: GET
- URL: `/api/v1/solicitudes/{nro}`
- Roles requeridos: `ROLE_CLIENTE`
- Respuesta: `200 OK` con el objeto `Solicitud` o `404 Not Found` si no existe.

- Curl:

```bash
curl -H "Authorization: Bearer <TOKEN>" http://localhost:8000/api/v1/solicitudes/abcdefgh
```

3) Listar solicitudes pendientes
- Método: GET
- URL: `/api/v1/solicitudes/pendientes`
- Roles requeridos: `ROLE_OPERADOR`
- Respuesta: `200 OK` con lista JSON de `Solicitud` (estado BORRADOR)

- Curl:

```bash
curl -H "Authorization: Bearer <TOKEN_OPERADOR>" http://localhost:8000/api/v1/solicitudes/pendientes
```

4) Aceptar una solicitud (cambiar a ACEPTADA)
- Método: PUT
- URL: `/api/v1/solicitudes/{nro}/aceptar`
- Roles requeridos: `ROLE_OPERADOR`
- Respuesta: `200 OK` con la solicitud actualizada o `404 Not Found`.

- Curl:

```bash
curl -X PUT -H "Authorization: Bearer <TOKEN_OPERADOR>" http://localhost:8000/api/v1/solicitudes/abcdefgh/aceptar
```

Autenticación / Obtención de token (Keycloak)
- En `docker-compose.yml` se propone un contenedor `keycloak` en el puerto `8081`.
- Para obtener un token de acceso usando Resource Owner Password Credentials (si está permitido) o client credentials, usar el endpoint de token de Keycloak:

Ejemplo (cliente público/ropc):

```bash
curl -X POST "http://localhost:8081/realms/TPI/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=<client-id>&username=<user>&password=<pass>"
```

El campo `access_token` en la respuesta es el valor a poner en la cabecera `Authorization: Bearer <token>`.

Notas de prueba y entorno
- Dependencias necesarias para ejecutar localmente:
  - PostgreSQL (si usas la configuración por defecto de `application.yml`) — el `DataSourceConfig` se conecta a `jdbc:postgresql://localhost:5432/flowboxdb`.
  - Keycloak en `http://localhost:8081` para emitir tokens.
  - Alternativa útil para pruebas rápidas: ejecutar la app en modo desarrollo y deshabilitar la seguridad temporalmente (no recomendado en equipo compartido). O usar un JWT firmado localmente con la misma clave pública que Keycloak expone.

Sugerencias para pruebas de integración rápida
- Iniciar con `docker-compose up` (contiene `postgres` y `keycloak`) antes de arrancar la app.
- Usar Swagger UI (`/swagger-ui.html`) para probar llamadas interactuamente (la UI se puede configurar para inyectar el token JWT).
- Crear una solicitud con el token de `CLIENTE`, luego desde otro cliente `OPERADOR` listar pendientes y aceptar la creada.

Si quieres, puedo:
- Crear colecciones Postman / HTTPie / REST Client con estos ejemplos.
- Añadir tests automatizados (JUnit + MockMvc) con bypass de seguridad para verificar el flujo básico.

---
Archivo generado automáticamente: `ms-solicitudes/ENDPOINTS.md`.
