package cl.smgt.dto;

import java.util.List;

public record ResultadoImportacionDto(
    int totalLineas,
    int creados,
    int omitidos,
    List<String> errores
) {
}
