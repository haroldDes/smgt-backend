package cl.smgt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CredencialesLoginDto(
    @Email @NotBlank String correo,
    @NotBlank String clave
) {
}
