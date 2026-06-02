package cl.smgt.dto;

public record ComentarioDto(
    Long id,
    Long autorId,
    String autorNombre,
    String rolAutor,
    String mensaje,
    boolean consejoTutor,
    boolean atendidoPorTutor,
    String fechaCreacion
) {
}
