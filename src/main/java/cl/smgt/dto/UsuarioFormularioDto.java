package cl.smgt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioFormularioDto(
    String nombre,
    @Email @NotBlank String correo,
    String clave,
    @NotBlank String rol,
    String temaTesis,
    Long grupoId
) {
}
