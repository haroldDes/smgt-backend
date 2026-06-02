package cl.smgt.controlador;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.smgt.dto.GrupoTableroDto;
import cl.smgt.dto.PanelUsuarioDto;
import cl.smgt.servicio.ServicioTablero;

@RestController
@RequestMapping("/api/tablero")
public class ControladorTablero {

    private final ServicioTablero servicioTablero;

    public ControladorTablero(ServicioTablero servicioTablero) {
        this.servicioTablero = servicioTablero;
    }

    @GetMapping("/panel/{usuarioId}")
    public PanelUsuarioDto obtenerPanel(@PathVariable Long usuarioId) {
        return servicioTablero.obtenerPanel(usuarioId);
    }

    @GetMapping("/grupos")
    public List<GrupoTableroDto> listarTableroGeneral() {
        return servicioTablero.listarTableroGeneral();
    }
}
