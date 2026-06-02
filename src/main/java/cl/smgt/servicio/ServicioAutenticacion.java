package cl.smgt.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.smgt.dominio.Usuario;
import cl.smgt.dto.CredencialesLoginDto;
import cl.smgt.dto.UsuarioSesionDto;
import cl.smgt.repositorio.UsuarioRepositorio;

@Service
@Transactional(readOnly = true)
public class ServicioAutenticacion {

    private final UsuarioRepositorio usuarioRepositorio;
    private final MapeadorSmgt mapeadorSmgt;

    public ServicioAutenticacion(UsuarioRepositorio usuarioRepositorio, MapeadorSmgt mapeadorSmgt) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.mapeadorSmgt = mapeadorSmgt;
    }

    public UsuarioSesionDto iniciarSesion(CredencialesLoginDto credenciales) {
        Usuario usuario = usuarioRepositorio.findByCorreoIgnoreCase(credenciales.correo().trim())
            .orElseThrow(() -> new IllegalArgumentException("Correo o clave invalida."));

        if (!usuario.getClave().equals(credenciales.clave().trim())) {
            throw new IllegalArgumentException("Correo o clave invalida.");
        }

        return mapeadorSmgt.aSesion(usuario);
    }
}
