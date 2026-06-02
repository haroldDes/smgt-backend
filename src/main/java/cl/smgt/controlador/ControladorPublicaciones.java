package cl.smgt.controlador;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.smgt.dto.ComentarioCreacionDto;
import cl.smgt.dto.ComentarioDto;
import cl.smgt.dto.PublicacionCreacionDto;
import cl.smgt.dto.PublicacionDto;
import cl.smgt.servicio.ServicioPublicacion;

@RestController
@RequestMapping("/api/publicaciones")
public class ControladorPublicaciones {

    private final ServicioPublicacion servicioPublicacion;

    public ControladorPublicaciones(ServicioPublicacion servicioPublicacion) {
        this.servicioPublicacion = servicioPublicacion;
    }

    @PostMapping
    public PublicacionDto crearPublicacion(@Validated @RequestBody PublicacionCreacionDto formulario) {
        return servicioPublicacion.crearPublicacion(formulario);
    }

    @PostMapping("/{publicacionId}/comentarios")
    public ComentarioDto comentar(@PathVariable Long publicacionId, @Validated @RequestBody ComentarioCreacionDto formulario) {
        return servicioPublicacion.agregarComentario(publicacionId, formulario);
    }
}
