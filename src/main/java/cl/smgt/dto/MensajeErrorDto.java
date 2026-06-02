package cl.smgt.dto;

import java.time.LocalDateTime;

public record MensajeErrorDto(
    LocalDateTime fecha,
    String mensaje,
    String detalle
) {
}
