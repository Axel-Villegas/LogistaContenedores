# Guía de Testing - MS Logística y MS Flota Viajes

Esta guía te ayudará a probar los endpoints de los microservicios **ms-logistica** y **tpi-flota-viajes**.

## 📋 Tabla de Contenidos

1. [Prerequisitos](#prerequisitos)
2. [Configuración Inicial](#configuración-inicial)
3. [Obtención de Tokens JWT](#obtención-de-tokens-jwt)
4. [Endpoints MS Logística](#endpoints-ms-logística)
5. [Endpoints MS Flota Viajes](#endpoints-ms-flota-viajes)
6. [Flujo Completo de Prueba](#flujo-completo-de-prueba)
7. [Solución de Problemas](#solución-de-problemas)

---

## Prerequisitos

### Servicios Requeridos

1. **PostgreSQL** - Base de datos (puerto 5432)
2. **Keycloak** - Servidor de autenticación (puerto 8088)
3. **Eureka Server** - Service discovery (puerto 8761)
4. **OSRM** - Servicio de routing (puerto 5000)
5. **API Gateway** - Punto de entrada (puerto 8085)

### Herramientas Recomendadas

- **Postman** o **REST Client** (extensión de VS Code)
- **cURL** (para línea de comandos)
- Navegador web (para Swagger UI)

---

## Configuración Inicial

### 1. Levantar los Servicios

```bash
# Desde la raíz del proyecto
docker-compose up -d
```

Verifica que todos los servicios estén corriendo:

```bash
docker-compose ps
```

### 2. Verificar Salud de los Servicios

- **Eureka Dashboard**: http://localhost:8761
- **Keycloak Admin**: http://localhost:8088
- **API Gateway**: http://localhost:8085
- **MS Logística Swagger**: http://localhost:8082/swagger-ui.html
- **MS Flota Swagger**: http://localhost:8083/swagger-ui.html

---

## Obtención de Tokens JWT

### Configuración en Keycloak

1. Accede a Keycloak Admin Console: http://localhost:8088
2. Login: `admin` / `admin123`
3. Realm: `tpi-backend`
4. Crea usuarios con roles:
   - **OPERADOR**: Para endpoints de gestión de flota
   - **TRANSPORTISTA**: Para endpoints de ejecución de viajes

### Obtener Token (cURL)

```bash
# Token para OPERADOR (confidential client `backend-client`)
curl -X POST http://localhost:8088/realms/tpi-backend/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=backend-client" \
  -d "client_secret=backend-secret" \
  -d "username=operador" \
  -d "password=operador"

# Token para TRANSPORTISTA (confidential client `backend-client`)
curl -X POST http://localhost:8088/realms/tpi-backend/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=backend-client" \
  -d "client_secret=backend-secret" \
  -d "username=transportista" \
  -d "password=transportista"
```

Nota: si en tu entorno Keycloak está mapeado al puerto `8088` (como indica la guía), reemplaza `8081` por `8088` en las URLs anteriores.

**Nota**: Reemplaza `<client-id>`, `<usuario-operador>`, etc. con tus valores reales.

### Verificar / obtener el `client_secret`

Si obtienes el error `{"error":"unauthorized_client","error_description":"Invalid client or Invalid client credentials"}` significa que el `client_secret` que estás usando no coincide con el configurado en Keycloak. Para entornos de desarrollo puedes obtener el secret actual con la Admin API. Ejemplo (requiere `jq` para parsear JSON):

```bash
# 1) Obtener un token de administrador (master realm)
ADMIN_TOKEN=$(curl -s -X POST 'http://localhost:8081/realms/master/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password&client_id=admin-cli&username=admin&password=admin123' | jq -r '.access_token')

# 2) Obtener el id interno del cliente `backend-client`
CLIENT_INTERNAL_ID=$(curl -s -H "Authorization: Bearer $ADMIN_TOKEN" \
  'http://localhost:8081/admin/realms/tpi-backend/clients?clientId=backend-client' | jq -r '.[0].id')

# 3) Obtener el secret actual
curl -s -H "Authorization: Bearer $ADMIN_TOKEN" \
  "http://localhost:8081/admin/realms/tpi-backend/clients/$CLIENT_INTERNAL_ID/client-secret" | jq -r '.value'
```

Si no tienes `jq`, puedes ejecutar las mismas llamadas desde PowerShell tal como se muestra en la guía de administración de Keycloak o usar el Admin Console en `http://localhost:8081` para ver el secret del cliente.

---

## Endpoints MS Logística

**Base URL**: `http://localhost:8082` (directo) o `http://localhost:8085` (vía Gateway)
**Prefijo**: `/api/v1`

### 1. Crear Depósito

**POST** `/api/v1/depositos`

**Descripción**: Crea un nuevo depósito en el sistema.

**Autenticación**: No requerida (o según configuración)

**Request Body**:
```json
{
  "nombre": "Depósito Central Buenos Aires",
  "direccion": "Av. Corrientes 1234, CABA",
  "latitud": -34.6037,
  "longitud": -58.3816,
  "costoEstadiaDiario": 5000.0
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "nombre": "Depósito Central Buenos Aires",
  "direccion": "Av. Corrientes 1234, CABA",
  "latitud": -34.6037,
  "longitud": -58.3816,
  "costoEstadiaDiario": 5000.0
}
```

---

### 2. Crear Tarifa

**POST** `/api/v1/tarifas`

**Descripción**: Crea una nueva tarifa vigente desde una fecha.

**Autenticación**: No requerida (o según configuración)

**Request Body**:
```json
{
  "valorKMBase": 150.0,
  "costoLitroCombustible": 850.0,
  "fechaVigencia": "2024-01-01"
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "valorKMBase": 150.0,
  "costoLitroCombustible": 850.0,
  "fechaVigencia": "2024-01-01"
}
```

---

### 3. Calcular Ruta (GET)

**GET** `/api/v1/rutas/calcular`

**Descripción**: Calcula distancia, tiempo y costo estimado de una ruta.

**Query Parameters**:
- `latOrigen` (required): Latitud del origen
- `lonOrigen` (required): Longitud del origen
- `latDestino` (required): Latitud del destino
- `lonDestino` (required): Longitud del destino
- `idDepositos` (optional): Lista de IDs de depósitos intermedios
- `idTarifa` (required): ID de la tarifa a usar

**Ejemplo**:
```
GET /api/v1/rutas/calcular?latOrigen=-34.6037&lonOrigen=-58.3816&latDestino=-34.7912&lonDestino=-57.9736&idTarifa=1
```

**Response** (200 OK):
```json
{
  "distanciaKm": 125.5,
  "tiempoSegundos": 7200,
  "costoEstimado": 18825.0
}
```

---

### 4. Planificar Ruta (POST)

**POST** `/api/v1/rutas/planificar`

**Descripción**: Planifica una ruta completa con tramos, calculando distancias y costos.

**Request Body**:
```json
{
  "nroSolicitud": "SOL-2024-001",
  "idDepositos": [1, 2],
  "latOrigen": -34.6037,
  "lonOrigen": -58.3816,
  "latDestino": -34.7912,
  "lonDestino": -57.9736,
  "idTarifa": 1
}
```

**Response** (201 Created):
```json
{
  "idRuta": 1,
  "nroSolicitud": "SOL-2024-001",
  "distanciaTotalKm": 250.5,
  "tiempoTotalSegundos": 14400,
  "costoEstimadoTotal": 37575.0,
  "tramos": [
    {
      "id": 1,
      "orden": 1,
      "origen": {
        "lat": -34.6037,
        "lon": -58.3816
      },
      "destino": {
        "lat": -34.6500,
        "lon": -58.4000
      },
      "distanciaKm": 10.5,
      "tiempoSegundos": 900,
      "estado": "PENDIENTE"
    }
  ]
}
```

---

### 5. Iniciar Tramo (INTERNO)

**PUT** `/api/v1/tramos/{id}/iniciar`

**Descripción**: Marca un tramo como iniciado. Usado internamente por MS Flota.

**Query Parameters**:
- `id` (path): ID del tramo

**Response** (200 OK): Vacío

---

### 6. Finalizar Tramo (INTERNO)

**PUT** `/api/v1/tramos/{id}/finalizar`

**Descripción**: Marca un tramo como finalizado con kilómetros recorridos.

**Query Parameters**:
- `id` (path): ID del tramo
- `kmRecorridos` (query): Kilómetros reales recorridos

**Ejemplo**:
```
PUT /api/v1/tramos/1/finalizar?kmRecorridos=125.5
```

**Response** (200 OK): Vacío

---

## Endpoints MS Flota Viajes

**Base URL**: `http://localhost:8083` (directo) o `http://localhost:8085` (vía Gateway)
**Prefijo**: `/api/flota`

### 1. Crear Transportista

**POST** `/api/flota/transportistas`

**Descripción**: Crea un nuevo transportista en el sistema.

**Autenticación**: Requerida - Rol: `OPERADOR`

**Request Body**:
```json
{
  "nombre": "Juan Pérez",
  "licencia": "LIC12345678",
  "contacto": "juan.perez@email.com"
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "nombre": "Juan Pérez",
  "licencia": "LIC12345678",
  "contacto": "juan.perez@email.com"
}
```

---

### 2. Crear Camión

**POST** `/api/flota/camiones`

**Descripción**: Registra un nuevo camión asociado a un transportista.

**Autenticación**: Requerida - Rol: `OPERADOR`

**Request Body**:
```json
{
  "dominio": "AY 123 BC",
  "capacidadPeso": 20000.0,
  "capacidadVolumen": 50.0,
  "idTransportista": 1,
  "costoPorKm": 120.0,
  "consumoCombustiblePromedio": 2.5
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "dominio": "AY 123 BC",
  "capacidadPeso": 20000.0,
  "capacidadVolumen": 50.0,
  "idTransportista": 1,
  "estado": "DISPONIBLE",
  "costoPorKm": 120.0,
  "consumoCombustiblePromedio": 2.5
}
```

---

### 3. Obtener Camión por ID

**GET** `/api/flota/camiones/{id}`

**Descripción**: Obtiene los detalles de un camión específico.

**Autenticación**: Requerida - Rol: `OPERADOR`

**Response** (200 OK):
```json
{
  "id": 1,
  "dominio": "AY 123 BC",
  "capacidadPeso": 20000.0,
  "capacidadVolumen": 50.0,
  "idTransportista": 1,
  "estado": "DISPONIBLE",
  "costoPorKm": 120.0,
  "consumoCombustiblePromedio": 2.5
}
```

---

### 4. Obtener Camiones Disponibles

**POST** `/api/flota/camiones/disponibles`

**Descripción**: Busca camiones disponibles que cumplan con los requisitos de capacidad.

**Autenticación**: Requerida - Rol: `OPERADOR`

**Request Body**:
```json
{
  "capacidadPesoRequerida": 15000.0,
  "capacidadVolumenRequerida": 40.0
}
```

**Response** (200 OK):
```json
{
  "camiones": [
    {
      "id": 1,
      "dominio": "AY 123 BC",
      "capacidadPeso": 20000.0,
      "capacidadVolumen": 50.0,
      "idTransportista": 1,
      "estado": "DISPONIBLE",
      "costoPorKm": 120.0
    }
  ],
  "total": 1
}
```

---

### 5. Obtener Camiones por Transportista

**GET** `/api/flota/transportistas/{id}/camiones`

**Descripción**: Lista todos los camiones de un transportista específico.

**Autenticación**: Requerida - Rol: `OPERADOR`

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "dominio": "AY 123 BC",
    "capacidadPeso": 20000.0,
    "capacidadVolumen": 50.0,
    "idTransportista": 1,
    "estado": "DISPONIBLE",
    "costoPorKm": 120.0
  }
]
```

---

### 6. Iniciar Tramo

**POST** `/api/flota/tramos/{idTramo}/iniciar`

**Descripción**: Inicia la ejecución de un tramo con un camión específico.

**Autenticación**: Requerida - Rol: `TRANSPORTISTA`

**Request Body**:
```json
{
  "dominioCamion": "AY 123 BC"
}
```

**Response** (200 OK):
```json
{
  "id": 1,
  "orden": 1,
  "estado": "EN_CURSO",
  "distanciaKm": 125.5,
  "kmRecorridos": 0.0
}
```

---

### 7. Finalizar Tramo

**POST** `/api/flota/tramos/{idTramo}/finalizar`

**Descripción**: Finaliza un tramo en curso registrando los kilómetros recorridos.

**Autenticación**: Requerida - Rol: `TRANSPORTISTA`

**Request Body**:
```json
{
  "dominioCamion": "AY 123 BC",
  "kmRecorridos": 125.5
}
```

**Response** (200 OK):
```json
{
  "id": 1,
  "orden": 1,
  "estado": "FINALIZADO",
  "distanciaKm": 125.5,
  "kmRecorridos": 125.5
}
```

---

## Flujo Completo de Prueba

### Escenario: Planificar y Ejecutar una Ruta

1. **Crear Depósito** (MS Logística)
   ```bash
   POST /api/v1/depositos
   ```

2. **Crear Tarifa** (MS Logística)
   ```bash
   POST /api/v1/tarifas
   ```

3. **Crear Transportista** (MS Flota)
   ```bash
   POST /api/flota/transportistas
   # Requiere token OPERADOR
   ```

4. **Crear Camión** (MS Flota)
   ```bash
   POST /api/flota/camiones
   # Requiere token OPERADOR
   ```

5. **Planificar Ruta** (MS Logística)
   ```bash
   POST /api/v1/rutas/planificar
   # Esto crea la ruta con tramos
   ```

6. **Obtener Camiones Disponibles** (MS Flota)
   ```bash
   POST /api/flota/camiones/disponibles
   # Requiere token OPERADOR
   ```

7. **Iniciar Tramo** (MS Flota)
   ```bash
   POST /api/flota/tramos/{idTramo}/iniciar
   # Requiere token TRANSPORTISTA
   ```

8. **Finalizar Tramo** (MS Flota)
   ```bash
   POST /api/flota/tramos/{idTramo}/finalizar
   # Requiere token TRANSPORTISTA
   ```

---

## Solución de Problemas

### Error: "Connection refused"

**Causa**: El servicio no está corriendo o el puerto está incorrecto.

**Solución**:
```bash
# Verificar servicios
docker-compose ps

# Ver logs
docker-compose logs ms-logistica
docker-compose logs ms-flota
```

### Error: "401 Unauthorized"

**Causa**: Token JWT inválido, expirado o sin los roles necesarios.

**Solución**:
- Verifica que el token sea válido
- Asegúrate de que el usuario tenga el rol correcto en Keycloak
- Obtén un nuevo token

### Error: "404 Not Found"

**Causa**: Ruta incorrecta o servicio no registrado en Eureka.

**Solución**:
- Verifica la URL en Eureka Dashboard: http://localhost:8761
- Revisa las rutas en el API Gateway
- Usa Swagger UI para ver los endpoints disponibles

### Error: "500 Internal Server Error"

**Causa**: Error en el servidor (BD, dependencias, etc.)

**Solución**:
```bash
# Ver logs detallados
docker-compose logs -f ms-logistica
docker-compose logs -f ms-flota

# Verificar conexión a BD
docker-compose exec postgres psql -U flowbox_user -d flowboxdb
```

### Error: "OSRM service unavailable"

**Causa**: El servicio OSRM no está disponible.

**Solución**:
```bash
# Verificar que OSRM esté corriendo
docker-compose ps osrm

# Ver logs
docker-compose logs osrm
```

---

## Notas Adicionales

1. **Swagger UI**: Ambos microservicios tienen Swagger habilitado:
   - MS Logística: http://localhost:8082/swagger-ui.html
   - MS Flota: http://localhost:8083/swagger-ui.html

2. **Base de Datos**: Los esquemas se crean automáticamente con `ddl-auto: update`

3. **Logs**: Los logs están configurados en nivel DEBUG para `com.logistica` y `utn.backend.tpi`

4. **Eureka**: Verifica que los servicios estén registrados en http://localhost:8761

---

## Archivos de Prueba

El archivo `test-endpoints.http` contiene ejemplos listos para usar con REST Client (VS Code).

Para usarlo:
1. Instala la extensión "REST Client" en VS Code
2. Abre `test-endpoints.http`
3. Configura las variables `@token_operador` y `@token_transportista`
4. Ejecuta las peticiones haciendo clic en "Send Request"

---

**Última actualización**: 2024
