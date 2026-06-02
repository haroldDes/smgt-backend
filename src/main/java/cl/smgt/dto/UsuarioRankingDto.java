package cl.smgt.dto;

public record UsuarioRankingDto(
    Long id,
    String nombre,
    String rol,
    Integer puntajeAyuda
) {
}
