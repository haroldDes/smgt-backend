package cl.smgt.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.smgt.dominio.EstadoSolicitud;
import cl.smgt.dominio.SolicitudIngreso;

public interface SolicitudIngresoRepositorio extends JpaRepository<SolicitudIngreso, Long> {

    List<SolicitudIngreso> findByGrupoTutorIdAndEstadoOrderByFechaSolicitudDesc(Long tutorId, EstadoSolicitud estado);

    List<SolicitudIngreso> findByGrupoIdAndEstadoOrderByFechaSolicitudDesc(Long grupoId, EstadoSolicitud estado);

    List<SolicitudIngreso> findByEstudianteIdOrderByFechaSolicitudDesc(Long estudianteId);

    Optional<SolicitudIngreso> findByEstudianteIdAndGrupoIdAndEstado(Long estudianteId, Long grupoId, EstadoSolicitud estado);

    long countByEstado(EstadoSolicitud estado);

    void deleteByGrupoId(Long grupoId);

    void deleteByEstudianteId(Long estudianteId);
}
