description: Ejecuta tests según el tipo indicado — unit, integration o manual
argument-hint: [unit | integration | manual]
---

Carga el agente en @agents/test-runner.md y adopta ese rol.

Recibes un argumento:
1. Tipo de test — valores válidos: `unit`, `integration`, `manual`

---

## Si $ARGUMENTS es `unit`

Ejecuta todos los tests unitarios del proyecto Spring Boot.

Antes de ejecutar:
1. Verifica que el proyecto compila correctamente
2. Verifica que Docker está corriendo con `docker compose ps`

Ejecuta:
```bash
mvn test
```

Reporta:
- Total de tests ejecutados
- Tests que pasan y fallan
- Coverage obtenido si está configurado
- Tiempo de ejecución

---

## Si $ARGUMENTS es `integration`

Ejecuta el suite completo de pruebas de integración BDD con Python y Behave.

Antes de ejecutar:
1. Verifica que el servidor está corriendo con `docker compose ps`
2. Verifica que el contenedor está healthy antes de continuar
3. Activa el venv del proyecto de integración

Ejecuta:
```bash
cd bdd
.venv/bin/behave features/ --tags="~@requires_network_failure" \
  -f allure_behave.formatter:AllureFormatter -o reports/allure-results
allure serve reports/allure-results
```

Reporta:
- Total de escenarios ejecutados
- Escenarios que pasan y fallan
- Nombre exacto de cada escenario fallido y su razón
- Tiempo de ejecución

---

## Si $ARGUMENTS es `manual`

Ejecuta pruebas manuales con curl basándote en docs/RESTSpec.md.

Reglas:
- Excluye todos los endpoints de JSONPlaceholder — solo prueba los endpoints propios de la API
- Cubre únicamente el happy path de cada endpoint — no hagas pruebas de error
- Máximo un curl por endpoint
- El servidor corre en http://localhost:8080

Antes de ejecutar:
Lee docs/RESTSpec.md para identificar los endpoints propios

Ejecuta un curl por cada endpoint de la tabla REST
Reporta por cada curl:
- Endpoint probado
- HTTP status recibido
- Si el response es el esperado según RESTSpec.md

---

## Si $ARGUMENTS no es ninguno de los anteriores

Responde con:
```
Argumento inválido. Usa: /run-tests unit | integration | manual
```

---


