package cl.smgt.dto;

import java.util.List;

public record ReporteAdministracionDto(
    long totalComentarios,
    long comentariosPendientesTutor,
    long estudiantesConGrupo,
    long estudiantesSinGrupo,
    long tutoresConGrupos,
    double promedioPuntajeAyuda,
    double promedioPublicacionesPorGrupo,
    List<SerieMetricaDto> usuariosPorRol,
    List<SerieMetricaDto> publicacionesPorEtapa,
    List<SerieMetricaDto> lineasInvestigacion,
    List<GrupoMetricaDto> gruposDestacados
) {
}
