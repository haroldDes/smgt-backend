package cl.smgt.servicio;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import cl.smgt.dominio.Comentario;
import cl.smgt.dominio.EtapaPublicacion;
import cl.smgt.dominio.GrupoInvestigacion;
import cl.smgt.dominio.Publicacion;
import cl.smgt.dominio.SolicitudIngreso;
import cl.smgt.dominio.Usuario;
import cl.smgt.dto.ComentarioDto;
import cl.smgt.dto.GrupoResumenDto;
import cl.smgt.dto.PublicacionDto;
import cl.smgt.dto.SolicitudDto;
import cl.smgt.dto.UsuarioDto;
import cl.smgt.dto.UsuarioRankingDto;
import cl.smgt.dto.UsuarioSesionDto;

@Component
public class MapeadorSmgt {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public UsuarioSesionDto aSesion(Usuario usuario) {
        return new UsuarioSesionDto(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getCorreo(),
            usuario.getRol().name(),
            usuario.getGrupoInvestigacion() != null ? usuario.getGrupoInvestigacion().getId() : null,
            usuario.getGrupoInvestigacion() != null ? usuario.getGrupoInvestigacion().getNombre() : null
        );
    }

    public UsuarioDto aUsuarioDto(Usuario usuario) {
        return new UsuarioDto(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getCorreo(),
            usuario.getRol().name(),
            usuario.getPuntajeAyuda(),
            usuario.getTemaTesis(),
            usuario.getGrupoInvestigacion() != null ? usuario.getGrupoInvestigacion().getId() : null,
            usuario.getGrupoInvestigacion() != null ? usuario.getGrupoInvestigacion().getNombre() : null
        );
    }

    public UsuarioRankingDto aRankingDto(Usuario usuario) {
        return new UsuarioRankingDto(usuario.getId(), usuario.getNombre(), usuario.getRol().name(), usuario.getPuntajeAyuda());
    }

    public GrupoResumenDto aGrupoResumenDto(GrupoInvestigacion grupo, int totalAlumnos, int totalSolicitudesPendientes) {
        return new GrupoResumenDto(
            grupo.getId(),
            grupo.getNombre(),
            grupo.getTemaTesis(),
            grupo.getDescripcion(),
            grupo.getColorHex(),
            grupo.getIcono(),
            grupo.getTutor().getId(),
            grupo.getTutor().getNombre(),
            totalAlumnos,
            totalSolicitudesPendientes
        );
    }

    public SolicitudDto aSolicitudDto(SolicitudIngreso solicitud) {
        return new SolicitudDto(
            solicitud.getId(),
            solicitud.getEstudiante().getId(),
            solicitud.getEstudiante().getNombre(),
            solicitud.getGrupo().getId(),
            solicitud.getGrupo().getNombre(),
            solicitud.getEstado().name(),
            formatearFecha(solicitud.getFechaSolicitud())
        );
    }

    public ComentarioDto aComentarioDto(Comentario comentario) {
        return new ComentarioDto(
            comentario.getId(),
            comentario.getAutor().getId(),
            comentario.getAutor().getNombre(),
            comentario.getAutor().getRol().name(),
            comentario.getMensaje(),
            comentario.isConsejoTutor(),
            comentario.isAtendidoPorTutor(),
            formatearFecha(comentario.getFechaCreacion())
        );
    }

    public PublicacionDto aPublicacionDto(Publicacion publicacion, List<ComentarioDto> comentarios) {
        return new PublicacionDto(
            publicacion.getId(),
            publicacion.getGrupo().getId(),
            publicacion.getGrupo().getNombre(),
            publicacion.getAutor().getId(),
            publicacion.getAutor().getNombre(),
            publicacion.getAutor().getRol().name(),
            publicacion.getTitulo(),
            publicacion.getResumen(),
            normalizarEtapa(publicacion.getEtapa()).name(),
            publicacion.getAnio(),
            publicacion.getCiudad(),
            publicacion.getEditorial(),
            publicacion.getDoi(),
            publicacion.getEnlace(),
            formatearFecha(publicacion.getFechaCreacion()),
            comentarios
        );
    }

    public String formatearFecha(LocalDateTime fecha) {
        return fecha.format(FORMATO_FECHA);
    }

    private EtapaPublicacion normalizarEtapa(EtapaPublicacion etapa) {
        if (etapa == EtapaPublicacion.PUBLICADA) {
            return EtapaPublicacion.PROPUESTA;
        }
        return etapa;
    }
}
