# DevopsSpec — Job Board API

## Contexto
Este documento es una especificación técnica para el agente @agents/devops.md.
Lee este archivo completo antes de crear cualquier archivo.
No inventes configuraciones — sigue estrictamente lo definido aquí.
Stack: Java 25 · Spring Boot 4 · Gradle

---

## TAREA 1 — Dockerfile

Crea `Dockerfile` en la raíz del proyecto.

Implementa multi-stage build con dos etapas:

Etapa 1 — build:
- Imagen base: imagen oficial de Gradle con JDK 25
- Copia el proyecto completo
- Ejecuta el build con Gradle — genera el JAR en build/libs/
- Excluye tests en el build del Dockerfile

Etapa 2 — runtime:
- Imagen base: imagen ligera de JRE 25
- Copia únicamente el JAR generado en la etapa anterior
- Expone el puerto 8080
- Variables de entorno de conexión a DB desde el ambiente — nunca hardcodeadas
- Entrypoint: ejecuta el JAR directamente

---


## TAREA 2 — .github/workflows/deploy.yml

Crea `.github/workflows/deploy.yml`.

Trigger: push a rama main

Job 1 — build:
- Checkout del código
- Setup JDK 25
- Cache de dependencias Gradle
- Compilar con Gradle sin tests
- Build de la imagen Docker
- Push a Docker Hub usando:
  - ${{ secrets.DOCKERHUB_USERNAME }}
  - ${{ secrets.DOCKERHUB_TOKEN }}

Job 2 — deploy:
- Depende de job build
- Conecta al servidor via SSH usando:
  - ${{ secrets.DEPLOY_HOST }}
  - ${{ secrets.DEPLOY_USER }}
  - ${{ secrets.DEPLOY_KEY }}
- Pull de la imagen nueva
- Reinicia el contenedor con docker compose

---

## Restricciones

- No modifiques ningún archivo de código de negocio
- No modifiques el docker-compose.yml existente
- Variables sensibles siempre desde secrets o .env — nunca hardcodeadas
- Crea los archivos en orden: Dockerfile → docker-compose-prod.yml → deploy.yml
- Cuando termines reporta los archivos creados y espera validación