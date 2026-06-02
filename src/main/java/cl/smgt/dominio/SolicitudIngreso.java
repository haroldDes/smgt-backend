package cl.smgt.dominio;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "solicitudes_ingreso")
public class SolicitudIngreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Usuario estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    private GrupoInvestigacion grupo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    protected SolicitudIngreso() {
    }

    public SolicitudIngreso(Usuario estudiante, GrupoInvestigacion grupo, EstadoSolicitud estado, LocalDateTime fechaSolicitud) {
        this.estudiante = estudiante;
        this.grupo = grupo;
        this.estado = estado;
        this.fechaSolicitud = fechaSolicitud;
    }

    public Long getId() {
        return id;
    }

    public Usuario getEstudiante() {
        return estudiante;
    }

    public GrupoInvestigacion getGrupo() {
        return grupo;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }
}
