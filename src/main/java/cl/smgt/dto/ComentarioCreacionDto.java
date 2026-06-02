package cl.smgt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ComentarioCreacionDto(
    @NotNull Long autorId,
    @NotBlank String mensaje,
    Boolean consejoTutor
) {
}
