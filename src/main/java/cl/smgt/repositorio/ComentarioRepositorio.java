package cl.smgt.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.smgt.dominio.Comentario;
import cl.smgt.dominio.RolUsuario;

public interface ComentarioRepositorio extends JpaRepository<Comentario, Long> {

    List<Comentario> findByPublicacionIdOrderByFechaCreacionAsc(Long publicacionId);

    List<Comentario> findByPublicacionGrupoTutorIdAndAtendidoPorTutorFalseAndAutorRolOrderByFechaCreacionDesc(Long tutorId, RolUsuario rol);

    void deleteByPublicacionId(Long publicacionId);

    void deleteByAutorId(Long autorId);
}
