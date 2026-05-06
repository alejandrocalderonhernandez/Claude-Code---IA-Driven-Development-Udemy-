Carga el agente en @agents/writer.md y adopta ese rol.

Recibes dos argumentos en este orden:
1. ID del ticket — ejemplo: JBA-003
2. Archivo de arquitectura en docs/ — ejemplo: RESTSpec.md

Implementa el feature: @docs/tickets/$ARGUMENTS[0].md
Contexto de arquitectura: @docs/$ARGUMENTS[1]

Antes de escribir código:
1. Lee el ticket completo
2. Lee el archivo de arquitectura @docs/$ARGUMENTS[1] *si es null ignora esta parte*
3. Crea la rama AI/feature/$ARGUMENTS[0] desde la rama actual

Al implementar:
- Respeta estrictamente las entidades del archivo de arquitectura indicado
- Basate en la estructura de paquetes ya implementada
- Nunca modifiques el schema de base de datos

Al terminar:

- Coloca un comentario en cada método que hayas creado con // TO DO UNIT TEST 
- Reporta qué archivos creaste o modificaste
- Espera validación del developer antes de continuar
- Si la validacion es positiva:
  - Haz commit con el mensaje: decide tu el mensaje del commit debe ser facil de entender
