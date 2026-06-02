package cl.smgt.dominio;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "comentarios")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publicacion_id", nullable = false)
    private Publicacion publicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(nullable = false)
    private boolean consejoTutor;

    @Column(nullable = false)
    private boolean atendidoPorTutor;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    protected Comentario() {
    }

    public Comentario(Publicacion publicacion, Usuario autor, String mensaje, boolean consejoTutor, boolean atendidoPorTutor, LocalDateTime fechaCreacion) {
        this.publicacion = publicacion;
        this.autor = autor;
        this.mensaje = mensaje;
        this.consejoTutor = consejoTutor;
        this.atendidoPorTutor = atendidoPorTutor;
        this.fechaCreacion = fechaCreacion;
    }

    public Long getId() {
        return id;
    }

    public Publicacion getPublicacion() {
        return publicacion;
    }

    public Usuario getAutor() {
        return autor;
    }

    public String getMensaje() {
        return mensaje;
    }

    public boolean isConsejoTutor() {
        return consejoTutor;
    }

    public void setConsejoTutor(boolean consejoTutor) {
        this.consejoTutor = consejoTutor;
    }

    public boolean isAtendidoPorTutor() {
        return atendidoPorTutor;
    }

    public void setAtendidoPorTutor(boolean atendidoPorTutor) {
        this.atendidoPorTutor = atendidoPorTutor;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
