# Job Board API

REST API de reclutamiento donde las empresas publican ofertas y los candidatos aplican.

## Stack

- Java 25 + Spring Boot 4
- PostgreSQL 16
- [JSONPlaceholder](https://jsonplaceholder.typicode.com) (candidatos, solo lectura)

## Requisitos

- Docker y Docker Compose

## Levantar el proyecto

```bash
docker-compose up --build
```

La API estará disponible en `http://localhost:8080`.

## Colección Postman

Importar `postman-collection.json` para explorar los endpoints disponibles.
