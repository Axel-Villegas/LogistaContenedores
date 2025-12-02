# Guía de Pruebas para MS-Logística

Esta guía te ayudará a probar los endpoints del microservicio de Logística para asegurar que funcionan correctamente, incluyendo la seguridad con Keycloak y la lógica de negocio.

## 1. Prerrequisitos

Asegúrate de tener los siguientes servicios corriendo antes de iniciar:

*   **PostgreSQL**: Base de datos para `ms-logistica`.
*   **Keycloak**: Servidor de autenticación (Puerto 8088).
*   **RabbitMQ**: Para mensajería asíncrona (Puerto 5672).
*   **OSRM**: Servicio de enrutamiento (Puerto 5000).

## 2. Iniciar la Aplicación

Ejecuta el siguiente comando en la terminal dentro de la carpeta `ms-logistica`:

```bash
mvn spring-boot:run
```

La aplicación iniciará en el puerto **8082** (por defecto).

## 3. Probar con Swagger UI (Recomendado)

La forma más fácil de probar es usando la interfaz visual de Swagger, que ya hemos configurado.

1.  Abre tu navegador y ve a: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
2.  Haz clic en el botón **Authorize** (arriba a la derecha).
3.  Ingresa tu token JWT (Bearer token).
    *   *Nota: Necesitas obtener un token de Keycloak para un usuario con el rol `OPERADOR`.*
4.  Ahora puedes probar los endpoints directamente desde la interfaz.

## 4. Probar con CURL (Manual)

Si prefieres usar la terminal, aquí tienes ejemplos de `curl`.

**IMPORTANTE**: Reemplaza `TU_TOKEN_JWT` con un token real obtenido de Keycloak.

### A. Crear una Tarifa (Rol: OPERADOR)
Necesaria para calcular costos.

```bash
curl -X POST http://localhost:8082/api/v1/tarifas \
  -H "Authorization: Bearer TU_TOKEN_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "valorKMBase": 100.0,
    "costoLitroCombustible": 1.5,
    "fechaVigencia": "2024-01-01T00:00:00"
  }'
```

### B. Crear un Depósito (Rol: OPERADOR)
Opcional, para usar como punto intermedio.

```bash
curl -X POST http://localhost:8082/api/v1/depositos \
  -H "Authorization: Bearer TU_TOKEN_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Deposito Central",
    "direccion": "Av. Principal 123",
    "latitud": -34.6037,
    "longitud": -58.3816,
    "costoEstadiaDiario": 500.0
  }'
```

### C. Planificar una Ruta (Rol: OPERADOR)
Este es el flujo principal. Calcula la ruta entre origen y destino.

```bash
curl -X POST http://localhost:8082/api/v1/rutas/planificar \
  -H "Authorization: Bearer TU_TOKEN_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "nroSolicitud": "SOL-001",
    "idDepositos": [], 
    "latOrigen": -34.6037,
    "lonOrigen": -58.3816,
    "latDestino": -34.9214,
    "lonDestino": -57.9545,
    "idTarifa": 1
  }'
```
*(Asegúrate de que el `idTarifa` exista, usa el ID retornado en el paso A)*

### D. Consultar Contenedores Pendientes (Autenticado)

```bash
curl -X GET http://localhost:8082/api/v1/contenedores/pendientes \
  -H "Authorization: Bearer TU_TOKEN_JWT"
```

## 5. Obtener un Token de Prueba (Ejemplo)

Si tienes configurado el cliente `tpi-backend` en Keycloak con `client_credentials` o un usuario de prueba:

```bash
curl -X POST http://localhost:8088/realms/tpi-backend/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=api-gateway" \
  -d "client_secret=TU_CLIENT_SECRET" \
  -d "grant_type=client_credentials"
```
*O usa Postman para hacer el login con un usuario (password grant).*
