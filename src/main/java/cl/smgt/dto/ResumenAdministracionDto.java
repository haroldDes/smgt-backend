package cl.smgt.dto;

public record ResumenAdministracionDto(
    long totalUsuarios,
    long totalTutores,
    long totalEstudiantes,
    long totalGrupos,
    long totalSolicitudesPendientes,
    long totalPublicaciones
) {
}
