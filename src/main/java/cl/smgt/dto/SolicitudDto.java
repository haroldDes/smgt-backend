package cl.smgt.dto;

public record SolicitudDto(
    Long id,
    Long estudianteId,
    String estudianteNombre,
    Long grupoId,
    String grupoNombre,
    String estado,
    String fechaSolicitud
) {
}
