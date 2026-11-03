# Microservices Stock Project

Sistema de pedidos y stock para una cadena de supermercados, hecho para
aprender arquitectura de microservicios con Spring Boot, Kafka y Docker.

## Estado
- [x] order-service
- [ ] inventory-service
- [ ] notification-service
- [ ] Despliegue en AWS

## Cómo arrancarlo
1. `docker compose up -d`
2. Arranca `orderService` (desde el IDE o con `./mvnw spring-boot:run`)
3. Tests: `./mvnw test` (requiere Docker encendido)

## API
- `POST /v1/orders`: crea un pedido
- `GET /v1/orders/{id}`: consulta un pedido

## Decisiones
- **Monorepo**: un repositorio, un proyecto Spring Boot por servicio.
- **Paquetes por funcionalidad** con capas ligeras, sin DDD completo ni hexagonal:
  el dominio es pequeño y no compensa el coste.
- **DTOs separados de las entidades**: la API no expone el modelo de datos.
- **Flyway y `ddl-auto: validate`**: el esquema está versionado.
- **Testcontainers** en vez de mocks o H2: se prueba contra PostgreSQL real.