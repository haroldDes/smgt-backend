package cl.smgt.servicio;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.smgt.dominio.Comentario;
import cl.smgt.dominio.EtapaPublicacion;
import cl.smgt.dominio.GrupoInvestigacion;
import cl.smgt.dominio.Publicacion;
import cl.smgt.dominio.RolUsuario;
import cl.smgt.dominio.Usuario;
import cl.smgt.dto.ComentarioCreacionDto;
import cl.smgt.dto.ComentarioDto;
import cl.smgt.dto.ComentarioPrioritarioDto;
import cl.smgt.dto.PublicacionCreacionDto;
import cl.smgt.dto.PublicacionDto;
import cl.smgt.repositorio.ComentarioRepositorio;
import cl.smgt.repositorio.GrupoInvestigacionRepositorio;
import cl.smgt.repositorio.PublicacionRepositorio;
import cl.smgt.repositorio.UsuarioRepositorio;

@Service
@Transactional
public class ServicioPublicacion {

    private final PublicacionRepositorio publicacionRepositorio;
    private final ComentarioRepositorio comentarioRepositorio;
    private final GrupoInvestigacionRepositorio grupoInvestigacionRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final MapeadorSmgt mapeadorSmgt;

    public ServicioPublicacion(
        PublicacionRepositorio publicacionRepositorio,
        ComentarioRepositorio comentarioRepositorio,
        GrupoInvestigacionRepositorio grupoInvestigacionRepositorio,
        UsuarioRepositorio usuarioRepositorio,
        MapeadorSmgt mapeadorSmgt
    ) {
        this.publicacionRepositorio = publicacionRepositorio;
        this.comentarioRepositorio = comentarioRepositorio;
        this.grupoInvestigacionRepositorio = grupoInvestigacionRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.mapeadorSmgt = mapeadorSmgt;
    }

    public PublicacionDto crearPublicacion(PublicacionCreacionDto formulario) {
        Usuario autor = obtenerUsuario(formulario.autorId());
        GrupoInvestigacion grupo = obtenerGrupo(formulario.grupoId());
        validarAccesoPublicacion(autor, grupo);

        Publicacion publicacion = new Publicacion(
            grupo,
            autor,
            formulario.titulo().trim(),
            formulario.resumen().trim(),
            resolverEtapaPublicacion(autor, grupo),
            formulario.anio(),
            formulario.ciudad().trim(),
            formulario.editorial().trim(),
            limpiarOpcional(formulario.doi()),
            limpiarOpcional(formulario.enlace()),
            LocalDateTime.now()
        );

        Publicacion publicacionGuardada = publicacionRepositorio.save(publicacion);
        autor.sumarPuntaje(autor.getRol() == RolUsuario.ESTUDIANTE ? 8 : 5);
        return mapearPublicacion(publicacionGuardada);
    }

    public ComentarioDto agregarComentario(Long publicacionId, ComentarioCreacionDto formulario) {
        Usuario autor = obtenerUsuario(formulario.autorId());
        Publicacion publicacion = publicacionRepositorio.findById(publicacionId)
            .orElseThrow(() -> new IllegalArgumentException("Publicacion no encontrada."));

        validarAccesoComentario(autor);
        boolean esTutor = autor.getRol() == RolUsuario.TUTOR || autor.getRol() == RolUsuario.DIRECTOR;

        if (esTutor) {
            comentarioRepositorio.findByPublicacionIdOrderByFechaCreacionAsc(publicacionId).stream()
                .filter(comentario -> comentario.getAutor().getRol() == RolUsuario.ESTUDIANTE && !comentario.isAtendidoPorTutor())
                .forEach(comentario -> comentario.setAtendidoPorTutor(true));
        }

        Comentario comentario = new Comentario(
            publicacion,
            autor,
            formulario.mensaje().trim(),
            esTutor && (formulario.consejoTutor() == null || formulario.consejoTutor()),
            esTutor,
            LocalDateTime.now()
        );

        Comentario comentarioGuardado = comentarioRepositorio.save(comentario);
        autor.sumarPuntaje(esTutor ? 6 : 2);
        return mapeadorSmgt.aComentarioDto(comentarioGuardado);
    }

    @Transactional(readOnly = true)
    public List<PublicacionDto> listarPublicacionesPorGrupo(Long grupoId) {
        return publicacionRepositorio.findByGrupoIdOrderByFechaCreacionDesc(grupoId).stream()
            .map(this::mapearPublicacion)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ComentarioPrioritarioDto> listarComentariosPendientesTutor(Long tutorId) {
        Usuario tutor = obtenerUsuario(tutorId);
        if (tutor.getRol() != RolUsuario.TUTOR && tutor.getRol() != RolUsuario.DIRECTOR) {
            throw new IllegalArgumentException("Solo el tutor puede revisar comentarios prioritarios.");
        }

        if (tutor.getRol() == RolUsuario.DIRECTOR) {
            return List.of();
        }

        return comentarioRepositorio
            .findByPublicacionGrupoTutorIdAndAtendidoPorTutorFalseAndAutorRolOrderByFechaCreacionDesc(tutorId, RolUsuario.ESTUDIANTE)
            .stream()
            .map(comentario -> new ComentarioPrioritarioDto(
                comentario.getId(),
                comentario.getPublicacion().getId(),
                comentario.getPublicacion().getTitulo(),
                comentario.getPublicacion().getGrupo().getId(),
                comentario.getPublicacion().getGrupo().getNombre(),
                comentario.getAutor().getId(),
                comentario.getAutor().getNombre(),
                comentario.getMensaje(),
                mapeadorSmgt.formatearFecha(comentario.getFechaCreacion())
            ))
            .toList();
    }

    private PublicacionDto mapearPublicacion(Publicacion publicacion) {
        List<ComentarioDto> comentarios = comentarioRepositorio.findByPublicacionIdOrderByFechaCreacionAsc(publicacion.getId()).stream()
            .map(mapeadorSmgt::aComentarioDto)
            .toList();
        return mapeadorSmgt.aPublicacionDto(publicacion, comentarios);
    }

    private void validarAccesoPublicacion(Usuario usuario, GrupoInvestigacion grupo) {
        if (usuario.getRol() == RolUsuario.DIRECTOR) {
            return;
        }
        if (usuario.getRol() == RolUsuario.TUTOR && grupo.getTutor().getId().equals(usuario.getId())) {
            return;
        }
        if (usuario.getRol() == RolUsuario.ESTUDIANTE) {
            return;
        }
        throw new IllegalArgumentException("El usuario no tiene acceso a este grupo.");
    }

    private void validarAccesoComentario(Usuario usuario) {
        if (usuario.getRol() == RolUsuario.DIRECTOR
            || usuario.getRol() == RolUsuario.TUTOR
            || usuario.getRol() == RolUsuario.ESTUDIANTE) {
            return;
        }
        throw new IllegalArgumentException("El usuario no tiene permiso para comentar.");
    }

    private EtapaPublicacion resolverEtapaPublicacion(Usuario autor, GrupoInvestigacion grupo) {
        if (autor.getRol() == RolUsuario.DIRECTOR) {
            return EtapaPublicacion.PROPUESTA;
        }
        if (autor.getRol() == RolUsuario.TUTOR) {
            return grupo.getTutor().getId().equals(autor.getId()) ? EtapaPublicacion.PROPUESTA : EtapaPublicacion.EN_REVISION;
        }
        if (autor.getGrupoInvestigacion() != null && autor.getGrupoInvestigacion().getId().equals(grupo.getId())) {
            return EtapaPublicacion.PROPUESTA;
        }
        return EtapaPublicacion.EN_REVISION;
    }

    private String limpiarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private Usuario obtenerUsuario(Long usuarioId) {
        return usuarioRepositorio.findById(usuarioId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
    }

    private GrupoInvestigacion obtenerGrupo(Long grupoId) {
        return grupoInvestigacionRepositorio.findById(grupoId)
            .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado."));
    }
}
