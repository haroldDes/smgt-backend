package cl.smgt.controlador;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cl.smgt.dto.MensajeErrorDto;

@RestControllerAdvice
public class ControladorErroresApi {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MensajeErrorDto> manejarArgumentos(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
            .body(new MensajeErrorDto(LocalDateTime.now(), ex.getMessage(), "Revise los datos enviados."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MensajeErrorDto> manejarValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .orElse("Solicitud invalida.");

        return ResponseEntity.badRequest()
            .body(new MensajeErrorDto(LocalDateTime.now(), mensaje, "Corrija el formulario."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MensajeErrorDto> manejarGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new MensajeErrorDto(LocalDateTime.now(), "Error interno del servidor.", ex.getMessage()));
    }
}
