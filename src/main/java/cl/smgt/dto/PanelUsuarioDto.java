package cl.smgt.dto;

import java.util.List;

public record PanelUsuarioDto(
    UsuarioSesionDto usuario,
    ResumenAdministracionDto resumenAdministracion,
    ReporteAdministracionDto reporteAdministracion,
    List<UsuarioDto> usuarios,
    List<GrupoTableroDto> grupos,
    List<UsuarioRankingDto> ranking,
    List<SolicitudDto> notificacionesTutor,
    List<ComentarioPrioritarioDto> comentariosPrioritarios
) {
}
