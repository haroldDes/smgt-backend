package cl.smgt.dto;

public record UsuarioSesionDto(
    Long id,
    String nombre,
    String correo,
    String rol,
    Long grupoId,
    String grupoNombre
) {
}
