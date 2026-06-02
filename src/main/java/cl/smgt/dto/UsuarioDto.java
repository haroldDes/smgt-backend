package cl.smgt.dto;

public record UsuarioDto(
    Long id,
    String nombre,
    String correo,
    String rol,
    Integer puntajeAyuda,
    String temaTesis,
    Long grupoId,
    String grupoNombre
) {
}
