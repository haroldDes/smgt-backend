package cl.smgt.servicio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.smgt.dominio.EstadoSolicitud;
import cl.smgt.dominio.GrupoInvestigacion;
import cl.smgt.dominio.Publicacion;
import cl.smgt.dominio.RolUsuario;
import cl.smgt.dominio.SolicitudIngreso;
import cl.smgt.dominio.Usuario;
import cl.smgt.dto.GrupoFormularioDto;
import cl.smgt.dto.GrupoResumenDto;
import cl.smgt.dto.SolicitudDto;
import cl.smgt.repositorio.ComentarioRepositorio;
import cl.smgt.repositorio.GrupoInvestigacionRepositorio;
import cl.smgt.repositorio.PublicacionRepositorio;
import cl.smgt.repositorio.SolicitudIngresoRepositorio;
import cl.smgt.repositorio.UsuarioRepositorio;

@Service
@Transactional
public class ServicioGrupoInvestigacion {

    private final GrupoInvestigacionRepositorio grupoInvestigacionRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final SolicitudIngresoRepositorio solicitudIngresoRepositorio;
    private final PublicacionRepositorio publicacionRepositorio;
    private final ComentarioRepositorio comentarioRepositorio;
    private final MapeadorSmgt mapeadorSmgt;

    public ServicioGrupoInvestigacion(
        GrupoInvestigacionRepositorio grupoInvestigacionRepositorio,
        UsuarioRepositorio usuarioRepositorio,
        SolicitudIngresoRepositorio solicitudIngresoRepositorio,
        PublicacionRepositorio publicacionRepositorio,
        ComentarioRepositorio comentarioRepositorio,
        MapeadorSmgt mapeadorSmgt
    ) {
        this.grupoInvestigacionRepositorio = grupoInvestigacionRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.solicitudIngresoRepositorio = solicitudIngresoRepositorio;
        this.publicacionRepositorio = publicacionRepositorio;
        this.comentarioRepositorio = comentarioRepositorio;
        this.mapeadorSmgt = mapeadorSmgt;
    }

    @Transactional(readOnly = true)
    public List<GrupoResumenDto> listarTodos() {
        return grupoInvestigacionRepositorio.findAllByOrderByNombreAsc().stream()
            .map(this::mapearResumen)
            .toList();
    }

    public GrupoResumenDto crearGrupo(Long usuarioId, GrupoFormularioDto formulario) {
        Usuario actor = obtenerUsuario(usuarioId);
        Usuario tutor = resolverTutorParaCreacion(actor, formulario.tutorId());
        IdentidadVisualGrupo identidad = detectarIdentidad(formulario.temaTesis());

        GrupoInvestigacion grupo = new GrupoInvestigacion(
            formulario.nombre().trim(),
            formulario.temaTesis().trim(),
            formulario.descripcion(),
            identidad.colorHex(),
            identidad.icono(),
            tutor
        );

        return mapearResumen(grupoInvestigacionRepositorio.save(grupo));
    }

    public GrupoResumenDto actualizarGrupo(Long usuarioId, Long grupoId, GrupoFormularioDto formulario) {
        Usuario actor = obtenerUsuario(usuarioId);
        GrupoInvestigacion grupo = obtenerGrupo(grupoId);

        validarGestionGrupo(actor, grupo);

        Usuario tutor = grupo.getTutor();
        if (actor.getRol() == RolUsuario.DIRECTOR && formulario.tutorId() != null) {
            tutor = obtenerUsuario(formulario.tutorId());
            if (tutor.getRol() != RolUsuario.TUTOR) {
                throw new IllegalArgumentException("El tutor asignado no tiene rol TUTOR.");
            }
        }

        IdentidadVisualGrupo identidad = detectarIdentidad(formulario.temaTesis());
        grupo.setNombre(formulario.nombre().trim());
        grupo.setTemaTesis(formulario.temaTesis().trim());
        grupo.setDescripcion(formulario.descripcion());
        grupo.setTutor(tutor);
        grupo.setColorHex(identidad.colorHex());
        grupo.setIcono(identidad.icono());

        return mapearResumen(grupoInvestigacionRepositorio.save(grupo));
    }

    public void eliminarGrupo(Long usuarioId, Long grupoId) {
        Usuario actor = obtenerUsuario(usuarioId);
        GrupoInvestigacion grupo = obtenerGrupo(grupoId);
        validarGestionGrupo(actor, grupo);

        usuarioRepositorio.findByGrupoInvestigacionIdOrderByNombreAsc(grupoId)
            .forEach(alumno -> alumno.setGrupoInvestigacion(null));

        List<Publicacion> publicaciones = publicacionRepositorio.findAllByGrupoId(grupoId);
        for (Publicacion publicacion : publicaciones) {
            comentarioRepositorio.deleteByPublicacionId(publicacion.getId());
        }

        solicitudIngresoRepositorio.deleteByGrupoId(grupoId);
        publicacionRepositorio.deleteByGrupoId(grupoId);
        grupoInvestigacionRepositorio.delete(grupo);
    }

    public SolicitudDto solicitarIngreso(Long estudianteId, Long grupoId) {
        Usuario estudiante = obtenerUsuario(estudianteId);
        if (estudiante.getRol() != RolUsuario.ESTUDIANTE) {
            throw new IllegalArgumentException("Solo un estudiante puede solicitar ingreso a un grupo.");
        }

        if (estudiante.getGrupoInvestigacion() != null) {
            throw new IllegalArgumentException("El estudiante ya pertenece a un grupo.");
        }

        boolean yaTienePendiente = solicitudIngresoRepositorio.findByEstudianteIdOrderByFechaSolicitudDesc(estudianteId).stream()
            .anyMatch(solicitud -> solicitud.getEstado() == EstadoSolicitud.PENDIENTE);
        if (yaTienePendiente) {
            throw new IllegalArgumentException("El estudiante ya tiene una solicitud pendiente.");
        }

        GrupoInvestigacion grupo = obtenerGrupo(grupoId);
        solicitudIngresoRepositorio.findByEstudianteIdAndGrupoIdAndEstado(estudianteId, grupoId, EstadoSolicitud.PENDIENTE)
            .ifPresent(solicitud -> {
                throw new IllegalArgumentException("La solicitud ya fue enviada.");
            });

        SolicitudIngreso solicitud = new SolicitudIngreso(estudiante, grupo, EstadoSolicitud.PENDIENTE, LocalDateTime.now());
        return mapeadorSmgt.aSolicitudDto(solicitudIngresoRepositorio.save(solicitud));
    }

    public SolicitudDto resolverSolicitud(Long usuarioId, Long solicitudId, boolean aprobar) {
        Usuario actor = obtenerUsuario(usuarioId);
        SolicitudIngreso solicitud = solicitudIngresoRepositorio.findById(solicitudId)
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada."));

        validarGestionGrupo(actor, solicitud.getGrupo());
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new IllegalArgumentException("La solicitud ya fue procesada.");
        }

        if (aprobar) {
            if (solicitud.getEstudiante().getGrupoInvestigacion() != null) {
                throw new IllegalArgumentException("El estudiante ya pertenece a otro grupo.");
            }
            solicitud.setEstado(EstadoSolicitud.APROBADA);
            solicitud.getEstudiante().setGrupoInvestigacion(solicitud.getGrupo());
            solicitud.getEstudiante().sumarPuntaje(4);

            solicitudIngresoRepositorio.findByEstudianteIdOrderByFechaSolicitudDesc(solicitud.getEstudiante().getId()).stream()
                .filter(otra -> !otra.getId().equals(solicitud.getId()) && otra.getEstado() == EstadoSolicitud.PENDIENTE)
                .forEach(otra -> otra.setEstado(EstadoSolicitud.RECHAZADA));
        } else {
            solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        }

        if (actor.getRol() == RolUsuario.TUTOR) {
            actor.sumarPuntaje(3);
        }

        return mapeadorSmgt.aSolicitudDto(solicitud);
    }

    @Transactional(readOnly = true)
    public List<SolicitudDto> listarSolicitudesPendientesTutor(Long tutorId) {
        Usuario tutor = obtenerUsuario(tutorId);
        if (tutor.getRol() != RolUsuario.TUTOR && tutor.getRol() != RolUsuario.DIRECTOR) {
            throw new IllegalArgumentException("Solo tutor o director puede revisar solicitudes.");
        }

        if (tutor.getRol() == RolUsuario.DIRECTOR) {
            return solicitudIngresoRepositorio.findAll().stream()
                .filter(solicitud -> solicitud.getEstado() == EstadoSolicitud.PENDIENTE)
                .map(mapeadorSmgt::aSolicitudDto)
                .toList();
        }

        return solicitudIngresoRepositorio.findByGrupoTutorIdAndEstadoOrderByFechaSolicitudDesc(tutorId, EstadoSolicitud.PENDIENTE).stream()
            .map(mapeadorSmgt::aSolicitudDto)
            .toList();
    }

    private Usuario resolverTutorParaCreacion(Usuario actor, Long tutorId) {
        if (actor.getRol() == RolUsuario.TUTOR) {
            return actor;
        }
        if (actor.getRol() != RolUsuario.DIRECTOR) {
            throw new IllegalArgumentException("Solo director o tutor pueden crear grupos.");
        }
        if (tutorId == null) {
            throw new IllegalArgumentException("Debe indicar un tutor para el grupo.");
        }
        Usuario tutor = obtenerUsuario(tutorId);
        if (tutor.getRol() != RolUsuario.TUTOR) {
            throw new IllegalArgumentException("El usuario asignado no es tutor.");
        }
        return tutor;
    }

    private void validarGestionGrupo(Usuario actor, GrupoInvestigacion grupo) {
        if (actor.getRol() == RolUsuario.DIRECTOR) {
            return;
        }
        if (actor.getRol() == RolUsuario.TUTOR && grupo.getTutor().getId().equals(actor.getId())) {
            return;
        }
        throw new IllegalArgumentException("No tiene permiso para administrar este grupo.");
    }

    private Usuario obtenerUsuario(Long usuarioId) {
        return usuarioRepositorio.findById(usuarioId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
    }

    private GrupoInvestigacion obtenerGrupo(Long grupoId) {
        return grupoInvestigacionRepositorio.findById(grupoId)
            .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado."));
    }

    private GrupoResumenDto mapearResumen(GrupoInvestigacion grupo) {
        int totalAlumnos = usuarioRepositorio.findByGrupoInvestigacionIdOrderByNombreAsc(grupo.getId()).size();
        int totalPendientes = solicitudIngresoRepositorio.findByGrupoIdAndEstadoOrderByFechaSolicitudDesc(grupo.getId(), EstadoSolicitud.PENDIENTE).size();
        return mapeadorSmgt.aGrupoResumenDto(grupo, totalAlumnos, totalPendientes);
    }

    private IdentidadVisualGrupo detectarIdentidad(String temaTesis) {
        String normalizado = temaTesis.toLowerCase(Locale.ROOT);

        if (contiene(normalizado, "derecho", "jurid", "ley", "penal", "civil", "justicia")) {
            return new IdentidadVisualGrupo("#c89c45", "/iconos/grupos/balanza.svg");
        }
        if (contiene(normalizado, "micro", "salud", "medic", "biomed", "quim", "laboratorio")) {
            return new IdentidadVisualGrupo("#1f7a8c", "/iconos/grupos/microscopio.svg");
        }
        return new IdentidadVisualGrupo("#2563eb", "/iconos/grupos/redes.svg");
    }

    private boolean contiene(String texto, String... claves) {
        for (String clave : claves) {
            if (texto.contains(clave)) {
                return true;
            }
        }
        return false;
    }

    private record IdentidadVisualGrupo(String colorHex, String icono) {
    }
}
