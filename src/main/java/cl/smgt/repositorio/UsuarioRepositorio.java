package cl.smgt.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.smgt.dominio.RolUsuario;
import cl.smgt.dominio.Usuario;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    long countByRol(RolUsuario rol);

    List<Usuario> findByRolOrderByNombreAsc(RolUsuario rol);

    List<Usuario> findByGrupoInvestigacionIdOrderByNombreAsc(Long grupoId);

    List<Usuario> findAllByOrderByPuntajeAyudaDescNombreAsc();

    List<Usuario> findAllByOrderByNombreAsc();
}
