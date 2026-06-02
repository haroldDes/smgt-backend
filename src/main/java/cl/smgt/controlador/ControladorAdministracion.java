package cl.smgt.controlador;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cl.smgt.dto.GrupoFormularioDto;
import cl.smgt.dto.GrupoResumenDto;
import cl.smgt.dto.ReporteAdministracionDto;
import cl.smgt.dto.ResumenAdministracionDto;
import cl.smgt.dto.ResultadoImportacionDto;
import cl.smgt.dto.UsuarioDto;
import cl.smgt.dto.UsuarioFormularioDto;
import cl.smgt.servicio.ServicioAdministracion;
import cl.smgt.servicio.ServicioGrupoInvestigacion;

@RestController
@RequestMapping("/api/administracion")
public class ControladorAdministracion {

    private final ServicioAdministracion servicioAdministracion;
    private final ServicioGrupoInvestigacion servicioGrupoInvestigacion;

    public ControladorAdministracion(ServicioAdministracion servicioAdministracion, ServicioGrupoInvestigacion servicioGrupoInvestigacion) {
        this.servicioAdministracion = servicioAdministracion;
        this.servicioGrupoInvestigacion = servicioGrupoInvestigacion;
    }

    @GetMapping("/usuarios")
    public List<UsuarioDto> listarUsuarios(@RequestParam Long usuarioId) {
        return servicioAdministracion.listarUsuarios(usuarioId);
    }

    @PostMapping("/usuarios")
    public UsuarioDto crearUsuario(@RequestParam Long usuarioId, @Validated @RequestBody UsuarioFormularioDto formulario) {
        return servicioAdministracion.crearUsuario(usuarioId, formulario);
    }

    @PutMapping("/usuarios/{id}")
    public UsuarioDto actualizarUsuario(@RequestParam Long usuarioId, @PathVariable Long id, @Validated @RequestBody UsuarioFormularioDto formulario) {
        return servicioAdministracion.actualizarUsuario(usuarioId, id, formulario);
    }

    @DeleteMapping("/usuarios/{id}")
    public void eliminarUsuario(@RequestParam Long usuarioId, @PathVariable Long id) {
        servicioAdministracion.eliminarUsuario(usuarioId, id);
    }

    @GetMapping("/resumen")
    public ResumenAdministracionDto obtenerResumen(@RequestParam Long usuarioId) {
        return servicioAdministracion.obtenerResumen(usuarioId);
    }

    @GetMapping("/reportes")
    public ReporteAdministracionDto obtenerReportes(@RequestParam Long usuarioId) {
        return servicioAdministracion.obtenerReporte(usuarioId);
    }

    @PostMapping("/importacion/alumnos")
    public ResultadoImportacionDto importarAlumnos(@RequestParam Long usuarioId, @RequestPart("archivo") MultipartFile archivo) {
        return servicioAdministracion.importarAlumnos(usuarioId, archivo);
    }

    @GetMapping("/grupos")
    public List<GrupoResumenDto> listarGrupos(@RequestParam Long usuarioId) {
        servicioAdministracion.obtenerResumen(usuarioId);
        return servicioGrupoInvestigacion.listarTodos();
    }

    @PostMapping("/grupos")
    public GrupoResumenDto crearGrupo(@RequestParam Long usuarioId, @Validated @RequestBody GrupoFormularioDto formulario) {
        return servicioGrupoInvestigacion.crearGrupo(usuarioId, formulario);
    }

    @PutMapping("/grupos/{id}")
    public GrupoResumenDto actualizarGrupo(@RequestParam Long usuarioId, @PathVariable Long id, @Validated @RequestBody GrupoFormularioDto formulario) {
        return servicioGrupoInvestigacion.actualizarGrupo(usuarioId, id, formulario);
    }

    @DeleteMapping("/grupos/{id}")
    public void eliminarGrupo(@RequestParam Long usuarioId, @PathVariable Long id) {
        servicioGrupoInvestigacion.eliminarGrupo(usuarioId, id);
    }
}
