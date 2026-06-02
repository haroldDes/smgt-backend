package cl.smgt.dto;

import jakarta.validation.constraints.NotBlank;

public record GrupoFormularioDto(
    @NotBlank String nombre,
    @NotBlank String temaTesis,
    String descripcion,
    Long tutorId
) {
}
