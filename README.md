# Event Telemetry Service (Reactive Core)

Este microservicio reactivo gestiona la ingesta, evaluación de riesgos, cálculo de comisiones y almacenamiento en tiempo real de la telemetría transaccional. Forma parte de un ejercicio práctico de arquitectura híbrida (Imperativa + Reactiva) orientado al procesamiento asíncrono no bloqueante, la aplicación de patrones de diseño GoF y la exposición multicanal de servicios (REST + gRPC).

> **📌 Repositorio Complementario:**
> Este servicio atiende las solicitudes de evaluación de riesgo y comisiones enviadas desde el servicio transaccional imperativo.
> 🔗 [Account & Transaction Service (gRPC Client / Imperative Core)](https://github.com/Juan-Barraza/account-transaction-service)

---

## Arquitectura

* **Paradigma:** Reactivo y No Bloqueante (Spring WebFlux + Project Reactor).
* **Comunicación Entrante:**
* **REST WebFlux:** Interfaz HTTP expuesta para clientes REST y documentada con OpenAPI 3 / Swagger.
* **Servidor gRPC:** Comunicación de alta velocidad basada en HTTP/2 y Protocol Buffers (Netty + `grpc-spring-boot-starter`).


* **Persistencia:** Amazon DynamoDB (Local / Cloud) utilizando el SDK asíncrono no bloqueante (`DynamoDbAsyncClient`).
* **Manejo de Eventos:** Patrón de publicación y suscripción interno para el procesamiento asíncrono de alertas de seguridad.

---

## Patrones de Diseño Implementados

1. **Strategy Pattern (`FeeCalculationStrategy`):** Encapsula los algoritmos de cálculo de comisiones según el canal transaccional (`APP`, `WEB`, `ATM`, `POS`). Permite la incorporación de nuevas reglas tarifarias sin modificar la lógica principal.
2. **Factory Pattern (`FeeStrategyFactory`):** Desacopla la instanciación de las estrategias de comisión, seleccionando dinámicamente la implementación adecuada en tiempo de ejecución.
3. **Observer / Event Publisher Pattern (`HighRiskTransactionEvent`):** Evalúa si una transacción supera los umbrales de riesgo (> $10.000.000 COP) y emite un evento asíncrono desacoplado para auditoría, notificaciones o monitoreo.
4. **DTO & Builder Pattern:** Garantiza la inmutabilidad de la información intercambiada entre capas mediante Java `Records` y el patrón Builder provisto por Lombok.

---

## Requisitos Previos y Ejecución Conjunta

Este microservicio se orquesta mediante **Docker Compose** junto a DynamoDB Local y el microservicio imperativo de cuentas.

### Variables de Entorno Para Este Microservicio

| Variable | Descripción | Valor por Defecto |
| --- | --- | --- |
| `SERVER_PORT_TELEMETRY` | Puerto HTTP para la API REST | `8081` |
| `GRPC_PORT` | Puerto expuesto para el servidor gRPC | `9090` |
| `DYNAMODB_TABLE_NAME`| Nombre de la tabla | `telemetry_events`|
| `LOCALSTACK_AUTH_TOKEN`| Token de seguridad en localStack local| `token` |
| `AWS_REGION` | Región configurada para DynamoDB | `us-east-1` |
| `AWS_ENDPOINT_URL` | Endpoint del contenedor DynamoDB Local | `http://localhost:4566` |
| `AWS_ACCESS_KEY_ID` | Clave de acceso para ambiente local | `dummy` |
| `AWS_SECRET_ACCESS_KEY` | Clave secreta para ambiente local | `dummy` |

### Despliegue con Docker Compose

Desde la raíz del proyecto orquestador (donde se encuentra el archivo `docker-compose.yml` unificado):

```bash
docker-compose up --build
```

---

## Documentación de API y Servicios

### 1. Documentación REST (OpenAPI / Swagger)

La interfaz Swagger UI está disponible en el entorno local una vez iniciado el servicio:

`http://localhost:8081/swagger-ui.html`

#### Endpoints Principales REST

* `POST /api/v1/telemetry/transactions` — Ingesta, evalúa la comisión y determina el riesgo de una transacción (`Mono<TelemetryResponse>`).
* `GET /api/v1/telemetry/account/{accountId}` — Retorna el flujo reactivo de telemetría de una cuenta, con opción de filtro por alto riesgo (`Flux<TelemetryResponse>`).
* `GET /api/v1/telemetry/account/{accountId}/{timestamp}` — Consulta el detalle de un evento específico mediante clave compuesta (`Mono<TelemetryResponse>`).

---

### 2. Documentación gRPC

El servicio expone la interfaz descrita en el archivo `telemetry.proto` en el puerto `9090`. Posee **Server Reflection** habilitado, lo que permite la auto-descubierta de métodos utilizando clientes como Postman, `grpcurl` o `grpcui`.

#### Métodos gRPC Expuestos

* **`EvaluateTransactionRisk` (Unary RPC):** Recibe la información de la transacción desde el microservicio imperativo, calcula la comisión de forma no bloqueante, evalúa si es de alto riesgo y retorna la respuesta inmediata.
* **`StreamTransactionsByAccount` (Server Streaming RPC):** Transmite un flujo continuo de transacciones (`stream TransactionGrpcResponse`) asociadas a una cuenta hacia el cliente solicitante.