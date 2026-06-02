package cl.smgt.dominio;

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
@Table(name = "grupos_investigacion")
public class GrupoInvestigacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 140)
    private String nombre;

    @Column(nullable = false, length = 255)
    private String temaTesis;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, length = 20)
    private String colorHex;

    @Column(nullable = false, length = 180)
    private String icono;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Usuario tutor;

    protected GrupoInvestigacion() {
    }

    public GrupoInvestigacion(String nombre, String temaTesis, String descripcion, String colorHex, String icono, Usuario tutor) {
        this.nombre = nombre;
        this.temaTesis = temaTesis;
        this.descripcion = descripcion;
        this.colorHex = colorHex;
        this.icono = icono;
        this.tutor = tutor;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTemaTesis() {
        return temaTesis;
    }

    public void setTemaTesis(String temaTesis) {
        this.temaTesis = temaTesis;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public Usuario getTutor() {
        return tutor;
    }

    public void setTutor(Usuario tutor) {
        this.tutor = tutor;
    }
}
