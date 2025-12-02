# API Reference Documentation

This document provides a comprehensive list of the API endpoints available in the microservices architecture. It is intended for frontend developers to understand how to consume the services.

**Base URL:** All requests should be routed through the API Gateway at `http://localhost:8080`.

## 1. Microservice: `ms-solicitudes` (Requests)
**Base Path:** `/api/v1/solicitudes`

| Method | Endpoint | Description | Request Body | Response | Roles |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/` | Create a new transport request. | `SolicitudRequestDTO` | `SolicitudResponseDTO` | `CLIENTE` |
| `GET` | `/{nro}` | Get a request by its number (UUID). | - | `Solicitud` | `CLIENTE` |
| `GET` | `/pendientes` | Get all requests with status `BORRADOR`. | - | `List<Solicitud>` | `OPERADOR` |
| `PUT` | `/{nro}/aceptar` | Accept a request (changes status to `ACEPTADA`). | - | `Solicitud` | `OPERADOR` |

## 2. Microservice: `ms-logistica` (Logistics)
**Base Path:** `/api/v1`

| Method | Endpoint | Description | Request Body | Response | Roles |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/rutas/planificar` | Plan a route for a request. | `PlanificarRutaRequest` | `RutaPlanningResponse` | `OPERADOR` |
| `GET` | `/rutas/calcular` | Calculate distance and cost between points. | - | `CalculoResponse` | `OPERADOR` |
| `POST` | `/depositos` | Create a new deposit. | `Deposito` | `Deposito` | `OPERADOR` |
| `POST` | `/tarifas` | Create a new tariff. | `Tarifa` | `Tarifa` | `OPERADOR` |
| `GET` | `/contenedores/pendientes` | Get pending containers (tramos not finalized). | - | `List<Tramo>` | `Authenticated` (CLIENTE, OPERADOR) |
| `POST` | `/tramos/{id}/asignar` | Assign a truck to a tramo (Internal/Direct). | - | `Void` | `Authenticated` (Internal) |
| `PUT` | `/tramos/{id}/iniciar` | Mark tramo as started. | - | `Void` | `Authenticated` (Internal) |
| `PUT` | `/tramos/{id}/finalizar` | Mark tramo as finished. | - | `Void` | `Authenticated` (Internal) |

## 3. Microservice: `tpi-flota-viajes` (Fleet & Trips)
**Base Path:** `/api/flota`

| Method | Endpoint | Description | Request Body | Response | Roles |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/camiones` | Register a new truck. | `CrearCamionRequest` | `CamionResponse` | `OPERADOR` |
| `GET` | `/camiones/{id}` | Get truck details by ID. | - | `CamionResponse` | `OPERADOR` |
| `POST` | `/camiones/disponibles` | Get available trucks matching capacity. | `ObtenerDisponiblesRequest` | `ListaCamionesDisponiblesResponse` | `OPERADOR` |
| `POST` | `/transportistas` | Register a new carrier (transportista). | `CrearTransportistaRequest` | `TransportistaResponse` | `OPERADOR` |
| `GET` | `/transportistas/{id}/camiones` | Get all trucks for a carrier. | - | `List<CamionResponse>` | `OPERADOR` |
| `POST` | `/tramos/{idTramo}/asignar` | Assign a truck to a tramo. | - | `Void` | `OPERADOR` |
| `POST` | `/tramos/{idTramo}/iniciar` | Start a trip (tramo). | `IniciarTramoRequest` | `TramoDTO` | `TRANSPORTISTA` |
| `POST` | `/tramos/{idTramo}/finalizar` | Finish a trip (tramo). | `FinalizarTramoRequest` | `TramoDTO` | `TRANSPORTISTA` |

## Data Models (Brief)

### SolicitudRequestDTO
```json
{
  "idCliente": "string",
  "idContenedor": "string",
  "destino": {
    "lat": 0.0,
    "lon": 0.0
  }
}
```

### PlanificarRutaRequest
```json
{
  "nroSolicitud": "string",
  "idDepositos": [1, 2],
  "latOrigen": 0.0,
  "lonOrigen": 0.0,
  "latDestino": 0.0,
  "lonDestino": 0.0,
  "idTarifa": 1
}
```

### CrearCamionRequest
```json
{
  "dominio": "AA123BB",
  "capacidadPeso": 10000.0,
  "capacidadVolumen": 50.0,
  "consumoCombustiblePromedio": 15.0,
  "costoPorKm": 100.0,
  "idTransportista": 1
}
```

## Roles & Security

The API uses Role-Based Access Control (RBAC) via Keycloak. The following roles are defined:

*   **CLIENTE**: Can create and view their own transport requests.
*   **OPERADOR**: Has full access to manage logistics, fleet, and approve requests. Can also view pending requests.
*   **TRANSPORTISTA**: Can manage trips (start/finish) assigned to them.
*   **Authenticated (Internal)**: Endpoints intended for service-to-service communication or general authenticated access.
