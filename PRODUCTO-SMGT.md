# SMGT | Base de producto

## 1. Proposito del sistema

SMGT es un sistema de gestion investigativa modular orientado a centralizar el trabajo entre direccion, tutores y estudiantes sobre un tablero principal unico. Su objetivo es ordenar grupos, publicaciones, solicitudes de ingreso, comentarios y metricas de seguimiento en una sola plataforma.

## 2. Vision del producto

La vision de SMGT es convertirse en un espacio simple y visible para gestionar investigacion academica colaborativa, reduciendo la dispersion de informacion y permitiendo que direccion, tutores y estudiantes trabajen sobre una misma fuente de verdad.

## 3. Problema que resuelve

- La informacion investigativa suele quedar dispersa entre planillas, correos y mensajes.
- Los tutores no siempre tienen visibilidad rapida de solicitudes o comentarios pendientes.
- La direccion necesita ver actividad, cobertura y avance sin revisar datos manualmente.
- Los estudiantes necesitan un espacio claro para integrarse a un grupo y publicar avances.

## 4. Propuesta de valor

- Unifica operacion academica y seguimiento en un solo tablero.
- Reduce trabajo manual para coordinacion y tutoria.
- Mejora trazabilidad de publicaciones, comentarios y solicitudes.
- Entrega visibilidad rapida del estado del sistema a la direccion.
- Permite partir con un MVP funcional antes de escalar a una plataforma institucional mas grande.

## 5. Actores del sistema

- Director: administra usuarios, grupos, carga masiva y reportes.
- Tutor: gestiona sus grupos, revisa solicitudes y prioriza comentarios sin respuesta.
- Estudiante: se integra a un grupo, publica avances y participa en el tablero.

## 6. Objetivo del MVP

Validar que un sistema unico puede coordinar gestion investigativa basica entre multiples roles, con base de datos real, tablero visible, seguimiento tutorial y reportes administrativos minimos.

## 7. Alcance del MVP actual

- Login por roles contra base de datos real.
- CRUD de usuarios.
- CRUD de grupos.
- Solicitud de ingreso de estudiantes a grupos.
- Aprobacion o rechazo de solicitudes por tutor.
- Tablero principal tipo Trello por etapas de publicacion.
- Registro de publicaciones con ficha bibliografica APA expandible.
- Comentarios y consejos del tutor destacados visualmente.
- Ranking de puntaje de ayuda.
- Reportes administrativos cuantitativos y cualitativos.
- Carga masiva de alumnos por CSV o TXT.

## 8. Fuera de alcance del MVP

Por ahora el sistema no considera:

- notificaciones push o correo automatico
- autenticacion avanzada con hash de contrasenas o recuperacion de clave
- carga de documentos adjuntos
- flujo documental formal con aprobaciones multi-etapa
- integracion con sistemas externos institucionales
- dashboards de BI avanzados
- auditoria completa por cada accion del sistema

## 9. Product backlog sugerido

### Prioridad alta

1. Como director quiero administrar usuarios para mantener control institucional.
2. Como director quiero administrar grupos para ordenar lineas de investigacion.
3. Como director quiero importar alumnos por archivo para acelerar la carga inicial.
4. Como tutor quiero revisar solicitudes pendientes para decidir ingresos al grupo.
5. Como tutor quiero ver comentarios sin respuesta para priorizar acompanamiento.
6. Como estudiante quiero solicitar ingreso a un grupo para integrarme al proceso investigativo.
7. Como estudiante quiero publicar avances para dejar evidencia en el tablero.
8. Como usuario quiero visualizar publicaciones por etapa para entender el estado del trabajo.

### Prioridad media

9. Como director quiero ver reportes por rol, etapa y linea de investigacion para tomar decisiones.
10. Como tutor quiero editar sus grupos sin salir del dashboard.
11. Como estudiante quiero ver su espacio personal con grupo, puntaje y publicaciones.
12. Como sistema quiero diferenciar visualmente consejos del tutor frente a comentarios comunes.

### Prioridad futura

13. Notificaciones por correo o internas.
14. Bitacora de auditoria por accion.
15. Filtros avanzados por grupo, tutor o linea.
16. Exportacion de reportes.
17. Panel documental con archivos adjuntos.
18. Seguridad reforzada con hash de contrasenas y control de sesion.

## 10. Requerimientos funcionales

### Autenticacion y acceso

- El sistema debe permitir login por correo y clave.
- El sistema debe reconocer roles: DIRECTOR, TUTOR y ESTUDIANTE.
- Cada rol debe ver una vista especializada sin perder el tablero principal.

### Gestion administrativa

- El director debe poder crear, editar y eliminar usuarios.
- El director debe poder crear, editar y eliminar grupos.
- El director debe poder importar alumnos desde CSV o TXT.
- El director debe poder consultar metricas administrativas.

### Gestion de grupos

- El tutor debe poder crear y editar grupos propios.
- Un tutor puede estar a cargo de mas de un grupo al mismo tiempo.
- El sistema debe asignar identidad visual automatica al grupo segun tema de tesis.
- El estudiante puede pertenecer a un solo grupo.

### Flujo investigativo

- El estudiante debe poder solicitar ingreso a un grupo.
- El tutor debe poder aprobar o rechazar solicitudes pendientes.
- El estudiante debe poder publicar una ficha bibliografica.
- El estudiante debe poder aportar publicaciones a otros grupos.
- Las publicaciones realizadas fuera del grupo propio del estudiante deben ingresar en revision.
- El tablero debe ordenar publicaciones por etapa.
- Cada publicacion debe expandirse como ficha APA.

### Interaccion

- Los usuarios autenticados deben poder comentar publicaciones de cualquier grupo.
- Los comentarios del tutor deben verse con estilo destacado.
- El tutor debe visualizar comentarios estudiantiles no atendidos.
- El sistema debe calcular puntaje de ayuda y ranking.

## 11. Requerimientos no funcionales

- Arquitectura modular MVC.
- Backend en Java 21 con Spring Boot.
- Frontend ligero en HTML, CSS y JavaScript vainilla.
- Persistencia sobre MySQL.
- DTOs para desacoplar la base de datos del navegador.
- Diseno responsive y mobile-first.
- Tiempo de carga razonable en redes limitadas.
- Navegacion clara por rol.
- Escalabilidad suficiente para crecer en usuarios, grupos y publicaciones.
- Mantenibilidad del codigo con separacion de capas.

## 12. Reglas de negocio actuales

- Solo el estudiante puede solicitar ingreso a un grupo.
- Un estudiante no puede pertenecer a mas de un grupo.
- Un estudiante no puede mantener multiples solicitudes pendientes activas.
- El tutor solo gestiona sus propios grupos.
- Un tutor puede gestionar varios grupos a la vez.
- El director puede gestionar toda la plataforma.
- El estudiante publica directamente en su grupo y puede aportar a otros grupos con etapa forzada a revision.
- Los comentarios entre grupos estan permitidos para fomentar colaboracion.
- El DOI se registra como texto informativo.
- El tema del estudiante se alinea con su grupo cuando corresponde.

## 13. Indicadores que el sistema debe mostrar

- Total de usuarios por rol.
- Total de grupos activos.
- Total de publicaciones.
- Publicaciones por etapa.
- Estudiantes con grupo y sin grupo.
- Comentarios pendientes de tutor.
- Promedio de puntaje de ayuda.
- Ranking de colaboracion.
- Grupos destacados por actividad.
- Lineas de investigacion predominantes.

## 14. Criterios de exito del MVP

El MVP se considera validado si logra:

- centralizar el flujo basico de al menos director, tutor y estudiante
- permitir gestion operativa sin depender de planillas externas
- mostrar actividad real en el tablero principal
- entregar metricas que permitan a direccion leer estado y carga del sistema
- reducir la incertidumbre del tutor sobre solicitudes y comentarios pendientes

## 15. Criterios de aceptacion por modulo

### Login

- un usuario valido puede iniciar sesion
- un usuario invalido recibe mensaje de error
- el sistema reconoce correctamente el rol del usuario

### Usuarios

- el director puede crear, editar y eliminar usuarios
- no se permite duplicar correo
- un estudiante puede quedar ligado a un grupo

### Grupos

- el director puede administrar todos los grupos
- el tutor puede administrar sus propios grupos
- el grupo recibe color e icono segun su tema

### Solicitudes

- el estudiante puede solicitar ingreso a un grupo
- el tutor puede aprobar o rechazar
- al aprobar, el estudiante queda asignado al grupo

### Publicaciones

- una publicacion se guarda con su etapa y ficha APA
- la publicacion aparece en la columna correcta del tablero

### Comentarios

- usuarios autorizados pueden comentar
- el comentario del tutor se distingue visualmente
- los comentarios pendientes del estudiante pueden ser detectados

### Reportes

- el director puede ver resumen y metricas
- los indicadores reflejan informacion real de la base de datos

## 16. Supuestos de producto

- existe una persona o unidad que cumple el rol de direccion
- cada grupo tiene un tutor responsable
- los estudiantes publican evidencias breves y progresivas
- el DOI no requiere validacion externa en esta etapa
- el sistema parte con volumen bajo o medio y crece por iteraciones

## 17. Riesgos principales

- baja adopcion si los usuarios siguen trabajando fuera del sistema
- datos incompletos si la carga inicial no es de buena calidad
- exceso de trabajo manual si no se prioriza bien la importacion masiva
- problemas de seguridad si el sistema escala sin mejorar autenticacion
- expectativas demasiado altas sobre analitica avanzada en una etapa MVP

## 18. Preguntas abiertas para discovery

Estas son preguntas utiles para levantar con un profesional no tecnico:

- que problema operativo quiere resolver primero
- quienes van a usar el sistema realmente
- que datos son obligatorios y cuales opcionales
- que flujo actual usan hoy para grupos, solicitudes y publicaciones
- que reportes necesita ver para sentir valor
- que significa exito para el proyecto en 1 mes, 3 meses y 6 meses
- que decisiones quiere tomar con los datos del sistema
- que cosas no pueden fallar en la operacion diaria
- que informacion necesita ver cada rol al entrar
- que parte del flujo actual le genera mas friccion hoy

## 19. Artefactos recomendados para una conversacion de producto

- objetivo del sistema en una frase
- alcance del MVP y alcance fuera de MVP
- backlog priorizado
- mockup o recorrido de pantallas
- reglas de negocio
- indicadores esperados
- riesgos y supuestos
- roadmap corto
- dudas abiertas por validar

## 20. Roadmap sugerido

### Iteracion 1

- login real
- CRUD de usuarios
- CRUD de grupos
- tablero visible

### Iteracion 2

- solicitudes de ingreso
- publicaciones APA
- comentarios y consejos del tutor

### Iteracion 3

- reportes administrativos
- ranking de ayuda
- carga masiva

### Iteracion 4

- notificaciones
- exportacion
- seguridad reforzada

## 21. Resumen ejecutivo

SMGT es un MVP funcional de gestion investigativa que ya cubre el flujo minimo entre direccion, tutores y estudiantes sobre una base de datos real. Su valor principal esta en centralizar operacion, seguimiento y visibilidad en un solo tablero. El siguiente paso no es agregar modulos sin criterio, sino validar uso real, mejorar datos, fortalecer seguridad y evolucionar el producto segun necesidades confirmadas del negocio.
