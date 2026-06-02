package cl.smgt.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.smgt.dominio.GrupoInvestigacion;

public interface GrupoInvestigacionRepositorio extends JpaRepository<GrupoInvestigacion, Long> {

    List<GrupoInvestigacion> findAllByOrderByNombreAsc();

    List<GrupoInvestigacion> findByTutorIdOrderByNombreAsc(Long tutorId);

    long countByTutorId(Long tutorId);
}
