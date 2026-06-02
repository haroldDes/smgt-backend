package cl.smgt.dto;

public record GrupoResumenDto(
    Long id,
    String nombre,
    String temaTesis,
    String descripcion,
    String colorHex,
    String icono,
    Long tutorId,
    String tutorNombre,
    int totalAlumnos,
    int totalSolicitudesPendientes
) {
}
