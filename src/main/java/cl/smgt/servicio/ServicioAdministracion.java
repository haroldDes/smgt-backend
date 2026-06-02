package cl.smgt.servicio;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import cl.smgt.dominio.GrupoInvestigacion;
import cl.smgt.dominio.Publicacion;
import cl.smgt.dominio.RolUsuario;
import cl.smgt.dominio.Usuario;
import cl.smgt.dto.ResumenAdministracionDto;
import cl.smgt.dto.ReporteAdministracionDto;
import cl.smgt.dto.ResultadoImportacionDto;
import cl.smgt.dto.UsuarioDto;
import cl.smgt.dto.UsuarioFormularioDto;
import cl.smgt.repositorio.ComentarioRepositorio;
import cl.smgt.repositorio.GrupoInvestigacionRepositorio;
import cl.smgt.repositorio.PublicacionRepositorio;
import cl.smgt.repositorio.SolicitudIngresoRepositorio;
import cl.smgt.repositorio.UsuarioRepositorio;

@Service
@Transactional
public class ServicioAdministracion {

    private final UsuarioRepositorio usuarioRepositorio;
    private final GrupoInvestigacionRepositorio grupoInvestigacionRepositorio;
    private final SolicitudIngresoRepositorio solicitudIngresoRepositorio;
    private final PublicacionRepositorio publicacionRepositorio;
    private final ComentarioRepositorio comentarioRepositorio;
    private final MapeadorSmgt mapeadorSmgt;
    private final ServicioImportacionCsv servicioImportacionCsv;
    private final ServicioReportesAdministracion servicioReportesAdministracion;

    public ServicioAdministracion(
        UsuarioRepositorio usuarioRepositorio,
        GrupoInvestigacionRepositorio grupoInvestigacionRepositorio,
        SolicitudIngresoRepositorio solicitudIngresoRepositorio,
        PublicacionRepositorio publicacionRepositorio,
        ComentarioRepositorio comentarioRepositorio,
        MapeadorSmgt mapeadorSmgt,
        ServicioImportacionCsv servicioImportacionCsv,
        ServicioReportesAdministracion servicioReportesAdministracion
    ) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.grupoInvestigacionRepositorio = grupoInvestigacionRepositorio;
        this.solicitudIngresoRepositorio = solicitudIngresoRepositorio;
        this.publicacionRepositorio = publicacionRepositorio;
        this.comentarioRepositorio = comentarioRepositorio;
        this.mapeadorSmgt = mapeadorSmgt;
        this.servicioImportacionCsv = servicioImportacionCsv;
        this.servicioReportesAdministracion = servicioReportesAdministracion;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto> listarUsuarios(Long directorId) {
        validarDirector(directorId);
        return usuarioRepositorio.findAllByOrderByNombreAsc().stream()
            .map(mapeadorSmgt::aUsuarioDto)
            .toList();
    }

    public UsuarioDto crearUsuario(Long directorId, UsuarioFormularioDto formulario) {
        validarDirector(directorId);

        if (usuarioRepositorio.existsByCorreoIgnoreCase(formulario.correo().trim())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo.");
        }

        String clave = limpiarTexto(formulario.clave());
        if (clave == null) {
            throw new IllegalArgumentException("La clave es obligatoria para crear el usuario.");
        }

        RolUsuario rol = convertirRol(formulario.rol());
        GrupoInvestigacion grupoAsignado = resolverGrupoEstudiante(rol, formulario.grupoId());
        Usuario usuario = new Usuario(
            formulario.nombre().trim(),
            formulario.correo().trim().toLowerCase(),
            clave,
            rol,
            resolverTemaTesisCreacion(rol, formulario.temaTesis(), grupoAsignado)
        );

        if (grupoAsignado != null) {
            usuario.setGrupoInvestigacion(grupoAsignado);
        }

        return mapeadorSmgt.aUsuarioDto(usuarioRepositorio.save(usuario));
    }

    public UsuarioDto actualizarUsuario(Long directorId, Long usuarioId, UsuarioFormularioDto formulario) {
        validarDirector(directorId);
        Usuario usuario = obtenerUsuario(usuarioId);

        if (!usuario.getCorreo().equalsIgnoreCase(formulario.correo().trim())
            && usuarioRepositorio.existsByCorreoIgnoreCase(formulario.correo().trim())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo.");
        }

        RolUsuario nuevoRol = convertirRol(formulario.rol());
        RolUsuario rolAnterior = usuario.getRol();
        if (usuario.getRol() == RolUsuario.TUTOR && nuevoRol != RolUsuario.TUTOR
            && grupoInvestigacionRepositorio.countByTutorId(usuario.getId()) > 0) {
            throw new IllegalArgumentException("No se puede cambiar el rol de un tutor con grupos asignados.");
        }

        usuario.setNombre(formulario.nombre().trim());
        usuario.setCorreo(formulario.correo().trim().toLowerCase());
        String nuevaClave = limpiarTexto(formulario.clave());
        if (nuevaClave != null) {
            usuario.setClave(nuevaClave);
        }
        usuario.setRol(nuevoRol);
        GrupoInvestigacion grupoAsignado = resolverGrupoEstudiante(nuevoRol, formulario.grupoId());

        if (grupoAsignado != null) {
            usuario.setGrupoInvestigacion(grupoAsignado);
            usuario.setTemaTesis(grupoAsignado.getTemaTesis());
        } else if (nuevoRol == RolUsuario.ESTUDIANTE) {
            usuario.setGrupoInvestigacion(null);
            usuario.setTemaTesis(null);
        } else if (nuevoRol != RolUsuario.ESTUDIANTE) {
            usuario.setGrupoInvestigacion(null);
            if (rolAnterior == RolUsuario.ESTUDIANTE) {
                usuario.setTemaTesis(null);
            } else {
                String temaManual = limpiarTexto(formulario.temaTesis());
                if (temaManual != null) {
                    usuario.setTemaTesis(temaManual);
                }
            }
        }

        return mapeadorSmgt.aUsuarioDto(usuarioRepositorio.save(usuario));
    }

    public void eliminarUsuario(Long directorId, Long usuarioId) {
        Usuario director = validarDirector(directorId);
        Usuario usuario = obtenerUsuario(usuarioId);

        if (director.getId().equals(usuario.getId()) && usuarioRepositorio.countByRol(RolUsuario.DIRECTOR) <= 1) {
            throw new IllegalArgumentException("No se puede eliminar el unico director del sistema.");
        }

        if (usuario.getRol() == RolUsuario.TUTOR && grupoInvestigacionRepositorio.countByTutorId(usuario.getId()) > 0) {
            throw new IllegalArgumentException("No se puede eliminar un tutor con grupos activos.");
        }

        List<Publicacion> publicaciones = publicacionRepositorio.findAllByAutorId(usuario.getId());
        for (Publicacion publicacion : publicaciones) {
            comentarioRepositorio.deleteByPublicacionId(publicacion.getId());
            publicacionRepositorio.delete(publicacion);
        }

        comentarioRepositorio.deleteByAutorId(usuario.getId());
        solicitudIngresoRepositorio.deleteByEstudianteId(usuario.getId());
        usuario.setGrupoInvestigacion(null);
        usuarioRepositorio.delete(usuario);
    }

    @Transactional(readOnly = true)
    public ResumenAdministracionDto obtenerResumen(Long directorId) {
        validarDirector(directorId);
        return new ResumenAdministracionDto(
            usuarioRepositorio.count(),
            usuarioRepositorio.countByRol(RolUsuario.TUTOR),
            usuarioRepositorio.countByRol(RolUsuario.ESTUDIANTE),
            grupoInvestigacionRepositorio.count(),
            solicitudIngresoRepositorio.countByEstado(cl.smgt.dominio.EstadoSolicitud.PENDIENTE),
            publicacionRepositorio.count()
        );
    }

    public ResultadoImportacionDto importarAlumnos(Long directorId, MultipartFile archivo) {
        validarDirector(directorId);
        return servicioImportacionCsv.importarAlumnos(archivo);
    }

    @Transactional(readOnly = true)
    public ReporteAdministracionDto obtenerReporte(Long directorId) {
        validarDirector(directorId);
        return servicioReportesAdministracion.construirReporte();
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto> listarUsuariosPorGrupo(Long grupoId) {
        return usuarioRepositorio.findByGrupoInvestigacionIdOrderByNombreAsc(grupoId).stream()
            .map(mapeadorSmgt::aUsuarioDto)
            .toList();
    }

    private Usuario validarDirector(Long directorId) {
        Usuario usuario = obtenerUsuario(directorId);
        if (usuario.getRol() != RolUsuario.DIRECTOR) {
            throw new IllegalArgumentException("Solo el director puede realizar esta accion.");
        }
        return usuario;
    }

    private Usuario obtenerUsuario(Long usuarioId) {
        return usuarioRepositorio.findById(usuarioId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
    }

    private RolUsuario convertirRol(String valor) {
        try {
            return RolUsuario.valueOf(valor.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Rol invalido. Use DIRECTOR, TUTOR o ESTUDIANTE.");
        }
    }

    private GrupoInvestigacion resolverGrupoEstudiante(RolUsuario rol, Long grupoId) {
        if (rol != RolUsuario.ESTUDIANTE || grupoId == null) {
            return null;
        }
        return grupoInvestigacionRepositorio.findById(grupoId)
            .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado."));
    }

    private String resolverTemaTesisCreacion(RolUsuario rol, String temaTesis, GrupoInvestigacion grupoAsignado) {
        if (rol == RolUsuario.ESTUDIANTE) {
            return grupoAsignado != null ? grupoAsignado.getTemaTesis() : null;
        }
        return limpiarTexto(temaTesis);
    }

    private String limpiarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
