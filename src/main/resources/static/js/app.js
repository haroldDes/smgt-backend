const ETAPAS = ["PROPUESTA", "EN_REVISION"];
const API_BASE_URL = (window.SMGT_API_BASE_URL || "").trim().replace(/\/$/, "");
const HERO_TITULO = "Panel SMGT.";
const HERO_TEXTO = "Grupos, seguimiento y publicaciones en una sola vista.";

const estado = {
    usuario: null,
    panel: null,
    tableroGeneral: [],
    edicionUsuarioId: null,
    edicionGrupoId: null,
    vistaRolActiva: null,
    inicioCarga: Date.now(),
    splashOculto: false
};

document.body.classList.add("carga-activa");

document.addEventListener("DOMContentLoaded", () => {
    sincronizarHeroEstatico();
    document.getElementById("form-login").addEventListener("submit", manejarLogin);
    document.body.addEventListener("click", manejarInteraccionesLayout);
    document.addEventListener("keydown", manejarTeclasLayout);
    document.getElementById("panel-vista-rol").addEventListener("change", manejarCambiosDinamicos);
    document.getElementById("panel-vistas").addEventListener("click", manejarClicksDinamicos);
    document.getElementById("panel-vista-rol").addEventListener("click", manejarClicksDinamicos);
    document.getElementById("panel-vista-rol").addEventListener("submit", manejarFormulariosDinamicos);
    document.getElementById("panel-tablero").addEventListener("click", manejarClicksDinamicos);
    document.getElementById("panel-tablero").addEventListener("submit", manejarFormulariosDinamicos);
    document.getElementById("panel-alertas").addEventListener("click", manejarClicksDinamicos);
    cargarVistaInicial();
});

function sincronizarHeroEstatico() {
    const titulo = document.getElementById("hero-titulo");
    const texto = document.getElementById("hero-texto");

    if (titulo) titulo.textContent = HERO_TITULO;
    if (texto) texto.textContent = HERO_TEXTO;
}

async function cargarVistaInicial() {
    try {
        estado.tableroGeneral = await apiRequest(apiUrl("/api/tablero/grupos"));
        actualizarInsignia("Datos sincronizados", false);
    } catch (error) {
        estado.tableroGeneral = [];
        actualizarInsignia("Sin conexion", true);
        mostrarMensaje("No fue posible cargar el tablero principal.", true);
    }
    renderizar();
    await ocultarPantallaCarga();
}

async function ocultarPantallaCarga() {
    if (estado.splashOculto) return;

    const tiempoMinimo = 900;
    const transcurrido = Date.now() - estado.inicioCarga;
    const esperaRestante = Math.max(0, tiempoMinimo - transcurrido);

    if (esperaRestante > 0) {
        await new Promise((resolve) => window.setTimeout(resolve, esperaRestante));
    }

    const pantalla = document.getElementById("pantalla-carga");
    if (!pantalla) return;

    pantalla.classList.add("pantalla-carga--oculta");
    document.body.classList.remove("carga-activa");
    estado.splashOculto = true;
}

async function manejarLogin(evento) {
    evento.preventDefault();
    const datos = new FormData(evento.currentTarget);
    const correo = (datos.get("correo") || "").toString().trim().toLowerCase();
    const clave = (datos.get("clave") || "").toString().trim();

    try {
        estado.usuario = await apiRequest(apiUrl("/api/autenticacion/login"), {
            method: "POST",
            body: JSON.stringify({ correo, clave })
        });
        estado.edicionUsuarioId = null;
        estado.edicionGrupoId = null;
        estado.vistaRolActiva = obtenerVistaInicialPorRol(estado.usuario.rol);
        await refrescarPanel();
        cerrarPanelLogin();
        mostrarMensaje("Sesion iniciada correctamente.", false);
    } catch (error) {
        if (error.esRed) {
            actualizarInsignia("Sin conexion", true);
            mostrarMensaje("No fue posible conectar con el backend.", true);
            return;
        }
        mostrarMensaje(error.message || "No fue posible iniciar sesion.", true);
    }
}

function cerrarSesion() {
    estado.usuario = null;
    estado.panel = null;
    estado.edicionUsuarioId = null;
    estado.edicionGrupoId = null;
    estado.vistaRolActiva = null;
    document.getElementById("form-login").reset();
    renderizar();
    mostrarMensaje("Sesion cerrada.", false);
}

function manejarInteraccionesLayout(evento) {
    const boton = evento.target.closest("[data-accion]");
    if (!boton) return;

    const accion = boton.dataset.accion;
    if (accion === "abrir-login") {
        abrirPanelLogin();
        return;
    }
    if (accion === "cerrar-login") {
        cerrarPanelLogin();
        return;
    }
    if (accion === "cerrar-sesion") {
        cerrarSesion();
    }
}

function manejarTeclasLayout(evento) {
    if (evento.key === "Escape") {
        cerrarPanelLogin();
    }
}

function manejarCambiosDinamicos(evento) {
    if (evento.target.matches('select[name="rol"]')) {
        sincronizarFormularioUsuario(evento.target.form);
    }
}

function abrirPanelLogin() {
    const panel = document.getElementById("panel-login");
    if (!panel) return;

    panel.classList.remove("oculto");
    panel.setAttribute("aria-hidden", "false");
    document.body.classList.add("carga-activa");
    window.setTimeout(() => panel.querySelector('input[name="correo"]')?.focus(), 40);
}

function cerrarPanelLogin() {
    const panel = document.getElementById("panel-login");
    if (!panel || panel.classList.contains("oculto")) return;

    panel.classList.add("oculto");
    panel.setAttribute("aria-hidden", "true");
    if (estado.splashOculto) {
        document.body.classList.remove("carga-activa");
    }
}

async function refrescarPanel() {
    if (!estado.usuario) {
        estado.panel = null;
        estado.vistaRolActiva = null;
        renderizar();
        return;
    }

    estado.panel = await apiRequest(apiUrl(`/api/tablero/panel/${estado.usuario.id}`));
    asegurarVistaRolActiva();
    renderizar();
}

function asegurarVistaRolActiva() {
    if (!estado.usuario) {
        estado.vistaRolActiva = null;
        return;
    }

    const vistas = obtenerVistasRol().map((vista) => vista.id);
    if (!vistas.includes(estado.vistaRolActiva)) {
        estado.vistaRolActiva = obtenerVistaInicialPorRol(estado.usuario.rol);
    }
}

function obtenerVistaInicialPorRol(rol) {
    if (rol === "DIRECTOR") return "reportes";
    if (rol === "TUTOR") return "seguimiento";
    if (rol === "ESTUDIANTE") return "espacio";
    return null;
}

function obtenerVistasRol() {
    if (!estado.panel?.usuario) return [];

    if (estado.panel.usuario.rol === "DIRECTOR") {
        return [
            { id: "reportes", etiqueta: "Reportes", descripcion: "Metricas y hallazgos" },
            { id: "usuarios", etiqueta: "Usuarios", descripcion: "CRUD de personas" },
            { id: "grupos", etiqueta: "Grupos", descripcion: "Gestion y tablero" },
            { id: "carga", etiqueta: "Carga masiva", descripcion: "Importacion y calidad" }
        ];
    }

    if (estado.panel.usuario.rol === "TUTOR") {
        return [
            { id: "seguimiento", etiqueta: "Seguimiento", descripcion: "Solicitudes y foco" },
            { id: "grupos", etiqueta: "Mis grupos", descripcion: "Edicion y control" },
            { id: "publicaciones", etiqueta: "Publicaciones", descripcion: "Produccion del grupo" }
        ];
    }

    return [
        { id: "espacio", etiqueta: "Mi espacio", descripcion: "Estado y avance" },
        { id: "publicar", etiqueta: "Publicar", descripcion: "Nueva ficha APA" }
    ];
}

function renderizar() {
    renderizarResumenGlobal();
    renderizarSesion();
    renderizarAccesoRapido();
    renderizarInsights();
    renderizarTablero();
    renderizarRanking();
    renderizarAlertas();
    renderizarVistaRol();
}

function renderizarResumenGlobal() {
    const nodo = document.getElementById("panel-resumen-global");

    if (!estado.panel?.usuario) {
        const grupos = obtenerGruposTableroActuales();
        nodo.innerHTML = renderizarTarjetasHero([
            { etiqueta: "Grupos", valor: grupos.length, detalle: "Tableros visibles" },
            { etiqueta: "Publicaciones", valor: contarPublicacionesEnGrupos(grupos), detalle: "Fichas activas" },
            { etiqueta: "Comentarios", valor: contarComentariosEnGrupos(grupos), detalle: "Interacciones registradas" }
        ]);
        return;
    }

    const rol = estado.panel.usuario.rol;
    if (rol === "DIRECTOR") {
        nodo.innerHTML = renderizarTarjetasHero([
            { etiqueta: "Usuarios", valor: estado.panel.resumenAdministracion.totalUsuarios, detalle: "Base activa" },
            { etiqueta: "Grupos", valor: estado.panel.resumenAdministracion.totalGrupos, detalle: "Tableros en marcha" },
            { etiqueta: "Publicaciones", valor: estado.panel.resumenAdministracion.totalPublicaciones, detalle: "Produccion total" }
        ]);
        return;
    }

    if (rol === "TUTOR") {
        nodo.innerHTML = renderizarTarjetasHero([
            { etiqueta: "Mis grupos", valor: estado.panel.grupos.length, detalle: "Espacios a cargo" },
            { etiqueta: "Pendientes", valor: estado.panel.notificacionesTutor.length, detalle: "Solicitudes activas" },
            { etiqueta: "Sin respuesta", valor: estado.panel.comentariosPrioritarios.length, detalle: "Comentarios por revisar" }
        ]);
        return;
    }

    const solicitudPendiente = buscarSolicitudPendienteActual();
    nodo.innerHTML = renderizarTarjetasHero([
        { etiqueta: "Mi grupo", valor: estado.panel.usuario.grupoNombre || "Sin grupo", detalle: "Estado actual" },
        { etiqueta: "Puntaje", valor: buscarPuntajeActual(), detalle: "Ayuda acumulada" },
        { etiqueta: "Solicitud", valor: solicitudPendiente ? "Pendiente" : "Libre", detalle: solicitudPendiente ? solicitudPendiente.grupoNombre : "Puedes postular" }
    ]);
}

function renderizarTarjetasHero(items) {
    return items.map((item) => `
        <div class="resumen-hero__item">
            <span>${esc(item.etiqueta)}</span>
            <strong>${esc(item.valor)}</strong>
            <div class="texto-suave">${esc(item.detalle)}</div>
        </div>
    `).join("");
}

function renderizarSesion() {
    const nodo = document.getElementById("panel-sesion");
    const vistasNodo = document.getElementById("panel-vistas");

    if (!estado.panel?.usuario) {
        vistasNodo.innerHTML = "";
        nodo.innerHTML = `
            <div class="session-card session-card--acceso">
                <div class="session-card__encabezado">
                    <div>
                        <p class="bloque-titulo__kicker">Acceso por rol</p>
                        <h2>Activa tu espacio de trabajo.</h2>
                        <p class="texto-suave">El tablero ya concentra la actividad visible. Inicia sesion para publicar, comentar, aprobar solicitudes o administrar el sistema.</p>
                    </div>
                </div>
                <div class="session-card__resumen">
                    ${renderizarTarjetasKpi([
                        { etiqueta: "Director", valor: "Gestion", detalle: "Usuarios, grupos y reportes" },
                        { etiqueta: "Tutor", valor: "Seguimiento", detalle: "Solicitudes y comentarios" },
                        { etiqueta: "Estudiante", valor: "Publicaciones", detalle: "Aportes y participacion" }
                    ])}
                </div>
            </div>
        `;
        return;
    }

    const usuario = estado.panel.usuario;
    const chips = [
        `<span class="chip">Rol: ${esc(formatearRol(usuario.rol))}</span>`,
        `<span class="chip">Fuente: backend real</span>`
    ];
    if (usuario.grupoNombre) chips.push(`<span class="chip">Grupo: ${esc(usuario.grupoNombre)}</span>`);

    nodo.innerHTML = `
        <div class="session-card">
            <div class="session-card__encabezado">
                <div>
                    <p class="bloque-titulo__kicker">Sesion activa</p>
                    <h2>${esc(usuario.nombre)}</h2>
                    <p class="texto-suave">${esc(usuario.correo)}</p>
                </div>
                <div class="session-chips">${chips.join("")}</div>
            </div>
            <div class="session-card__resumen">
                ${renderizarTarjetasKpi(obtenerKpiSesion())}
            </div>
        </div>
    `;

    vistasNodo.innerHTML = `
        <div class="vista-tabs">
            ${obtenerVistasRol().map((vista) => `
                <button type="button" class="vista-tab ${estado.vistaRolActiva === vista.id ? "vista-tab--activa" : ""}" data-accion="cambiar-vista-rol" data-vista="${vista.id}">
                    <strong>${esc(vista.etiqueta)}</strong>
                    <small>${esc(vista.descripcion)}</small>
                </button>
            `).join("")}
        </div>
    `;
}

function obtenerKpiSesion() {
    if (!estado.panel?.usuario) return [];

    if (estado.panel.usuario.rol === "DIRECTOR") {
        return [
            { etiqueta: "Tutores", valor: estado.panel.resumenAdministracion.totalTutores, detalle: "Roles de acompanamiento" },
            { etiqueta: "Estudiantes", valor: estado.panel.resumenAdministracion.totalEstudiantes, detalle: "Participacion total" },
            { etiqueta: "Solicitudes", valor: estado.panel.resumenAdministracion.totalSolicitudesPendientes, detalle: "Pendientes globales" }
        ];
    }

    if (estado.panel.usuario.rol === "TUTOR") {
        return [
            { etiqueta: "Grupos", valor: estado.panel.grupos.length, detalle: "A cargo del tutor" },
            { etiqueta: "Alumnos", valor: totalAlumnosVisibles(), detalle: "Integrantes visibles" },
            { etiqueta: "Puntaje", valor: buscarPuntajeActual(), detalle: "Ayuda acumulada" }
        ];
    }

    return [
        { etiqueta: "Puntaje", valor: buscarPuntajeActual(), detalle: "Ranking personal" },
        { etiqueta: "Mis publicaciones", valor: contarPublicacionesAutor(estado.usuario.id), detalle: "Fichas creadas" },
        { etiqueta: "Comentarios", valor: contarComentariosAutor(estado.usuario.id), detalle: "Participacion en tablero" }
    ];
}

function renderizarTarjetasKpi(items) {
    return items.map((item) => `
        <div class="kpi-card">
            <span>${esc(item.etiqueta)}</span>
            <strong>${esc(item.valor)}</strong>
            <p>${esc(item.detalle)}</p>
        </div>
    `).join("");
}

function renderizarAccesoRapido() {
    const nodo = document.getElementById("panel-acceso-rapido");

    if (!estado.panel?.usuario) {
        nodo.innerHTML = `
            <div class="session-card session-card--acceso">
                <div>
                    <p class="bloque-titulo__kicker">Acceso</p>
                    <h2>Inicio de sesion</h2>
                    <p class="texto-suave">Abre el acceso cuando quieras entrar. El tablero sigue visible y el flujo principal no se interrumpe.</p>
                </div>
                <div class="session-card__acciones">
                    <button type="button" class="boton boton--primario" data-accion="abrir-login">Ingresar</button>
                </div>
            </div>
        `;
        return;
    }

    const usuario = estado.panel.usuario;
    nodo.innerHTML = `
        <div class="session-card session-card--acceso">
            <div>
                <p class="bloque-titulo__kicker">Sesion activa</p>
                <h2>${esc(usuario.nombre)}</h2>
                <p class="texto-suave">${esc(formatearRol(usuario.rol))}${usuario.grupoNombre ? ` | ${esc(usuario.grupoNombre)}` : ""}</p>
            </div>
            <div class="session-card__acciones">
                <button type="button" class="boton boton--secundario" data-accion="abrir-login">Cambiar usuario</button>
                <button type="button" class="boton boton--peligro" data-accion="cerrar-sesion">Cerrar sesion</button>
            </div>
        </div>
    `;
}

function renderizarInsights() {
    const nodo = document.getElementById("panel-insights");

    if (!estado.panel?.usuario) {
        const grupos = obtenerGruposTableroActuales();
        const grupoActivo = obtenerGrupoMasActivo(grupos);
        const etapaActiva = obtenerEtapaDominante(grupos);
        nodo.innerHTML = `
            <div class="item-lista">
                <p><strong>Grupos con produccion</strong></p>
                <p class="texto-suave">${grupos.filter((grupo) => grupo.publicaciones.length > 0).length} grupos muestran publicaciones visibles.</p>
            </div>
            <div class="item-lista">
                <p><strong>Etapa dominante</strong></p>
                <p class="texto-suave">${etapaActiva ? `${formatearEtapa(etapaActiva.etapa)} (${etapaActiva.total})` : "Todavia no hay publicaciones registradas."}</p>
            </div>
            <div class="item-lista">
                <p><strong>Grupo mas activo</strong></p>
                <p class="texto-suave">${grupoActivo ? `${grupoActivo.grupo.nombre} con ${grupoActivo.publicaciones.length} publicaciones.` : "Sin actividad suficiente para destacar un grupo."}</p>
            </div>
        `;
        return;
    }

    if (estado.panel.usuario.rol === "DIRECTOR") {
        const reporte = estado.panel.reporteAdministracion;
        const linea = reporte?.lineasInvestigacion?.[0];
        nodo.innerHTML = `
            <div class="item-lista">
                <p><strong>Promedio de ayuda</strong></p>
                <p class="texto-suave">${reporte ? reporte.promedioPuntajeAyuda : 0} pts por usuario.</p>
            </div>
            <div class="item-lista">
                <p><strong>Publicaciones por grupo</strong></p>
                <p class="texto-suave">${reporte ? reporte.promedioPublicacionesPorGrupo : 0} fichas en promedio.</p>
            </div>
            <div class="item-lista">
                <p><strong>Linea dominante</strong></p>
                <p class="texto-suave">${linea ? `${linea.etiqueta} (${linea.valor})` : "Sin datos suficientes"}.</p>
            </div>
        `;
        return;
    }

    if (estado.panel.usuario.rol === "TUTOR") {
        nodo.innerHTML = `
            <div class="item-lista">
                <p><strong>Grupos visibles</strong></p>
                <p class="texto-suave">${estado.panel.grupos.length} grupos en tu panel de trabajo.</p>
            </div>
            <div class="item-lista">
                <p><strong>Solicitudes abiertas</strong></p>
                <p class="texto-suave">${estado.panel.notificacionesTutor.length} alumnos esperan respuesta.</p>
            </div>
            <div class="item-lista">
                <p><strong>Comentarios prioritarios</strong></p>
                <p class="texto-suave">${estado.panel.comentariosPrioritarios.length} comentarios estudiantiles siguen pendientes.</p>
            </div>
        `;
        return;
    }

    const solicitudPendiente = buscarSolicitudPendienteActual();
    nodo.innerHTML = `
        <div class="item-lista">
            <p><strong>Estado del grupo</strong></p>
            <p class="texto-suave">${esc(estado.panel.usuario.grupoNombre || "Todavia no perteneces a un grupo.")}</p>
        </div>
        <div class="item-lista">
            <p><strong>Solicitud activa</strong></p>
            <p class="texto-suave">${solicitudPendiente ? `Pendiente en ${solicitudPendiente.grupoNombre}.` : "No tienes solicitudes pendientes."}</p>
        </div>
        <div class="item-lista">
            <p><strong>Produccion personal</strong></p>
            <p class="texto-suave">${contarPublicacionesAutor(estado.usuario.id)} publicaciones registradas.</p>
        </div>
    `;
}

function renderizarVistaRol() {
    const nodo = document.getElementById("panel-vista-rol");

    if (!estado.panel?.usuario) {
        nodo.classList.add("oculto");
        nodo.innerHTML = "";
        return;
    }

    nodo.classList.remove("oculto");

    if (estado.panel.usuario.rol === "DIRECTOR") {
        nodo.innerHTML = renderizarVistaDirector();
        return;
    }

    if (estado.panel.usuario.rol === "TUTOR") {
        nodo.innerHTML = renderizarVistaTutor();
        return;
    }

    nodo.innerHTML = renderizarVistaEstudiante();
}

function renderizarVistaDirector() {
    if (estado.vistaRolActiva === "usuarios") return renderizarVistaDirectorUsuarios();
    if (estado.vistaRolActiva === "grupos") return renderizarVistaDirectorGrupos();
    if (estado.vistaRolActiva === "carga") return renderizarVistaDirectorCarga();
    return renderizarVistaDirectorReportes();
}

function renderizarVistaDirectorReportes() {
    const reporte = estado.panel.reporteAdministracion;
    if (!reporte) {
        return `${renderizarCabeceraModulo("Reportes", "No hay metricas disponibles", "Inicia el tablero con datos reales para construir el panel administrativo.")}`;
    }

    return `
        ${renderizarCabeceraModulo("Reportes", "Panel de metricas administrativas", "Lectura rapida de volumen, actividad, saturacion tutorial y lineas de investigacion predominantes.")}
        <div class="kpi-grid">
            ${renderizarTarjetasKpi([
                { etiqueta: "Comentarios", valor: reporte.totalComentarios, detalle: "Interacciones totales" },
                { etiqueta: "Pendientes tutor", valor: reporte.comentariosPendientesTutor, detalle: "Sin respuesta aun" },
                { etiqueta: "Estudiantes con grupo", valor: reporte.estudiantesConGrupo, detalle: "Vinculados al tablero" },
                { etiqueta: "Estudiantes sin grupo", valor: reporte.estudiantesSinGrupo, detalle: "Oportunidad de captacion" },
                { etiqueta: "Tutores activos", valor: reporte.tutoresConGrupos, detalle: "Con grupos asignados" },
                { etiqueta: "Promedio ayuda", valor: reporte.promedioPuntajeAyuda, detalle: "Puntaje por usuario" }
            ])}
        </div>
        <div class="section-grid section-grid--dos">
            ${renderizarPanelSerie("Usuarios por rol", "Distribucion estructural del sistema.", reporte.usuariosPorRol)}
            ${renderizarPanelSerie("Publicaciones por etapa", "Distingue publicaciones visibles y aportes que aun requieren revision.", reporte.publicacionesPorEtapa)}
            ${renderizarPanelSerie("Lineas de investigacion", "Lectura cualitativa a partir de temas de tesis y grupos.", reporte.lineasInvestigacion)}
            <section class="panel-seccion">
                <h3>Lectura rapida</h3>
                <p class="texto-suave">Publicaciones por grupo: ${reporte.promedioPublicacionesPorGrupo}</p>
                <p class="texto-suave">Comentarios pendientes del tutor: ${reporte.comentariosPendientesTutor}</p>
                <p class="texto-suave">Cobertura de estudiantes con grupo: ${porcentaje(reporte.estudiantesConGrupo, reporte.estudiantesConGrupo + reporte.estudiantesSinGrupo)}%</p>
            </section>
        </div>
        <section class="panel-seccion">
            <h3>Grupos destacados por actividad</h3>
            <p class="texto-suave">Se priorizan publicaciones, comentarios, estudiantes y solicitudes pendientes.</p>
            <div class="stack-list">
                ${reporte.gruposDestacados.map((grupo) => `
                    <article class="stack-item">
                        <div class="stack-item__row">
                            <div>
                                <strong>${esc(grupo.nombre)}</strong>
                                <p class="texto-suave">Tutor: ${esc(grupo.tutorNombre)}</p>
                            </div>
                            <span class="tag tag--ok">${esc(grupo.lineaInvestigacion)}</span>
                        </div>
                        <div class="stack-item__meta">
                            <span class="chip">Alumnos: ${grupo.totalAlumnos}</span>
                            <span class="chip">Publicaciones: ${grupo.totalPublicaciones}</span>
                            <span class="chip">Comentarios: ${grupo.totalComentarios}</span>
                            <span class="chip">Pendientes: ${grupo.totalSolicitudesPendientes}</span>
                        </div>
                    </article>
                `).join("")}
            </div>
        </section>
    `;
}

function renderizarVistaDirectorUsuarios() {
    const usuarioEdicion = estado.edicionUsuarioId ? estado.panel.usuarios.find((usuario) => usuario.id === estado.edicionUsuarioId) : null;
    const rolFormulario = usuarioEdicion?.rol || "ESTUDIANTE";
    const mostrarGrupo = rolFormulario === "ESTUDIANTE";

    return `
        ${renderizarCabeceraModulo("Administracion", "CRUD de usuarios y roles", "El director crea, edita o elimina perfiles sin salir del dashboard.")}
        <div class="section-grid section-grid--dos">
            <section class="panel-seccion">
                <h3>${usuarioEdicion ? "Editar usuario" : "Nuevo usuario"}</h3>
                <p class="form-hint">La clave solo es obligatoria al crear. Al editar puedes dejarla vacia para conservar la actual.</p>
                <form id="form-usuario" class="formulario-grid--doble" data-accion="guardar-usuario">
                    <input type="hidden" name="id" value="${usuarioEdicion?.id || ""}">
                    <label><span>Nombre</span><input name="nombre" value="${escAttr(usuarioEdicion?.nombre)}" required></label>
                    <label><span>Correo</span><input name="correo" type="email" value="${escAttr(usuarioEdicion?.correo)}" required></label>
                    <label><span>Clave</span><input name="clave" value="" placeholder="${usuarioEdicion ? "Opcional al editar" : "clave123"}"></label>
                    <label><span>Rol</span><select name="rol">${["DIRECTOR", "TUTOR", "ESTUDIANTE"].map((rol) => `<option value="${rol}" ${rolFormulario === rol ? "selected" : ""}>${formatearRol(rol)}</option>`).join("")}</select></label>
                    <label id="campo-grupo-usuario" class="${mostrarGrupo ? "" : "oculto"}"><span>Grupo del estudiante</span><select name="grupoId"><option value="">Sin grupo</option>${estado.panel.grupos.map((grupo) => `<option value="${grupo.grupo.id}" ${usuarioEdicion?.grupoId === grupo.grupo.id ? "selected" : ""}>${esc(grupo.grupo.nombre)}</option>`).join("")}</select></label>
                    <div class="formulario-acciones">
                        <button type="submit" class="boton boton--primario">${usuarioEdicion ? "Actualizar usuario" : "Crear usuario"}</button>
                        ${usuarioEdicion ? '<button type="button" class="boton boton--secundario" data-accion="cancelar-edicion-usuario">Cancelar</button>' : ""}
                    </div>
                </form>
            </section>
            <section class="panel-seccion">
                <h3>Directorio del sistema</h3>
                <div class="stack-list">
                    ${estado.panel.usuarios.map((usuario) => `
                        <article class="stack-item">
                            <div class="stack-item__row">
                                <div>
                                    <strong>${esc(usuario.nombre)}</strong>
                                    <p class="texto-suave">${esc(usuario.correo)}</p>
                                </div>
                                <span class="tag">${esc(formatearRol(usuario.rol))}</span>
                            </div>
                            <div class="stack-item__meta">
                                <span class="chip">Puntaje: ${usuario.puntajeAyuda ?? 0}</span>
                                <span class="chip">${esc(usuario.grupoNombre || "Sin grupo")}</span>
                            </div>
                            <p class="texto-suave">${esc(usuario.temaTesis || "Sin tema de tesis registrado.")}</p>
                            <div class="acciones-en-linea">
                                <button type="button" class="boton boton--suave" data-accion="editar-usuario" data-id="${usuario.id}">Editar</button>
                                <button type="button" class="boton boton--peligro" data-accion="eliminar-usuario" data-id="${usuario.id}">Eliminar</button>
                            </div>
                        </article>
                    `).join("")}
                </div>
            </section>
        </div>
    `;
}

function renderizarVistaDirectorGrupos() {
    return `
        ${renderizarCabeceraModulo("Grupos", "CRUD de grupos investigativos", "La identidad visual se asigna automaticamente a partir del tema de tesis.")}
        ${renderizarModuloGrupos(true)}
    `;
}

function renderizarVistaDirectorCarga() {
    const reporte = estado.panel.reporteAdministracion;
    return `
        ${renderizarCabeceraModulo("Carga masiva", "Importacion de alumnos y lectura de calidad", "Usa CSV o TXT para poblar estudiantes y luego observa cobertura, distribucion y carga pendiente.")}
        <div class="section-grid section-grid--dos">
            <section class="panel-seccion">
                <h3>Importar alumnos</h3>
                <p class="form-hint">Columnas recomendadas: nombre, correo, temaTesis, clave. El sistema omite correos ya existentes.</p>
                <form id="form-importacion" class="formulario-grid" data-accion="importar-alumnos">
                    <label><span>Archivo CSV o TXT</span><input type="file" name="archivo" accept=".csv,.txt" required></label>
                    <div class="formulario-acciones">
                        <button type="submit" class="boton boton--primario">Importar alumnos</button>
                    </div>
                </form>
            </section>
            <section class="panel-seccion">
                <h3>Calidad del tablero</h3>
                <div class="kpi-grid">
                    ${renderizarTarjetasKpi([
                        { etiqueta: "Sin grupo", valor: reporte?.estudiantesSinGrupo ?? 0, detalle: "Estudiantes por asignar" },
                        { etiqueta: "Con grupo", valor: reporte?.estudiantesConGrupo ?? 0, detalle: "Cobertura actual" },
                        { etiqueta: "Promedio ayuda", valor: reporte?.promedioPuntajeAyuda ?? 0, detalle: "Participacion general" }
                    ])}
                </div>
            </section>
        </div>
    `;
}

function renderizarVistaTutor() {
    if (estado.vistaRolActiva === "grupos") {
        return `
            ${renderizarCabeceraModulo("Tutor", "Gestion de mis grupos", "Edita tus grupos y revisa su composicion sin salir del tablero principal.")}
            ${renderizarModuloGrupos(false)}
        `;
    }

    if (estado.vistaRolActiva === "publicaciones") {
        return `
            ${renderizarCabeceraModulo("Tutor", "Produccion y acompanamiento", "Publica documentos de referencia y revisa el balance por etapa en tus grupos.")}
            <div class="section-grid section-grid--dos">
                ${renderizarFormularioPublicacion(obtenerGruposPublicables(), "Nueva publicacion", "Crea fichas APA para cualquiera de tus grupos.")}
            ${renderizarPanelSerie("Balance por etapa", "Lectura agregada de propuestas visibles y aportes en revision.", construirSerieEtapas(estado.panel.grupos))}
            </div>
        `;
    }

    return `
        ${renderizarCabeceraModulo("Tutor", "Seguimiento prioritario", "Tus solicitudes y comentarios sin responder quedan visibles antes que cualquier otra accion.")}
        <div class="kpi-grid">
            ${renderizarTarjetasKpi([
                { etiqueta: "Grupos", valor: estado.panel.grupos.length, detalle: "Espacios a cargo" },
                { etiqueta: "Solicitudes", valor: estado.panel.notificacionesTutor.length, detalle: "Esperan decision" },
                { etiqueta: "Comentarios", valor: estado.panel.comentariosPrioritarios.length, detalle: "Sin respuesta" },
                { etiqueta: "Alumnos", valor: totalAlumnosVisibles(), detalle: "Integrantes visibles" }
            ])}
        </div>
        <div class="section-grid section-grid--dos">
            <section class="panel-seccion">
                <h3>Solicitudes de ingreso</h3>
                <div class="stack-list">
                    ${renderizarSolicitudes(estado.panel.notificacionesTutor, true)}
                </div>
            </section>
            <section class="panel-seccion">
                <h3>Comentarios prioritarios</h3>
                <div class="stack-list">
                    ${renderizarComentariosPrioritarios(estado.panel.comentariosPrioritarios)}
                </div>
            </section>
        </div>
        <div class="section-grid">
            ${renderizarFormularioPublicacion(obtenerGruposPublicables(), "Publicacion rapida", "Deja una nueva ficha APA sin cambiar de modulo.")}
        </div>
    `;
}

function renderizarVistaEstudiante() {
    if (estado.vistaRolActiva === "publicar") {
        return `
            ${renderizarCabeceraModulo("Estudiante", "Publicar en el tablero", "Tu aporte queda ordenado por etapa y se expande como ficha APA dentro del tablero principal.")}
            ${renderizarFormularioPublicacion(obtenerGruposPublicables(), "Nueva ficha APA", "Solo puedes publicar en tu grupo actual.")}
        `;
    }

    const solicitudPendiente = buscarSolicitudPendienteActual();
    const companeros = estado.panel.usuario.grupoId
        ? estado.panel.usuarios.filter((usuario) => usuario.id !== estado.usuario.id)
        : [];

    return `
        ${renderizarCabeceraModulo("Estudiante", "Mi espacio de avance", "Aqui ves tu estado actual, la situacion de tu grupo y el siguiente paso recomendado.")}
        <div class="kpi-grid">
            ${renderizarTarjetasKpi([
                { etiqueta: "Mi grupo", valor: estado.panel.usuario.grupoNombre || "Sin grupo", detalle: "Pertenencia actual" },
                { etiqueta: "Puntaje", valor: buscarPuntajeActual(), detalle: "Ayuda acumulada" },
                { etiqueta: "Mis publicaciones", valor: contarPublicacionesAutor(estado.usuario.id), detalle: "Fichas creadas" },
                { etiqueta: "Solicitud", valor: solicitudPendiente ? "Pendiente" : "Sin tramite", detalle: solicitudPendiente ? solicitudPendiente.grupoNombre : "Puedes postular" }
            ])}
        </div>
        <div class="section-grid section-grid--dos">
            <section class="panel-seccion">
                <h3>Estado actual</h3>
                <p class="texto-suave">
                    ${estado.panel.usuario.grupoNombre
                        ? `Ya perteneces a ${esc(estado.panel.usuario.grupoNombre)}. Publica, comenta y sigue el tablero de tu grupo.`
                        : solicitudPendiente
                            ? `Tu solicitud a ${esc(solicitudPendiente.grupoNombre)} sigue pendiente de respuesta.`
                            : "Todavia no perteneces a un grupo. Usa el tablero principal para postular a uno."}
                </p>
            </section>
            <section class="panel-seccion">
                <h3>Companeros visibles</h3>
                <div class="stack-list">
                    ${companeros.length
                        ? companeros.map((usuario) => `
                            <article class="stack-item">
                                <strong>${esc(usuario.nombre)}</strong>
                                <p class="texto-suave">${esc(usuario.correo)}</p>
                                <p class="texto-suave">${esc(usuario.temaTesis || "Sin tema de tesis registrado.")}</p>
                            </article>
                        `).join("")
                        : '<div class="empty">Todavia no hay companeros visibles en tu panel actual.</div>'}
                </div>
            </section>
        </div>
    `;
}

function renderizarCabeceraModulo(kicker, titulo, descripcion) {
    return `
        <div class="bloque-titulo">
            <div>
                <p class="bloque-titulo__kicker">${esc(kicker)}</p>
                <h2>${esc(titulo)}</h2>
                <p class="texto-suave">${esc(descripcion)}</p>
            </div>
        </div>
    `;
}

function renderizarPanelSerie(titulo, descripcion, series) {
    return `
        <section class="panel-seccion">
            <h3>${esc(titulo)}</h3>
            <p class="texto-suave">${esc(descripcion)}</p>
            ${renderizarSerieMetricas(series)}
        </section>
    `;
}

function renderizarSerieMetricas(series) {
    if (!series?.length) {
        return '<div class="empty">Sin datos para esta lectura.</div>';
    }

    const maximo = Math.max(...series.map((serie) => serie.valor), 1);
    return `
        <div class="metric-bars">
            ${series.map((serie) => `
                <div class="metric-bar">
                    <div class="metric-bar__cabecera">
                        <div>
                            <strong>${esc(serie.etiqueta)}</strong>
                            <div class="texto-suave">${esc(serie.descripcion || "")}</div>
                        </div>
                        <strong>${serie.valor}</strong>
                    </div>
                    <div class="metric-bar__track">
                        <div class="metric-bar__fill" style="width:${(serie.valor / maximo) * 100}%"></div>
                    </div>
                </div>
            `).join("")}
        </div>
    `;
}

function renderizarModuloGrupos(esDirector) {
    const grupoEdicion = estado.edicionGrupoId ? estado.panel.grupos.find((grupo) => grupo.grupo.id === estado.edicionGrupoId)?.grupo : null;
    const tutores = estado.panel.usuarios.filter((usuario) => usuario.rol === "TUTOR");

    return `
        <div class="section-grid section-grid--dos">
            <section class="panel-seccion">
                <h3>${grupoEdicion ? "Editar grupo" : "Nuevo grupo"}</h3>
                <p class="form-hint">Los colores e iconos cambian automaticamente segun palabras clave del tema de tesis.</p>
                <form id="form-grupo" class="formulario-grid--doble" data-accion="guardar-grupo">
                    <input type="hidden" name="id" value="${grupoEdicion?.id || ""}">
                    <label><span>Nombre del grupo</span><input name="nombre" value="${escAttr(grupoEdicion?.nombre)}" required></label>
                    <label><span>Tema de tesis</span><input name="temaTesis" value="${escAttr(grupoEdicion?.temaTesis)}" required></label>
                    <label><span>Descripcion</span><textarea name="descripcion">${esc(grupoEdicion?.descripcion || "")}</textarea></label>
                    ${esDirector ? `<label><span>Tutor responsable</span><select name="tutorId" required><option value="">Seleccione</option>${tutores.map((tutor) => `<option value="${tutor.id}" ${grupoEdicion?.tutorId === tutor.id ? "selected" : ""}>${esc(tutor.nombre)}</option>`).join("")}</select></label>` : ""}
                    <div class="formulario-acciones">
                        <button type="submit" class="boton boton--primario">${grupoEdicion ? "Actualizar grupo" : "Crear grupo"}</button>
                        ${grupoEdicion ? '<button type="button" class="boton boton--secundario" data-accion="cancelar-edicion-grupo">Cancelar</button>' : ""}
                    </div>
                </form>
            </section>
            <section class="panel-seccion">
                <h3>${esDirector ? "Todos los grupos" : "Mis grupos"}</h3>
                <div class="stack-list">
                    ${estado.panel.grupos.map((grupo) => `
                        <article class="stack-item">
                            <div class="stack-item__row">
                                <div>
                                    <strong>${esc(grupo.grupo.nombre)}</strong>
                                    <p class="texto-suave">${esc(grupo.grupo.temaTesis)}</p>
                                </div>
                                <span class="tag">${esc(grupo.grupo.tutorNombre)}</span>
                            </div>
                            <div class="stack-item__meta">
                                <span class="chip">Alumnos: ${grupo.grupo.totalAlumnos}</span>
                                <span class="chip">Solicitudes: ${grupo.grupo.totalSolicitudesPendientes}</span>
                                <span class="chip">Publicaciones: ${grupo.publicaciones.length}</span>
                            </div>
                            <p class="texto-suave">${esc(grupo.grupo.descripcion || "Sin descripcion registrada.")}</p>
                            <div class="acciones-en-linea">
                                <button type="button" class="boton boton--suave" data-accion="editar-grupo" data-id="${grupo.grupo.id}">Editar</button>
                                ${esDirector ? `<button type="button" class="boton boton--peligro" data-accion="eliminar-grupo" data-id="${grupo.grupo.id}">Eliminar</button>` : ""}
                            </div>
                        </article>
                    `).join("")}
                </div>
            </section>
        </div>
    `;
}

function renderizarFormularioPublicacion(gruposParaPublicar, titulo, descripcion) {
    if (!gruposParaPublicar.length) {
        return `
            <section class="panel-seccion">
                <h3>${esc(titulo)}</h3>
                <p class="texto-suave">${esc(descripcion)}</p>
                <div class="empty">No hay grupos disponibles para publicar con tu rol actual.</div>
            </section>
        `;
    }

    return `
        <section class="panel-seccion">
            <h3>${esc(titulo)}</h3>
            <p class="texto-suave">${esc(descripcion)}</p>
            ${estado.panel?.usuario?.rol === "ESTUDIANTE"
                ? '<p class="form-hint">Tu grupo publica directo en su tablero. Si aportas a otro grupo, la publicacion entra automaticamente en revision.</p>'
                : ""}
            <form id="form-publicacion" class="formulario-grid--doble" data-accion="guardar-publicacion">
                <input type="hidden" name="etapa" value="PROPUESTA">
                <label><span>Grupo</span><select name="grupoId" required>${gruposParaPublicar.map((grupo) => `<option value="${grupo.id}">${esc(grupo.nombre)}</option>`).join("")}</select></label>
                <label><span>Titulo</span><input name="titulo" required></label>
                <label><span>Autor</span><input value="${escAttr(estado.panel?.usuario?.nombre)}" disabled></label>
                <label><span>Resumen</span><textarea name="resumen" required></textarea></label>
                <label><span>Anio</span><input type="number" name="anio" min="2000" max="2100" placeholder="${new Date().getFullYear()}" required></label>
                <label><span>Ciudad</span><input name="ciudad" placeholder="Ciudad de publicacion" required></label>
                <label><span>Editorial</span><input name="editorial" placeholder="Editorial o entidad" required></label>
                <label><span>DOI</span><input name="doi" placeholder="Texto informativo"></label>
                <label><span>Enlace</span><input name="enlace" type="url" placeholder="https://..."></label>
                <div class="formulario-acciones">
                    <button type="submit" class="boton boton--primario">Publicar</button>
                </div>
            </form>
        </section>
    `;
}

function renderizarSolicitudes(solicitudes, conAcciones) {
    if (!solicitudes?.length) {
        return '<div class="empty">No hay solicitudes pendientes en este momento.</div>';
    }

    return solicitudes.map((solicitud) => `
        <article class="stack-item">
            <strong>${esc(solicitud.estudianteNombre)}</strong>
            <p class="texto-suave">Quiere ingresar a ${esc(solicitud.grupoNombre)}.</p>
            <p class="texto-suave">Fecha: ${esc(solicitud.fechaSolicitud)}</p>
            ${conAcciones ? `
                <div class="acciones-en-linea">
                    <button type="button" class="boton boton--suave" data-accion="resolver-solicitud" data-id="${solicitud.id}" data-aprobar="true">Aprobar</button>
                    <button type="button" class="boton boton--peligro" data-accion="resolver-solicitud" data-id="${solicitud.id}" data-aprobar="false">Rechazar</button>
                </div>
            ` : ""}
        </article>
    `).join("");
}

function renderizarComentariosPrioritarios(comentarios) {
    if (!comentarios?.length) {
        return '<div class="empty">No hay comentarios prioritarios pendientes.</div>';
    }

    return comentarios.map((comentario) => `
        <article class="stack-item">
            <strong>${esc(comentario.estudianteNombre)}</strong>
            <p class="texto-suave">${esc(comentario.publicacionTitulo)} | ${esc(comentario.grupoNombre)}</p>
            <p>${esc(comentario.mensaje)}</p>
            <p class="texto-suave">${esc(comentario.fechaCreacion)}</p>
        </article>
    `).join("");
}

function renderizarTablero() {
    const nodo = document.getElementById("panel-tablero");
    const grupos = obtenerGruposTableroActuales();

    nodo.innerHTML = `
        ${renderizarCabeceraModulo(
            "Tablero principal",
            estado.panel?.usuario ? "Tablero de publicaciones" : "Vista general de publicaciones",
            estado.panel?.usuario
                ? "Este es el espacio principal de trabajo para grupos, etapas, comentarios y seguimiento."
                : "El tablero muestra actividad real por grupo y por etapa. Las acciones operativas se habilitan al iniciar sesion."
        )}
        <div class="kpi-grid">
            ${renderizarTarjetasKpi([
                { etiqueta: "Grupos visibles", valor: grupos.length, detalle: "Tarjetas principales" },
                { etiqueta: "Columnas", valor: ETAPAS.length, detalle: "Etapas del tablero" },
                { etiqueta: "Publicaciones", valor: grupos.reduce((total, grupo) => total + grupo.publicaciones.length, 0), detalle: "Fichas APA visibles" }
            ])}
        </div>
        <div class="tablero">
            ${grupos.length ? grupos.map(renderGrupoTablero).join("") : '<div class="empty">No hay grupos para mostrar.</div>'}
        </div>
    `;
}

function renderGrupoTablero(grupoTablero) {
    const grupo = grupoTablero.grupo;
    const color = grupo.colorHex || "#2563eb";
    const solicitudes = puedeVerSolicitudes() ? grupoTablero.solicitudesPendientes : [];
    const permitirSolicitud = puedeSolicitarGrupo(grupo.id);
    const expandido = debeMostrarGrupoExpandido(grupo.id);
    const mostrarToggle = estado.panel?.usuario?.rol === "ESTUDIANTE";
    const cabecera = `
        <div class="grupo-card__cabecera">
            <div class="grupo-card__top">
                <div class="grupo-card__principal">
                    <img class="grupo-card__icono" src="${escAttr(grupo.icono)}" alt="">
                    <div>
                        <h3>${esc(grupo.nombre)}</h3>
                        <p class="texto-suave">${esc(grupo.temaTesis)}</p>
                    </div>
                </div>
            </div>
            <div class="grupo-card__meta">
                <span class="chip">Tutor: ${esc(grupo.tutorNombre)}</span>
                <span class="chip">Alumnos: ${grupo.totalAlumnos}</span>
                <span class="chip">Pendientes: ${grupo.totalSolicitudesPendientes}</span>
            </div>
            <div class="resumen-etapas">
                ${ETAPAS.map((etapa) => `<span class="chip">${formatearEtapa(etapa)}: ${grupoTablero.publicaciones.filter((publicacion) => normalizarEtapaVisible(publicacion.etapa) === etapa).length}</span>`).join("")}
            </div>
        </div>
    `;
    const contenido = `
        <p class="grupo-card__descripcion">${esc(grupo.descripcion || "Sin descripcion registrada para este grupo.")}</p>
        ${permitirSolicitud ? `
            <div class="acciones-en-linea">
                <button type="button" class="boton boton--primario" data-accion="solicitar-ingreso" data-grupo-id="${grupo.id}">Solicitar ingreso</button>
            </div>
        ` : ""}
        ${solicitudes.length ? `
            <section class="panel-seccion">
                <h4>Solicitudes visibles</h4>
                <div class="stack-list">${renderizarSolicitudes(solicitudes, true)}</div>
            </section>
            <div class="barra-separadora"></div>
        ` : ""}
        <div class="tablero__columnas">
            ${ETAPAS.map((etapa) => renderColumnaEtapa(grupoTablero, etapa)).join("")}
        </div>
    `;

    if (!mostrarToggle) {
        return `
            <section class="grupo-card" style="--grupo-color:${escAttr(color)};">
                ${cabecera}
                <div class="grupo-card__contenido">
                    ${contenido}
                </div>
            </section>
        `;
    }

    return `
        <details class="grupo-card ${mostrarToggle ? "grupo-card--plegable" : ""}" style="--grupo-color:${escAttr(color)};" ${expandido ? "open" : ""}>
            <summary class="grupo-card__resumen">
                ${cabecera}
                <span class="grupo-card__toggle" aria-hidden="true"></span>
            </summary>
            <div class="grupo-card__contenido">
                ${contenido}
            </div>
        </details>
    `;
}

function renderColumnaEtapa(grupoTablero, etapa) {
    const items = grupoTablero.publicaciones.filter((publicacion) => normalizarEtapaVisible(publicacion.etapa) === etapa);
    return `
        <article class="columna-tablero">
            <div class="columna-tablero__cabecera">
                <h4>${formatearEtapa(etapa)}</h4>
                <span class="tag">${items.length}</span>
            </div>
            <div class="columna-tablero__lista">
                ${items.length ? items.map((publicacion) => renderPublicacion(grupoTablero.grupo.id, publicacion)).join("") : '<div class="empty">Sin publicaciones en esta columna.</div>'}
            </div>
        </article>
    `;
}

function renderPublicacion(grupoId, publicacion) {
    return `
        <details class="publicacion">
            <summary>
                <h5 class="publicacion__titulo">${esc(publicacion.titulo)}</h5>
                <p class="publicacion__meta">${esc(publicacion.autorNombre)} | ${esc(formatearRol(publicacion.rolAutor))} | ${esc(publicacion.fechaCreacion)}</p>
            </summary>
            <div class="ficha-apa">
                <div class="ficha-grid">
                    <p><strong>Autor:</strong> ${esc(publicacion.autorNombre)}</p>
                    <p><strong>Titulo:</strong> ${esc(publicacion.titulo)}</p>
                    <p><strong>Anio:</strong> ${publicacion.anio}</p>
                    <p><strong>Ciudad:</strong> ${esc(publicacion.ciudad)}</p>
                    <p><strong>Editorial:</strong> ${esc(publicacion.editorial)}</p>
                    <p><strong>DOI:</strong> ${esc(publicacion.doi || "Sin dato")}</p>
                    <p><strong>Enlace:</strong> ${publicacion.enlace ? `<a href="${escAttr(publicacion.enlace)}" target="_blank" rel="noreferrer">${esc(publicacion.enlace)}</a>` : "Sin dato"}</p>
                    <p><strong>Etapa:</strong> ${esc(formatearEtapa(publicacion.etapa))}</p>
                </div>
                <p><strong>Resumen:</strong> ${esc(publicacion.resumen)}</p>
                <div class="comentarios">
                    ${publicacion.comentarios.length ? publicacion.comentarios.map(renderComentario).join("") : '<div class="empty">Sin comentarios aun.</div>'}
                </div>
                ${puedeComentarEnGrupo(grupoId) ? `
                    <form class="formulario-grid" data-accion="guardar-comentario">
                        <input type="hidden" name="publicacionId" value="${publicacion.id}">
                        <label><span>Comentario</span><textarea name="mensaje" required></textarea></label>
                        ${estado.panel?.usuario.rol === "TUTOR" || estado.panel?.usuario.rol === "DIRECTOR" ? `<label><span>Estilo</span><select name="consejoTutor"><option value="true">Consejo del tutor</option><option value="false">Comentario simple</option></select></label>` : ""}
                        <div class="formulario-acciones">
                            <button type="submit" class="boton boton--secundario">Comentar</button>
                        </div>
                    </form>
                ` : ""}
            </div>
        </details>
    `;
}

function renderComentario(comentario) {
    const clases = ["comentario"];
    if (comentario.consejoTutor || comentario.rolAutor === "TUTOR" || comentario.rolAutor === "DIRECTOR") clases.push("comentario--tutor");
    if (comentario.rolAutor === "ESTUDIANTE" && !comentario.atendidoPorTutor) clases.push("comentario--pendiente");

    return `
        <div class="${clases.join(" ")}">
            <p><strong>${esc(comentario.autorNombre)}</strong> | ${esc(formatearRol(comentario.rolAutor))}</p>
            <p>${esc(comentario.mensaje)}</p>
            <p class="texto-suave">${esc(comentario.fechaCreacion)}</p>
        </div>
    `;
}

function renderizarRanking() {
    const nodo = document.getElementById("panel-ranking");
    const ranking = estado.panel?.ranking || [];

    if (!ranking.length) {
        nodo.innerHTML = '<div class="empty">Disponible con sesion activa.</div>';
        return;
    }

    nodo.innerHTML = ranking.map((item, indice) => `
        <div class="item-lista ${item.id === estado.usuario?.id ? "item-lista--foco" : ""}">
            <p><strong>#${indice + 1} ${esc(item.nombre)}</strong></p>
            <p class="texto-suave">${esc(formatearRol(item.rol))} | ${item.puntajeAyuda} pts</p>
        </div>
    `).join("");
}

function renderizarAlertas() {
    const nodo = document.getElementById("panel-alertas");

    if (!estado.panel?.usuario) {
        nodo.innerHTML = '<div class="empty">Las alertas operativas aparecen al ingresar con un rol.</div>';
        return;
    }

    if (estado.panel.usuario.rol === "DIRECTOR") {
        const gruposConPendientes = estado.panel.grupos.filter((grupo) => grupo.grupo.totalSolicitudesPendientes > 0).slice(0, 3);
        const reporte = estado.panel.reporteAdministracion;
        nodo.innerHTML = `
            <div class="item-lista">
                <p><strong>Comentarios sin tutor</strong></p>
                <p class="texto-suave">${reporte?.comentariosPendientesTutor ?? 0} comentarios estudiantiles siguen pendientes.</p>
            </div>
            ${gruposConPendientes.length ? gruposConPendientes.map((grupo) => `
                <div class="item-lista">
                    <p><strong>${esc(grupo.grupo.nombre)}</strong></p>
                    <p class="texto-suave">${grupo.grupo.totalSolicitudesPendientes} solicitudes pendientes.</p>
                </div>
            `).join("") : '<div class="item-lista"><p><strong>Solicitudes</strong></p><p class="texto-suave">No hay grupos con solicitudes pendientes.</p></div>'}
        `;
        return;
    }

    if (estado.panel.usuario.rol === "TUTOR") {
        const solicitudes = estado.panel.notificacionesTutor || [];
        const comentarios = estado.panel.comentariosPrioritarios || [];
        nodo.innerHTML = `
            ${solicitudes.length ? solicitudes.map((solicitud) => `
                <div class="item-lista">
                    <p><strong>Solicitud pendiente</strong></p>
                    <p>${esc(solicitud.estudianteNombre)} quiere unirse a ${esc(solicitud.grupoNombre)}.</p>
                    <div class="acciones-en-linea">
                        <button type="button" class="boton boton--suave" data-accion="resolver-solicitud" data-id="${solicitud.id}" data-aprobar="true">Aprobar</button>
                        <button type="button" class="boton boton--peligro" data-accion="resolver-solicitud" data-id="${solicitud.id}" data-aprobar="false">Rechazar</button>
                    </div>
                </div>
            `).join("") : '<div class="empty">No hay solicitudes pendientes.</div>'}
            ${comentarios.length ? comentarios.map((comentario) => `
                <div class="item-lista">
                    <p><strong>Comentario sin respuesta</strong></p>
                    <p>${esc(comentario.estudianteNombre)} en ${esc(comentario.publicacionTitulo)}</p>
                    <p class="texto-suave">${esc(comentario.mensaje)}</p>
                </div>
            `).join("") : '<div class="empty">No hay comentarios prioritarios.</div>'}
        `;
        return;
    }

    const solicitudPendiente = buscarSolicitudPendienteActual();
    nodo.innerHTML = `
        <div class="item-lista">
            <p><strong>Mi situacion</strong></p>
            <p class="texto-suave">${estado.panel.usuario.grupoNombre ? `Ya perteneces a ${esc(estado.panel.usuario.grupoNombre)}.` : "Todavia no perteneces a un grupo."}</p>
        </div>
        <div class="item-lista">
            <p><strong>Solicitud activa</strong></p>
            <p class="texto-suave">${solicitudPendiente ? `Pendiente en ${esc(solicitudPendiente.grupoNombre)}.` : "No tienes solicitudes pendientes."}</p>
        </div>
    `;
}

async function manejarFormulariosDinamicos(evento) {
    evento.preventDefault();
    const accion = evento.target.dataset.accion;
    try {
        if (accion === "guardar-usuario") await guardarUsuario(evento.target);
        if (accion === "guardar-grupo") await guardarGrupo(evento.target);
        if (accion === "guardar-publicacion") await guardarPublicacion(evento.target);
        if (accion === "guardar-comentario") await guardarComentario(evento.target);
        if (accion === "importar-alumnos") await importarAlumnos(evento.target);
    } catch (error) {
        mostrarMensaje(error.message || "No se pudo completar la operacion.", true);
    }
}

async function manejarClicksDinamicos(evento) {
    const boton = evento.target.closest("[data-accion]");
    if (!boton) return;

    const accion = boton.dataset.accion;
    const accionesClick = new Set([
        "cambiar-vista-rol",
        "cancelar-edicion-usuario",
        "cancelar-edicion-grupo",
        "editar-usuario",
        "editar-grupo",
        "eliminar-usuario",
        "eliminar-grupo",
        "solicitar-ingreso",
        "resolver-solicitud"
    ]);
    if (!accionesClick.has(accion)) return;

    try {
        if (accion === "cambiar-vista-rol") {
            estado.vistaRolActiva = boton.dataset.vista;
            renderizar();
            return;
        }
        if (accion === "cancelar-edicion-usuario") estado.edicionUsuarioId = null;
        if (accion === "cancelar-edicion-grupo") estado.edicionGrupoId = null;
        if (accion === "editar-usuario") estado.edicionUsuarioId = Number(boton.dataset.id);
        if (accion === "editar-grupo") estado.edicionGrupoId = Number(boton.dataset.id);
        if (accion === "eliminar-usuario") await eliminarUsuario(Number(boton.dataset.id));
        if (accion === "eliminar-grupo") await eliminarGrupo(Number(boton.dataset.id));
        if (accion === "solicitar-ingreso") await solicitarIngreso(Number(boton.dataset.grupoId));
        if (accion === "resolver-solicitud") await resolverSolicitud(Number(boton.dataset.id), boton.dataset.aprobar === "true");
        renderizar();
    } catch (error) {
        mostrarMensaje(error.message || "No se pudo completar la accion.", true);
    }
}

async function guardarUsuario(formulario) {
    const datos = Object.fromEntries(new FormData(formulario).entries());
    const clave = (datos.clave || "").toString().trim();
    if (!clave && !datos.id) {
        throw new Error("Ingrese una clave para crear el usuario.");
    }

    const cuerpo = {
        nombre: datos.nombre,
        correo: datos.correo,
        clave: clave || null,
        rol: datos.rol,
        temaTesis: datos.temaTesis || null,
        grupoId: datos.rol === "ESTUDIANTE" && datos.grupoId ? Number(datos.grupoId) : null
    };

    const url = datos.id
        ? `/api/administracion/usuarios/${datos.id}?usuarioId=${estado.usuario.id}`
        : `/api/administracion/usuarios?usuarioId=${estado.usuario.id}`;
    await apiRequest(apiUrl(url), { method: datos.id ? "PUT" : "POST", body: JSON.stringify(cuerpo) });
    formulario.reset();
    estado.edicionUsuarioId = null;
    await refrescarPanel();
    mostrarMensaje("Usuario guardado correctamente.", false);
}

async function guardarGrupo(formulario) {
    const datos = Object.fromEntries(new FormData(formulario).entries());
    const cuerpo = {
        nombre: datos.nombre,
        temaTesis: datos.temaTesis,
        descripcion: datos.descripcion,
        tutorId: datos.tutorId ? Number(datos.tutorId) : null
    };

    const esDirector = estado.panel.usuario.rol === "DIRECTOR";
    const base = esDirector ? "/api/administracion/grupos" : "/api/grupos";
    const url = datos.id ? `${base}/${datos.id}?usuarioId=${estado.usuario.id}` : `${base}?usuarioId=${estado.usuario.id}`;
    await apiRequest(apiUrl(url), { method: datos.id ? "PUT" : "POST", body: JSON.stringify(cuerpo) });
    formulario.reset();
    estado.edicionGrupoId = null;
    await refrescarPanel();
    mostrarMensaje("Grupo guardado correctamente.", false);
}

async function guardarPublicacion(formulario) {
    const datos = Object.fromEntries(new FormData(formulario).entries());
    const cuerpo = {
        grupoId: Number(datos.grupoId),
        autorId: estado.usuario.id,
        titulo: datos.titulo,
        resumen: datos.resumen,
        etapa: datos.etapa,
        anio: Number(datos.anio),
        ciudad: datos.ciudad,
        editorial: datos.editorial,
        doi: datos.doi || null,
        enlace: datos.enlace || null
    };
    const publicacion = await apiRequest(apiUrl("/api/publicaciones"), { method: "POST", body: JSON.stringify(cuerpo) });
    formulario.reset();
    await refrescarPanel();
    const esAporteExterno = estado.panel?.usuario?.rol === "ESTUDIANTE"
        && estado.panel.usuario.grupoId !== publicacion.grupoId;
    mostrarMensaje(esAporteExterno && publicacion.etapa === "EN_REVISION"
        ? "Publicacion enviada al grupo y marcada en revision."
        : "Publicacion creada correctamente.", false);
}

async function guardarComentario(formulario) {
    const datos = Object.fromEntries(new FormData(formulario).entries());
    const cuerpo = {
        autorId: estado.usuario.id,
        mensaje: datos.mensaje,
        consejoTutor: datos.consejoTutor ? datos.consejoTutor === "true" : null
    };
    await apiRequest(apiUrl(`/api/publicaciones/${datos.publicacionId}/comentarios`), { method: "POST", body: JSON.stringify(cuerpo) });
    formulario.reset();
    await refrescarPanel();
    mostrarMensaje("Comentario registrado.", false);
}

async function importarAlumnos(formulario) {
    const archivo = formulario.querySelector('input[name="archivo"]').files[0];
    if (!archivo) throw new Error("Seleccione un archivo para importar.");
    const data = new FormData();
    data.append("archivo", archivo);
    const resultado = await apiRequest(apiUrl(`/api/administracion/importacion/alumnos?usuarioId=${estado.usuario.id}`), { method: "POST", body: data, esFormulario: true });
    formulario.reset();
    await refrescarPanel();
    mostrarMensaje(`Importacion terminada. Creados: ${resultado.creados}. Omitidos: ${resultado.omitidos}.`, false);
}

async function eliminarUsuario(id) {
    await apiRequest(apiUrl(`/api/administracion/usuarios/${id}?usuarioId=${estado.usuario.id}`), { method: "DELETE" });
    estado.edicionUsuarioId = null;
    await refrescarPanel();
    mostrarMensaje("Usuario eliminado.", false);
}

async function eliminarGrupo(id) {
    await apiRequest(apiUrl(`/api/administracion/grupos/${id}?usuarioId=${estado.usuario.id}`), { method: "DELETE" });
    estado.edicionGrupoId = null;
    await refrescarPanel();
    mostrarMensaje("Grupo eliminado.", false);
}

async function solicitarIngreso(grupoId) {
    await apiRequest(apiUrl(`/api/grupos/${grupoId}/solicitudes?usuarioId=${estado.usuario.id}`), { method: "POST" });
    await refrescarPanel();
    mostrarMensaje("Solicitud enviada correctamente.", false);
}

async function resolverSolicitud(id, aprobar) {
    await apiRequest(apiUrl(`/api/grupos/solicitudes/${id}/resolver?usuarioId=${estado.usuario.id}&aprobar=${aprobar}`), { method: "POST" });
    await refrescarPanel();
    mostrarMensaje(aprobar ? "Solicitud aprobada." : "Solicitud rechazada.", false);
}

async function apiRequest(url, opciones = {}) {
    const configuracion = {
        method: opciones.method || "GET",
        headers: opciones.esFormulario ? {} : { "Content-Type": "application/json" },
        body: opciones.body
    };

    try {
        const respuesta = await fetch(url, configuracion);
        if (!respuesta.ok) {
            let mensaje = "Error en la solicitud.";
            try {
                const datos = await respuesta.json();
                mensaje = datos.mensaje || mensaje;
            } catch {
                // sin json
            }
            throw new Error(mensaje);
        }
        if (respuesta.status === 204) return null;
        const texto = await respuesta.text();
        return texto ? JSON.parse(texto) : null;
    } catch (error) {
        if (error instanceof TypeError) error.esRed = true;
        throw error;
    }
}

function puedeSolicitarGrupo(grupoId) {
    if (!estado.panel?.usuario || estado.panel.usuario.rol !== "ESTUDIANTE" || estado.panel.usuario.grupoId) return false;
    return !estado.panel.grupos.some((grupo) => grupo.solicitudesPendientes.some((solicitud) => solicitud.estudianteId === estado.usuario.id && solicitud.estado === "PENDIENTE" && solicitud.grupoId === grupoId));
}

function puedeComentarEnGrupo(grupoId) {
    return Boolean(estado.panel?.usuario);
}

function puedeVerSolicitudes() {
    return estado.panel?.usuario?.rol === "DIRECTOR" || estado.panel?.usuario?.rol === "TUTOR";
}

function obtenerGruposPublicables() {
    if (!estado.panel?.usuario) return [];
    if (estado.panel.usuario.rol === "DIRECTOR" || estado.panel.usuario.rol === "TUTOR") {
        return estado.panel.grupos.map((grupo) => ({ id: grupo.grupo.id, nombre: grupo.grupo.nombre }));
    }
    const grupos = [...estado.panel.grupos];
    grupos.sort((a, b) => {
        if (a.grupo.id === estado.panel.usuario.grupoId) return -1;
        if (b.grupo.id === estado.panel.usuario.grupoId) return 1;
        return a.grupo.nombre.localeCompare(b.grupo.nombre);
    });
    return grupos.map((grupo) => ({
        id: grupo.grupo.id,
        nombre: grupo.grupo.id === estado.panel.usuario.grupoId
            ? `Mi grupo · ${grupo.grupo.nombre}`
            : `Aportar a · ${grupo.grupo.nombre}`
    }));
}

function sincronizarFormularioUsuario(formulario) {
    if (!formulario) return;

    const campoGrupo = formulario.querySelector("#campo-grupo-usuario");
    const selectorRol = formulario.querySelector('select[name="rol"]');
    const selectorGrupo = formulario.querySelector('select[name="grupoId"]');
    if (!campoGrupo || !selectorRol || !selectorGrupo) return;

    const esEstudiante = selectorRol.value === "ESTUDIANTE";
    campoGrupo.classList.toggle("oculto", !esEstudiante);
    if (!esEstudiante) {
        selectorGrupo.value = "";
    }
}

function obtenerGruposTableroActuales() {
    const grupos = [...(estado.panel?.grupos || estado.tableroGeneral || [])];
    if (estado.panel?.usuario?.rol === "ESTUDIANTE") {
        grupos.sort((a, b) => {
            if (a.grupo.id === estado.panel.usuario.grupoId) return -1;
            if (b.grupo.id === estado.panel.usuario.grupoId) return 1;
            return a.grupo.nombre.localeCompare(b.grupo.nombre);
        });
    }
    return grupos;
}

function debeMostrarGrupoExpandido(grupoId) {
    if (estado.panel?.usuario?.rol !== "ESTUDIANTE") {
        return true;
    }
    return estado.panel.usuario.grupoId === grupoId;
}

function contarPublicacionesEnGrupos(grupos) {
    return (grupos || []).reduce((total, grupo) => total + grupo.publicaciones.length, 0);
}

function contarComentariosEnGrupos(grupos) {
    return (grupos || []).reduce(
        (total, grupo) => total + grupo.publicaciones.reduce((subtotal, publicacion) => subtotal + publicacion.comentarios.length, 0),
        0
    );
}

function obtenerGrupoMasActivo(grupos) {
    return (grupos || []).reduce((mejor, actual) => {
        const actividadActual = actual.publicaciones.length + actual.publicaciones.reduce((subtotal, publicacion) => subtotal + publicacion.comentarios.length, 0);
        const actividadMejor = mejor
            ? mejor.publicaciones.length + mejor.publicaciones.reduce((subtotal, publicacion) => subtotal + publicacion.comentarios.length, 0)
            : -1;

        return actividadActual > actividadMejor ? actual : mejor;
    }, null);
}

function obtenerEtapaDominante(grupos) {
    const etapas = ETAPAS.map((etapa) => ({
        etapa,
        total: (grupos || []).reduce((total, grupo) => total + grupo.publicaciones.filter((publicacion) => normalizarEtapaVisible(publicacion.etapa) === etapa).length, 0)
    })).filter((item) => item.total > 0);

    if (!etapas.length) return null;

    return etapas.reduce((dominante, actual) => actual.total > dominante.total ? actual : dominante);
}

function totalAlumnosVisibles() {
    return (estado.panel?.grupos || []).reduce((total, grupo) => total + grupo.grupo.totalAlumnos, 0);
}

function buscarPuntajeActual() {
    return estado.panel?.ranking.find((item) => item.id === estado.usuario?.id)?.puntajeAyuda ?? 0;
}

function contarPublicacionesAutor(autorId) {
    return (estado.panel?.grupos || []).reduce((total, grupo) => total + grupo.publicaciones.filter((publicacion) => publicacion.autorId === autorId).length, 0);
}

function contarComentariosAutor(autorId) {
    return (estado.panel?.grupos || []).reduce((total, grupo) => total + grupo.publicaciones.reduce((subTotal, publicacion) => subTotal + publicacion.comentarios.filter((comentario) => comentario.autorId === autorId).length, 0), 0);
}

function buscarSolicitudPendienteActual() {
    if (!estado.panel?.usuario) return null;
    for (const grupo of estado.panel.grupos) {
        const solicitud = grupo.solicitudesPendientes.find((item) => item.estudianteId === estado.usuario.id && item.estado === "PENDIENTE");
        if (solicitud) return solicitud;
    }
    return null;
}

function construirSerieEtapas(grupos) {
    return ETAPAS.map((etapa) => ({
        etiqueta: formatearEtapa(etapa),
        valor: grupos.reduce((total, grupo) => total + grupo.publicaciones.filter((publicacion) => normalizarEtapaVisible(publicacion.etapa) === etapa).length, 0),
        descripcion: "Lectura consolidada"
    }));
}

function porcentaje(parte, total) {
    if (!total) return 0;
    return Math.round((parte / total) * 100);
}

function formatearRol(rol) {
    if (rol === "DIRECTOR") return "Director";
    if (rol === "TUTOR") return "Tutor";
    if (rol === "ESTUDIANTE") return "Estudiante";
    return rol || "";
}

function formatearEtapa(etapa) {
    if (etapa === "EN_REVISION") return "En revision";
    if (etapa === "PROPUESTA") return "Propuesta";
    if (etapa === "PUBLICADA") return "Propuesta";
    return etapa || "";
}

function normalizarEtapaVisible(etapa) {
    return etapa === "PUBLICADA" ? "PROPUESTA" : etapa;
}

function mostrarMensaje(texto, esError) {
    const nodo = document.getElementById("mensaje-global");
    nodo.textContent = texto;
    nodo.classList.remove("oculto");
    nodo.style.background = esError ? "rgba(141, 61, 51, 0.12)" : "rgba(23, 77, 61, 0.12)";
    nodo.style.color = esError ? "#8d3d33" : "#174d3d";
}

function actualizarInsignia(texto, esFallback) {
    const nodo = document.getElementById("insignia-modo");
    nodo.textContent = texto;
    nodo.className = `insignia ${esFallback ? "insignia--alerta" : "insignia--ok"}`;
}

function apiUrl(path) {
    return API_BASE_URL ? `${API_BASE_URL}${path}` : path;
}

function esc(valor) {
    return (valor ?? "").toString().replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
}

function escAttr(valor) {
    return esc(valor).replaceAll('"', "&quot;");
}
