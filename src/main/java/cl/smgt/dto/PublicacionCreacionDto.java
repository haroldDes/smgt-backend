package cl.smgt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PublicacionCreacionDto(
    @NotNull Long grupoId,
    @NotNull Long autorId,
    @NotBlank String titulo,
    @NotBlank String resumen,
    @NotBlank String etapa,
    @NotNull Integer anio,
    @NotBlank String ciudad,
    @NotBlank String editorial,
    String doi,
    String enlace
) {
}
