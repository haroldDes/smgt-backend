package cl.smgt.dto;

public record GrupoMetricaDto(
    Long grupoId,
    String nombre,
    String tutorNombre,
    String lineaInvestigacion,
    int totalAlumnos,
    long totalPublicaciones,
    long totalComentarios,
    long totalSolicitudesPendientes
) {
}
