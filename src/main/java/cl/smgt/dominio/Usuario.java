package cl.smgt.dominio;

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
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 120)
    private String correo;

    @Column(nullable = false, length = 120)
    private String clave;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RolUsuario rol;

    @Column(nullable = false)
    private Integer puntajeAyuda = 0;

    @Column(length = 255)
    private String temaTesis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id")
    private GrupoInvestigacion grupoInvestigacion;

    protected Usuario() {
    }

    public Usuario(String nombre, String correo, String clave, RolUsuario rol, String temaTesis) {
        this.nombre = nombre;
        this.correo = correo;
        this.clave = clave;
        this.rol = rol;
        this.temaTesis = temaTesis;
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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public Integer getPuntajeAyuda() {
        return puntajeAyuda;
    }

    public void setPuntajeAyuda(Integer puntajeAyuda) {
        this.puntajeAyuda = puntajeAyuda;
    }

    public void sumarPuntaje(int incremento) {
        this.puntajeAyuda = (this.puntajeAyuda == null ? 0 : this.puntajeAyuda) + incremento;
    }

    public String getTemaTesis() {
        return temaTesis;
    }

    public void setTemaTesis(String temaTesis) {
        this.temaTesis = temaTesis;
    }

    public GrupoInvestigacion getGrupoInvestigacion() {
        return grupoInvestigacion;
    }

    public void setGrupoInvestigacion(GrupoInvestigacion grupoInvestigacion) {
        this.grupoInvestigacion = grupoInvestigacion;
    }
}
