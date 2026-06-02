package cl.smgt.servicio;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.smgt.dominio.Comentario;
import cl.smgt.dominio.EstadoSolicitud;
import cl.smgt.dominio.EtapaPublicacion;
import cl.smgt.dominio.GrupoInvestigacion;
import cl.smgt.dominio.Publicacion;
import cl.smgt.dominio.RolUsuario;
import cl.smgt.dominio.SolicitudIngreso;
import cl.smgt.dominio.Usuario;
import cl.smgt.dto.GrupoMetricaDto;
import cl.smgt.dto.ReporteAdministracionDto;
import cl.smgt.dto.SerieMetricaDto;
import cl.smgt.repositorio.ComentarioRepositorio;
import cl.smgt.repositorio.GrupoInvestigacionRepositorio;
import cl.smgt.repositorio.PublicacionRepositorio;
import cl.smgt.repositorio.SolicitudIngresoRepositorio;
import cl.smgt.repositorio.UsuarioRepositorio;

@Service
@Transactional(readOnly = true)
public class ServicioReportesAdministracion {

    private final UsuarioRepositorio usuarioRepositorio;
    private final GrupoInvestigacionRepositorio grupoInvestigacionRepositorio;
    private final PublicacionRepositorio publicacionRepositorio;
    private final ComentarioRepositorio comentarioRepositorio;
    private final SolicitudIngresoRepositorio solicitudIngresoRepositorio;

    public ServicioReportesAdministracion(
        UsuarioRepositorio usuarioRepositorio,
        GrupoInvestigacionRepositorio grupoInvestigacionRepositorio,
        PublicacionRepositorio publicacionRepositorio,
        ComentarioRepositorio comentarioRepositorio,
        SolicitudIngresoRepositorio solicitudIngresoRepositorio
    ) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.grupoInvestigacionRepositorio = grupoInvestigacionRepositorio;
        this.publicacionRepositorio = publicacionRepositorio;
        this.comentarioRepositorio = comentarioRepositorio;
        this.solicitudIngresoRepositorio = solicitudIngresoRepositorio;
    }

    public ReporteAdministracionDto construirReporte() {
        List<Usuario> usuarios = usuarioRepositorio.findAll();
        List<Usuario> estudiantes = usuarios.stream()
            .filter(usuario -> usuario.getRol() == RolUsuario.ESTUDIANTE)
            .toList();
        List<GrupoInvestigacion> grupos = grupoInvestigacionRepositorio.findAllByOrderByNombreAsc();
        List<Publicacion> publicaciones = publicacionRepositorio.findAll();
        List<Comentario> comentarios = comentarioRepositorio.findAll();
        List<SolicitudIngreso> solicitudes = solicitudIngresoRepositorio.findAll();

        Map<Long, Long> publicacionesPorGrupo = publicaciones.stream()
            .collect(Collectors.groupingBy(publicacion -> publicacion.getGrupo().getId(), Collectors.counting()));
        Map<Long, Long> comentariosPorGrupo = comentarios.stream()
            .collect(Collectors.groupingBy(comentario -> comentario.getPublicacion().getGrupo().getId(), Collectors.counting()));
        Map<Long, Long> solicitudesPendientesPorGrupo = solicitudes.stream()
            .filter(solicitud -> solicitud.getEstado() == EstadoSolicitud.PENDIENTE)
            .collect(Collectors.groupingBy(solicitud -> solicitud.getGrupo().getId(), Collectors.counting()));
        Map<Long, Integer> alumnosPorGrupo = estudiantes.stream()
            .filter(estudiante -> estudiante.getGrupoInvestigacion() != null)
            .collect(Collectors.toMap(
                estudiante -> estudiante.getGrupoInvestigacion().getId(),
                estudiante -> 1,
                Integer::sum
            ));

        long estudiantesConGrupo = estudiantes.stream()
            .filter(estudiante -> estudiante.getGrupoInvestigacion() != null)
            .count();
        long estudiantesSinGrupo = estudiantes.size() - estudiantesConGrupo;
        long comentariosPendientesTutor = comentarios.stream()
            .filter(comentario -> comentario.getAutor().getRol() == RolUsuario.ESTUDIANTE && !comentario.isAtendidoPorTutor())
            .count();
        long tutoresConGrupos = grupos.stream()
            .map(GrupoInvestigacion::getTutor)
            .map(Usuario::getId)
            .distinct()
            .count();

        List<SerieMetricaDto> usuariosPorRol = List.of(
            serie("Directores", usuarios.stream().filter(usuario -> usuario.getRol() == RolUsuario.DIRECTOR).count(), "Control institucional"),
            serie("Tutores", usuarios.stream().filter(usuario -> usuario.getRol() == RolUsuario.TUTOR).count(), "Lideran grupos y seguimiento"),
            serie("Estudiantes", usuarios.stream().filter(usuario -> usuario.getRol() == RolUsuario.ESTUDIANTE).count(), "Participacion investigativa")
        );

        List<SerieMetricaDto> publicacionesPorEtapa = List.of(
            serie(
                "Propuesta",
                publicaciones.stream().filter(publicacion -> normalizarEtapa(publicacion.getEtapa()) == EtapaPublicacion.PROPUESTA).count(),
                "Aportes visibles en el tablero"
            ),
            serie(
                "En revision",
                publicaciones.stream().filter(publicacion -> normalizarEtapa(publicacion.getEtapa()) == EtapaPublicacion.EN_REVISION).count(),
                "Aportes que esperan revision"
            )
        );

        List<SerieMetricaDto> lineasInvestigacion = construirLineasInvestigacion(usuarios, grupos);

        List<GrupoMetricaDto> gruposDestacados = grupos.stream()
            .map(grupo -> new GrupoMetricaDto(
                grupo.getId(),
                grupo.getNombre(),
                grupo.getTutor().getNombre(),
                detectarLinea(grupo.getTemaTesis()),
                alumnosPorGrupo.getOrDefault(grupo.getId(), 0),
                publicacionesPorGrupo.getOrDefault(grupo.getId(), 0L),
                comentariosPorGrupo.getOrDefault(grupo.getId(), 0L),
                solicitudesPendientesPorGrupo.getOrDefault(grupo.getId(), 0L)
            ))
            .sorted(Comparator
                .comparingLong((GrupoMetricaDto grupo) -> puntajeActividad(grupo))
                .reversed()
                .thenComparing(GrupoMetricaDto::nombre))
            .limit(4)
            .toList();

        double promedioPuntajeAyuda = usuarios.isEmpty()
            ? 0
            : redondear((double) usuarios.stream().mapToInt(usuario -> usuario.getPuntajeAyuda() == null ? 0 : usuario.getPuntajeAyuda()).sum() / usuarios.size());
        double promedioPublicacionesPorGrupo = grupos.isEmpty()
            ? 0
            : redondear((double) publicaciones.size() / grupos.size());

        return new ReporteAdministracionDto(
            comentarios.size(),
            comentariosPendientesTutor,
            estudiantesConGrupo,
            estudiantesSinGrupo,
            tutoresConGrupos,
            promedioPuntajeAyuda,
            promedioPublicacionesPorGrupo,
            usuariosPorRol,
            publicacionesPorEtapa,
            lineasInvestigacion,
            gruposDestacados
        );
    }

    private List<SerieMetricaDto> construirLineasInvestigacion(List<Usuario> usuarios, List<GrupoInvestigacion> grupos) {
        Map<String, Long> conteo = new LinkedHashMap<>();
        conteo.put("Derecho y regulacion", 0L);
        conteo.put("Salud y biomedica", 0L);
        conteo.put("Redes y datos", 0L);
        conteo.put("Otras lineas", 0L);

        grupos.stream()
            .map(GrupoInvestigacion::getTemaTesis)
            .forEach(tema -> conteo.compute(detectarLinea(tema), (clave, valor) -> valor == null ? 1L : valor + 1));

        usuarios.stream()
            .map(Usuario::getTemaTesis)
            .filter(tema -> tema != null && !tema.isBlank())
            .forEach(tema -> conteo.compute(detectarLinea(tema), (clave, valor) -> valor == null ? 1L : valor + 1));

        Map<String, String> descripciones = Map.of(
            "Derecho y regulacion", "Temas legales, normativos y de justicia",
            "Salud y biomedica", "Temas de salud, laboratorio y prevencion",
            "Redes y datos", "Temas de sistemas, datos y colaboracion",
            "Otras lineas", "Temas sin patron dominante"
        );

        return conteo.entrySet().stream()
            .map(entry -> serie(entry.getKey(), entry.getValue(), descripciones.getOrDefault(entry.getKey(), "Analisis de temas")))
            .sorted(Comparator.comparingLong(SerieMetricaDto::valor).reversed())
            .toList();
    }

    private long puntajeActividad(GrupoMetricaDto grupo) {
        return (grupo.totalPublicaciones() * 5L)
            + (grupo.totalComentarios() * 2L)
            + grupo.totalAlumnos()
            + (grupo.totalSolicitudesPendientes() * 3L);
    }

    private SerieMetricaDto serie(String etiqueta, long valor, String descripcion) {
        return new SerieMetricaDto(etiqueta, valor, descripcion);
    }

    private EtapaPublicacion normalizarEtapa(EtapaPublicacion etapa) {
        if (etapa == EtapaPublicacion.PUBLICADA) {
            return EtapaPublicacion.PROPUESTA;
        }
        return etapa;
    }

    private double redondear(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }

    private String detectarLinea(String tema) {
        String normalizado = normalizarTexto(tema);
        if (contiene(normalizado, "derecho", "jurid", "ley", "penal", "civil", "justicia", "regulacion")) {
            return "Derecho y regulacion";
        }
        if (contiene(normalizado, "micro", "salud", "medic", "biomed", "quim", "laboratorio", "prevent")) {
            return "Salud y biomedica";
        }
        if (contiene(normalizado, "red", "dato", "analitic", "sistema", "digital", "automat", "colaboracion")) {
            return "Redes y datos";
        }
        return "Otras lineas";
    }

    private String normalizarTexto(String texto) {
        String valor = texto == null ? "" : texto.trim().toLowerCase();
        return Normalizer.normalize(valor, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private boolean contiene(String texto, String... claves) {
        for (String clave : claves) {
            if (texto.contains(clave)) {
                return true;
            }
        }
        return false;
    }
}
