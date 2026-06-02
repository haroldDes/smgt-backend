package cl.smgt.dto;

import java.util.List;

public record PublicacionDto(
    Long id,
    Long grupoId,
    String grupoNombre,
    Long autorId,
    String autorNombre,
    String rolAutor,
    String titulo,
    String resumen,
    String etapa,
    Integer anio,
    String ciudad,
    String editorial,
    String doi,
    String enlace,
    String fechaCreacion,
    List<ComentarioDto> comentarios
) {
}
