package cl.smgt.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.smgt.dominio.Publicacion;

public interface PublicacionRepositorio extends JpaRepository<Publicacion, Long> {

    List<Publicacion> findByGrupoIdOrderByFechaCreacionDesc(Long grupoId);

    List<Publicacion> findAllByGrupoId(Long grupoId);

    List<Publicacion> findAllByAutorId(Long autorId);

    long countByGrupoId(Long grupoId);

    void deleteByGrupoId(Long grupoId);
}
