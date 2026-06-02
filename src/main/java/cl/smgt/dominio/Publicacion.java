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
@Table(name = "publicaciones")
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    private GrupoInvestigacion grupo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(nullable = false, length = 180)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String resumen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EtapaPublicacion etapa;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false, length = 100)
    private String ciudad;

    @Column(nullable = false, length = 120)
    private String editorial;

    @Column(length = 120)
    private String doi;

    @Column(length = 255)
    private String enlace;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    protected Publicacion() {
    }

    public Publicacion(
        GrupoInvestigacion grupo,
        Usuario autor,
        String titulo,
        String resumen,
        EtapaPublicacion etapa,
        Integer anio,
        String ciudad,
        String editorial,
        String doi,
        String enlace,
        LocalDateTime fechaCreacion
    ) {
        this.grupo = grupo;
        this.autor = autor;
        this.titulo = titulo;
        this.resumen = resumen;
        this.etapa = etapa;
        this.anio = anio;
        this.ciudad = ciudad;
        this.editorial = editorial;
        this.doi = doi;
        this.enlace = enlace;
        this.fechaCreacion = fechaCreacion;
    }

    public Long getId() {
        return id;
    }

    public GrupoInvestigacion getGrupo() {
        return grupo;
    }

    public Usuario getAutor() {
        return autor;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getResumen() {
        return resumen;
    }

    public void setResumen(String resumen) {
        this.resumen = resumen;
    }

    public EtapaPublicacion getEtapa() {
        return etapa;
    }

    public void setEtapa(EtapaPublicacion etapa) {
        this.etapa = etapa;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getEnlace() {
        return enlace;
    }

    public void setEnlace(String enlace) {
        this.enlace = enlace;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
