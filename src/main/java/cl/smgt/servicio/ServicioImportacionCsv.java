package cl.smgt.servicio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import cl.smgt.dominio.RolUsuario;
import cl.smgt.dominio.Usuario;
import cl.smgt.dto.ResultadoImportacionDto;
import cl.smgt.repositorio.UsuarioRepositorio;

@Service
@Transactional
public class ServicioImportacionCsv {

    private final UsuarioRepositorio usuarioRepositorio;

    public ServicioImportacionCsv(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public ResultadoImportacionDto importarAlumnos(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar un archivo CSV o TXT.");
        }

        int totalLineas = 0;
        int creados = 0;
        int omitidos = 0;
        List<String> errores = new ArrayList<>();

        try (BufferedReader lector = new BufferedReader(new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                totalLineas++;

                if (linea.isBlank()) {
                    omitidos++;
                    continue;
                }

                if (totalLineas == 1 && linea.toLowerCase().contains("nombre") && linea.toLowerCase().contains("correo")) {
                    continue;
                }

                String[] columnas = linea.split(Pattern.quote(detectarSeparador(linea)));
                if (columnas.length < 2) {
                    omitidos++;
                    errores.add("Linea " + totalLineas + ": faltan columnas obligatorias.");
                    continue;
                }

                String nombre = columnas[0].trim();
                String correo = columnas[1].trim().toLowerCase();
                String temaTesis = columnas.length > 2 ? columnas[2].trim() : "Investigacion aplicada";
                String clave = columnas.length > 3 && !columnas[3].isBlank() ? columnas[3].trim() : "clave123";

                if (nombre.isBlank() || correo.isBlank()) {
                    omitidos++;
                    errores.add("Linea " + totalLineas + ": nombre o correo vacio.");
                    continue;
                }

                if (usuarioRepositorio.existsByCorreoIgnoreCase(correo)) {
                    omitidos++;
                    continue;
                }

                usuarioRepositorio.save(new Usuario(nombre, correo, clave, RolUsuario.ESTUDIANTE, temaTesis));
                creados++;
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("No se pudo leer el archivo de importacion.");
        }

        return new ResultadoImportacionDto(totalLineas, creados, omitidos, errores);
    }

    private String detectarSeparador(String linea) {
        if (linea.contains(";")) {
            return ";";
        }
        if (linea.contains("\t")) {
            return "\t";
        }
        return ",";
    }
}
