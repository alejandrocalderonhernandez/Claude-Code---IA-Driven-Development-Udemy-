Carga el agente en @agents/writer.md y adopta ese rol.

Recibes un argumento:
1. ID del ticket — ejemplo: JBA-003

Genera y ejecuta los tests unitarios del feature: @docs/tickets/$ARGUMENTS[0].md

Antes de escribir tests:
1. Lee el ticket completo
2. Busca todos los comentarios // TO DO UNIT TEST en el código del feature
3. Cada comentario marca un método que requiere test — ese es tu contrato
4. Usa la estrategia de Mocking que consideres mejor dependiendo el framework

Al generar los tests:
- Usa BDD con comentarios — Given / When / Then
- Genera al menos un test por cada método marcado con // TO DO UNIT TEST
- Cubre el happy path y todos los paths de error de cada método
- Mockea siempre el servicio externo JSONPlaceholder — nunca lo llames real
- Toda lógica de negocio se testea en la capa de servicio — no en controladores
- Nombra cada test en español — debe leerse como una oración

Al ejecutar:
- Corre los tests y reporta el resultado
- El coverage mínimo aceptable es 80%
- Si el coverage es menor a 80% agrega los tests faltantes y vuelve a correr
- Si algún test falla corrígelo antes de continuar

Al terminar:
- Borra todos los comentarios // TO DO UNIT TEST del código
- Reporta: total de tests, tests que pasan, coverage obtenido
- Espera validación del developer antes de continuar
- Si la validación es positiva:
  - Haz commit con el mensaje: decide tú el mensaje — debe ser fácil de entender