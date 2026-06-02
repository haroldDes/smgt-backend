package cl.smgt.controlador;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.smgt.dto.GrupoFormularioDto;
import cl.smgt.dto.GrupoResumenDto;
import cl.smgt.dto.SolicitudDto;
import cl.smgt.servicio.ServicioGrupoInvestigacion;

@RestController
@RequestMapping("/api/grupos")
public class ControladorGrupos {

    private final ServicioGrupoInvestigacion servicioGrupoInvestigacion;

    public ControladorGrupos(ServicioGrupoInvestigacion servicioGrupoInvestigacion) {
        this.servicioGrupoInvestigacion = servicioGrupoInvestigacion;
    }

    @PostMapping
    public GrupoResumenDto crearGrupoTutor(@RequestParam Long usuarioId, @Validated @RequestBody GrupoFormularioDto formulario) {
        return servicioGrupoInvestigacion.crearGrupo(usuarioId, formulario);
    }

    @PutMapping("/{grupoId}")
    public GrupoResumenDto actualizarGrupoTutor(@PathVariable Long grupoId, @RequestParam Long usuarioId, @Validated @RequestBody GrupoFormularioDto formulario) {
        return servicioGrupoInvestigacion.actualizarGrupo(usuarioId, grupoId, formulario);
    }

    @PostMapping("/{grupoId}/solicitudes")
    public SolicitudDto solicitarIngreso(@PathVariable Long grupoId, @RequestParam Long usuarioId) {
        return servicioGrupoInvestigacion.solicitarIngreso(usuarioId, grupoId);
    }

    @PostMapping("/solicitudes/{solicitudId}/resolver")
    public SolicitudDto resolverSolicitud(@PathVariable Long solicitudId, @RequestParam Long usuarioId, @RequestParam boolean aprobar) {
        return servicioGrupoInvestigacion.resolverSolicitud(usuarioId, solicitudId, aprobar);
    }
}
