package cl.smgt.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConfiguracionCors implements WebMvcConfigurer {

    private final String origenesPermitidos;

    public ConfiguracionCors(@Value("${smgt.cors.allowed-origins:*}") String origenesPermitidos) {
        this.origenesPermitidos = origenesPermitidos;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origenes = origenesPermitidos.split(",");
        for (int indice = 0; indice < origenes.length; indice++) {
            origenes[indice] = origenes[indice].trim();
        }

        registry.addMapping("/api/**")
            .allowedOrigins(origenes)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*");
    }
}
