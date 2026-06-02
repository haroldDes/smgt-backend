package cl.smgt.dto;

import java.util.List;

public record GrupoTableroDto(
    GrupoResumenDto grupo,
    List<UsuarioDto> alumnos,
    List<PublicacionDto> publicaciones,
    List<SolicitudDto> solicitudesPendientes
) {
}
