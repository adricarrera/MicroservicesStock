## Project Overview
This project is aimed at building an order and inventory management system based on microservices.
It manages the orders, then checks the inventory and updates it accordingly.

*Current implementation:*

It is built using Spring Boot to develop the REST API.
PostgreSQL is used as the database to store the data.
Flyway is used to manage the database migrations.
JUnit and Testcontainers are used to perform unit and integration tests.
Docker is used for containerization, and Docker Compose is used to orchestrate the containers.

## Current Features
Currently, I'm building order-service microservice, which is responsible for managing the orders.
Orders can be created and retrieved by their ID.

## Requirements
- Java 17
- Docker
- Docker Compose

## Getting Started

### 1. Start infrastructure
`docker compose up -d`
### 2. Run application
`./mvnw spring-boot:run`
### 3. Run tests
`./mvnw test`

## Decisions
### 1. Monorepo
All microservices will be maintained in a single repository. This simplifies project management.
### 2. Flyway over ddl-auto
Flyway is used to manage database schema changes through versioned migrations. 
This provides greater control, traceability and consistency across different environments.
### 3. Testcontainers over Mocks
Testcontainers is used to run integration tests against a real PostgreSQL instance instead of relying exclusively on mocks.
This helps verify actual database interactions and detect integration issues.