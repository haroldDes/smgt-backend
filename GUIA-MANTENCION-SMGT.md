# SMGT | Guia de mantencion y seguimiento del dato

## 1. Para que sirve esta guia

Esta guia esta pensada para un desarrollador junior o alguien que entra por primera vez al proyecto.

La idea es responder 4 preguntas:

- donde esta cada cosa
- como viaja un dato dentro del sistema
- que archivos tocar segun el cambio que quiero hacer
- como no romper el flujo entre frontend, backend y base de datos

## 2. Antes de tocar el proyecto

La regla mas importante es esta:

- trabaja siempre sobre `src/main/java` y `src/main/resources`
- no tomes `target/` como base para mantener el proyecto

`target/` es codigo compilado o generado. Puede servir para ejecutar, pero no es la fuente real del proyecto.

## 3. Como pensar el sistema en simple

Puedes imaginar SMGT como 3 bloques:

1. `Frontend`
   - lo que el usuario ve y toca
   - HTML, CSS y JavaScript

2. `Backend`
   - decide que hacer con cada accion
   - valida reglas, permisos y construye respuestas

3. `Base de datos`
   - guarda usuarios, grupos, publicaciones, comentarios y solicitudes

En resumen:

```text
Pantalla -> Backend -> Base de datos -> Backend -> Pantalla
```

## 4. Mapa rapido de carpetas

## `src/main/java/cl/smgt`

Aqui vive todo el backend.

### `AplicacionSmgt.java`

- es el punto de arranque de Spring Boot
- si la app inicia, pasa por aqui

### `configuracion/`

- configuraciones globales
- ejemplo: CORS

### `controlador/`

- recibe las peticiones HTTP del frontend
- su trabajo es corto: recibir, mandar al servicio y responder
- no deberia tener mucha logica de negocio

Piensalo asi:

> el controlador es la puerta de entrada

### `servicio/`

- aqui vive la logica importante del sistema
- permisos
- validaciones
- reglas de negocio
- construccion de paneles
- calculo de metricas

Piensalo asi:

> el servicio es el cerebro del sistema

### `repositorio/`

- aqui se consulta y guarda informacion en MySQL
- es la capa que habla con la base de datos

Piensalo asi:

> el repositorio es la mano que toca la base de datos

### `dominio/`

- son las entidades JPA
- representan las tablas reales

Ejemplos:

- `Usuario`
- `GrupoInvestigacion`
- `Publicacion`
- `Comentario`
- `SolicitudIngreso`

Piensalo asi:

> dominio = como se ve la tabla en Java

### `dto/`

- son los objetos que usa el backend para enviar o recibir datos
- ayudan a no mandar entidades completas al frontend

Piensalo asi:

> DTO = formato de intercambio de datos

## `src/main/resources`

Aqui vive configuracion y frontend estatico.

### `application.properties`

- puerto
- conexion a base de datos
- CORS
- variables globales

### `static/index.html`

- estructura principal de la pantalla

### `static/css/panel.css`

- estilos visuales

### `static/js/app.js`

- eventos
- llamadas al backend
- render del dashboard
- armado dinamico de vistas y modulos

### `static/iconos/`

- iconos visuales del sistema

## `ejemplos/`

- SQL de apoyo
- archivos CSV de prueba

## 5. Flujo real de un dato

Si un usuario hace clic en algo, normalmente pasa esto:

1. el usuario interactua en la pantalla
2. `app.js` captura el evento
3. `app.js` llama a un endpoint `/api/...`
4. un controlador recibe la peticion
5. el controlador llama a un servicio
6. el servicio valida reglas y usa repositorios
7. los repositorios consultan o guardan en MySQL
8. el servicio arma la respuesta
9. el backend devuelve un DTO
10. `app.js` renderiza el resultado en la pantalla

## 6. Ejemplo facil: login

Cuando el usuario inicia sesion:

1. escribe correo y clave en la pantalla
2. `app.js` toma esos datos en `manejarLogin()`
3. envia POST a `/api/autenticacion/login`
4. entra en `ControladorAutenticacion`
5. pasa a `ServicioAutenticacion`
6. se consulta `UsuarioRepositorio`
7. si esta correcto, se devuelve `UsuarioSesionDto`
8. el frontend guarda el usuario actual
9. luego pide el panel completo del usuario

Archivos importantes:

- `static/js/app.js`
- `controlador/ControladorAutenticacion.java`
- `servicio/ServicioAutenticacion.java`
- `repositorio/UsuarioRepositorio.java`
- `dto/UsuarioSesionDto.java`

## 7. Ejemplo facil: cargar el panel principal

Despues del login, el frontend pide el panel del usuario.

Ese panel no llega en partes sueltas. El backend arma un solo objeto grande con todo lo necesario.

Eso pasa en:

- `ServicioTablero`

Ese servicio junta:

- usuario en sesion
- ranking
- grupos
- usuarios visibles
- alertas
- solicitudes
- reportes

Y devuelve:

- `PanelUsuarioDto`

Por eso `ServicioTablero` es uno de los archivos mas importantes del proyecto.

## 8. Que hace cada controlador

### `ControladorAutenticacion`

- login

### `ControladorTablero`

- entrega el tablero general con grupos, publicaciones y comentarios
- entrega panel completo del usuario

### `ControladorAdministracion`

- CRUD de usuarios
- CRUD de grupos desde director
- resumen administrativo
- reportes
- importacion de alumnos

### `ControladorGrupos`

- crear grupo como tutor
- editar grupo como tutor
- solicitar ingreso
- aprobar o rechazar solicitudes

### `ControladorPublicaciones`

- crear publicaciones
- agregar comentarios

## 9. Que hace cada servicio

### `ServicioAutenticacion`

- valida correo y clave

### `ServicioTablero`

- construye la vista completa segun el rol

### `ServicioAdministracion`

- maneja usuarios
- genera resumen
- importa alumnos
- entrega reportes del director

### `ServicioGrupoInvestigacion`

- maneja grupos
- maneja solicitudes de ingreso
- aplica reglas del tutor y del estudiante
- permite que un tutor lleve mas de un grupo
- asigna color e icono al grupo segun tema

### `ServicioPublicacion`

- crea publicaciones
- crea comentarios
- valida si una publicacion entra directa o queda en revision segun el grupo destino
- permite comentarios colaborativos entre grupos
- encuentra comentarios pendientes del tutor

### `ServicioReportesAdministracion`

- calcula metricas y lecturas de negocio

### `MapeadorSmgt`

- transforma entidades en DTOs
- si cambias el formato de salida al frontend, casi seguro toca revisar este archivo

## 10. Como saber donde cambiar algo

## Si quieres cambiar texto o etiquetas

Revisa primero:

- `static/index.html`
- `static/js/app.js`

Usa esta regla:

- si el texto esta fijo en la pagina, probablemente esta en `index.html`
- si el texto aparece al renderizar datos, probablemente esta en `app.js`

## Si quieres cambiar colores, espaciado o look visual

Revisa:

- `static/css/panel.css`

## Si quieres cambiar una regla de negocio

Revisa:

- `servicio/`

Ejemplos:

- quien puede publicar
- quien puede comentar
- cuantos puntos gana alguien
- quien puede aprobar algo

## Si quieres cambiar una consulta de datos

Revisa:

- `repositorio/`

Ejemplos:

- traer un filtro nuevo
- ordenar distinto
- contar elementos
- buscar por otro campo

## Si quieres agregar o modificar un campo

Normalmente debes revisar esta cadena:

1. tabla SQL
2. entidad en `dominio/`
3. DTO de entrada o salida
4. servicio
5. mapper
6. frontend

## 11. Recetas rapidas de mantenimiento

## Quiero cambiar el CRUD de usuarios

Revisa:

- `ControladorAdministracion`
- `ServicioAdministracion`
- `UsuarioFormularioDto`
- `UsuarioDto`
- `Usuario`
- `UsuarioRepositorio`
- `MapeadorSmgt`
- `app.js`

## Quiero cambiar el CRUD de grupos

Revisa:

- `ControladorAdministracion`
- `ControladorGrupos`
- `ServicioGrupoInvestigacion`
- `GrupoFormularioDto`
- `GrupoResumenDto`
- `GrupoInvestigacion`
- `GrupoInvestigacionRepositorio`
- `app.js`

## Quiero cambiar publicaciones o comentarios

Revisa:

- `ControladorPublicaciones`
- `ServicioPublicacion`
- `PublicacionCreacionDto`
- `ComentarioCreacionDto`
- `PublicacionDto`
- `ComentarioDto`
- `Publicacion`
- `Comentario`
- `app.js`

## Quiero cambiar reportes o metricas

Revisa:

- `ServicioReportesAdministracion`
- `ReporteAdministracionDto`
- `SerieMetricaDto`
- `GrupoMetricaDto`
- `app.js`

## Quiero cambiar el tablero principal

Revisa:

- `static/index.html`
- `static/css/panel.css`
- `static/js/app.js`

Funciones importantes en `app.js`:

- `renderizar()`
- `renderizarTablero()`
- `renderGrupoTablero()`
- `renderPublicacion()`
- `renderComentario()`

## 12. Ejemplos de seguimiento del dato

## Caso: crear usuario

Ruta del dato:

1. formulario en frontend
2. `app.js -> guardarUsuario()`
3. `/api/administracion/usuarios`
4. `ControladorAdministracion`
5. `ServicioAdministracion.crearUsuario(...)`
6. `UsuarioRepositorio.save(...)`
7. `MapeadorSmgt.aUsuarioDto(...)`
8. respuesta al frontend
9. refresco del panel

## Caso: publicar

Ruta del dato:

1. formulario en frontend
2. `app.js -> guardarPublicacion()`
3. `/api/publicaciones`
4. `ControladorPublicaciones`
5. `ServicioPublicacion.crearPublicacion(...)`
6. validacion del grupo destino y decision de etapa directa o en revision
7. guardado en base de datos
8. respuesta con `PublicacionDto`
9. render en el tablero

## Caso: comentar

Ruta del dato:

1. textarea en frontend
2. `app.js -> guardarComentario()`
3. `/api/publicaciones/{id}/comentarios`
4. `ControladorPublicaciones`
5. `ServicioPublicacion.agregarComentario(...)`
6. si comenta tutor, se pueden marcar pendientes como atendidos
7. guardado en base de datos
8. respuesta con `ComentarioDto`
9. render del comentario en la publicacion

## 13. Checklist al agregar un campo nuevo

Ejemplo: agregar `institucion` a usuario.

Checklist:

1. agregar columna en SQL si corresponde
2. agregar campo en entidad `Usuario`
3. revisar `UsuarioFormularioDto`
4. revisar `UsuarioDto`
5. revisar `UsuarioSesionDto` si aplica
6. revisar `ServicioAdministracion`
7. revisar `MapeadorSmgt`
8. revisar frontend
9. revisar reportes si usan ese dato

## 14. Archivos que normalmente no se tocan primero

### `target/`

- salida generada

### `Dockerfile`

- solo si cambia despliegue

### `config.js`

- solo si cambia la URL del backend en despliegue

### `application.properties`

- solo si cambia puerto, base de datos o configuracion global

## 15. Recomendacion final para no romper el sistema

Cuando hagas un cambio, sigue este orden:

1. entiende si el cambio es visual, de datos o de negocio
2. si toca datos o reglas, empieza por backend
3. luego revisa DTOs y mapper
4. despues ajusta frontend
5. prueba el flujo completo

La regla final es esta:

> si un dato viene de backend, no cambies solo la pantalla.
> si cambias la estructura de un dato, revisa backend, DTOs y frontend juntos.

## 16. Mini glosario para junior

- `Entidad`: clase Java que representa una tabla
- `DTO`: objeto que se usa para enviar o recibir datos
- `Repositorio`: clase/interfaz para consultar o guardar en la base
- `Servicio`: donde vive la logica de negocio
- `Controlador`: recibe una peticion HTTP
- `Renderizar`: dibujar datos en la pantalla
- `Endpoint`: URL de la API
- `JPA/Hibernate`: tecnologia que conecta Java con la base de datos
