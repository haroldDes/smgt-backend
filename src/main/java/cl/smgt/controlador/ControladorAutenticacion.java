package cl.smgt.controlador;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.smgt.dto.CredencialesLoginDto;
import cl.smgt.dto.UsuarioSesionDto;
import cl.smgt.servicio.ServicioAutenticacion;

@RestController
@RequestMapping("/api/autenticacion")
public class ControladorAutenticacion {

    private final ServicioAutenticacion servicioAutenticacion;

    public ControladorAutenticacion(ServicioAutenticacion servicioAutenticacion) {
        this.servicioAutenticacion = servicioAutenticacion;
    }

    @PostMapping("/login")
    public UsuarioSesionDto iniciarSesion(@Validated @RequestBody CredencialesLoginDto credenciales) {
        return servicioAutenticacion.iniciarSesion(credenciales);
    }
}
