package cl.smgt.dto;

public record ComentarioPrioritarioDto(
    Long comentarioId,
    Long publicacionId,
    String publicacionTitulo,
    Long grupoId,
    String grupoNombre,
    Long estudianteId,
    String estudianteNombre,
    String mensaje,
    String fechaCreacion
) {
}
