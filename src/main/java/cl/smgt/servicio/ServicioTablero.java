package cl.smgt.servicio;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.smgt.dominio.EstadoSolicitud;
import cl.smgt.dominio.GrupoInvestigacion;
import cl.smgt.dominio.RolUsuario;
import cl.smgt.dominio.Usuario;
import cl.smgt.dto.ComentarioPrioritarioDto;
import cl.smgt.dto.GrupoTableroDto;
import cl.smgt.dto.PanelUsuarioDto;
import cl.smgt.dto.ReporteAdministracionDto;
import cl.smgt.dto.ResumenAdministracionDto;
import cl.smgt.dto.SolicitudDto;
import cl.smgt.dto.UsuarioDto;
import cl.smgt.dto.UsuarioRankingDto;
import cl.smgt.dto.UsuarioSesionDto;
import cl.smgt.repositorio.GrupoInvestigacionRepositorio;
import cl.smgt.repositorio.SolicitudIngresoRepositorio;
import cl.smgt.repositorio.UsuarioRepositorio;

@Service
@Transactional(readOnly = true)
public class ServicioTablero {

    private final UsuarioRepositorio usuarioRepositorio;
    private final GrupoInvestigacionRepositorio grupoInvestigacionRepositorio;
    private final SolicitudIngresoRepositorio solicitudIngresoRepositorio;
    private final ServicioAdministracion servicioAdministracion;
    private final ServicioGrupoInvestigacion servicioGrupoInvestigacion;
    private final ServicioPublicacion servicioPublicacion;
    private final MapeadorSmgt mapeadorSmgt;

    public ServicioTablero(
        UsuarioRepositorio usuarioRepositorio,
        GrupoInvestigacionRepositorio grupoInvestigacionRepositorio,
        SolicitudIngresoRepositorio solicitudIngresoRepositorio,
        ServicioAdministracion servicioAdministracion,
        ServicioGrupoInvestigacion servicioGrupoInvestigacion,
        ServicioPublicacion servicioPublicacion,
        MapeadorSmgt mapeadorSmgt
    ) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.grupoInvestigacionRepositorio = grupoInvestigacionRepositorio;
        this.solicitudIngresoRepositorio = solicitudIngresoRepositorio;
        this.servicioAdministracion = servicioAdministracion;
        this.servicioGrupoInvestigacion = servicioGrupoInvestigacion;
        this.servicioPublicacion = servicioPublicacion;
        this.mapeadorSmgt = mapeadorSmgt;
    }

    public PanelUsuarioDto obtenerPanel(Long usuarioId) {
        Usuario usuario = usuarioRepositorio.findById(usuarioId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        UsuarioSesionDto sesion = mapeadorSmgt.aSesion(usuario);
        List<UsuarioRankingDto> ranking = usuarioRepositorio.findAllByOrderByPuntajeAyudaDescNombreAsc().stream()
            .limit(10)
            .map(mapeadorSmgt::aRankingDto)
            .toList();

        List<GrupoInvestigacion> gruposBase = seleccionarGruposParaUsuario(usuario);
        List<GrupoTableroDto> grupos = gruposBase.stream()
            .map(this::construirGrupoTablero)
            .toList();

        List<UsuarioDto> usuarios = seleccionarUsuariosParaPanel(usuario, gruposBase);
        List<SolicitudDto> notificacionesTutor = usuario.getRol() == RolUsuario.TUTOR
            ? servicioGrupoInvestigacion.listarSolicitudesPendientesTutor(usuarioId)
            : List.of();
        List<ComentarioPrioritarioDto> comentariosPrioritarios = usuario.getRol() == RolUsuario.TUTOR
            ? servicioPublicacion.listarComentariosPendientesTutor(usuarioId)
            : List.of();
        ResumenAdministracionDto resumen = usuario.getRol() == RolUsuario.DIRECTOR
            ? servicioAdministracion.obtenerResumen(usuarioId)
            : new ResumenAdministracionDto(0, 0, 0, 0, 0, 0);
        ReporteAdministracionDto reporte = usuario.getRol() == RolUsuario.DIRECTOR
            ? servicioAdministracion.obtenerReporte(usuarioId)
            : null;

        return new PanelUsuarioDto(sesion, resumen, reporte, usuarios, grupos, ranking, notificacionesTutor, comentariosPrioritarios);
    }

    public List<GrupoTableroDto> listarTableroGeneral() {
        return grupoInvestigacionRepositorio.findAllByOrderByNombreAsc().stream()
            .map(this::construirGrupoTablero)
            .toList();
    }

    private List<GrupoInvestigacion> seleccionarGruposParaUsuario(Usuario usuario) {
        if (usuario.getRol() == RolUsuario.TUTOR) {
            return grupoInvestigacionRepositorio.findByTutorIdOrderByNombreAsc(usuario.getId());
        }
        return grupoInvestigacionRepositorio.findAllByOrderByNombreAsc();
    }

    private List<UsuarioDto> seleccionarUsuariosParaPanel(Usuario usuario, List<GrupoInvestigacion> gruposBase) {
        if (usuario.getRol() == RolUsuario.DIRECTOR) {
            return usuarioRepositorio.findAllByOrderByNombreAsc().stream()
                .map(mapeadorSmgt::aUsuarioDto)
                .toList();
        }

        List<UsuarioDto> usuarios = new ArrayList<>();
        if (usuario.getRol() == RolUsuario.TUTOR) {
            usuarios.add(mapeadorSmgt.aUsuarioDto(usuario));
            for (GrupoInvestigacion grupo : gruposBase) {
                usuarios.addAll(servicioAdministracion.listarUsuariosPorGrupo(grupo.getId()));
            }
            return usuarios;
        }

        if (usuario.getGrupoInvestigacion() != null) {
            return servicioAdministracion.listarUsuariosPorGrupo(usuario.getGrupoInvestigacion().getId());
        }
        return List.of(mapeadorSmgt.aUsuarioDto(usuario));
    }

    private GrupoTableroDto construirGrupoTablero(GrupoInvestigacion grupo) {
        return new GrupoTableroDto(
            mapeadorSmgt.aGrupoResumenDto(
                grupo,
                usuarioRepositorio.findByGrupoInvestigacionIdOrderByNombreAsc(grupo.getId()).size(),
                solicitudIngresoRepositorio.findByGrupoIdAndEstadoOrderByFechaSolicitudDesc(grupo.getId(), EstadoSolicitud.PENDIENTE).size()
            ),
            servicioAdministracion.listarUsuariosPorGrupo(grupo.getId()),
            servicioPublicacion.listarPublicacionesPorGrupo(grupo.getId()),
            solicitudIngresoRepositorio.findByGrupoIdAndEstadoOrderByFechaSolicitudDesc(grupo.getId(), EstadoSolicitud.PENDIENTE).stream()
                .map(mapeadorSmgt::aSolicitudDto)
                .toList()
        );
    }
}
