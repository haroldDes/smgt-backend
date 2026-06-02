SET NAMES utf8mb4;
SET time_zone = '-04:00';

DROP DATABASE IF EXISTS smgt;
CREATE DATABASE smgt CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smgt;

CREATE TABLE usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    correo VARCHAR(120) NOT NULL,
    clave VARCHAR(120) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    puntaje_ayuda INT NOT NULL DEFAULT 0,
    tema_tesis VARCHAR(255) NULL,
    grupo_id BIGINT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuarios_correo (correo),
    KEY idx_usuarios_rol (rol),
    KEY idx_usuarios_grupo (grupo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE grupos_investigacion (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(140) NOT NULL,
    tema_tesis VARCHAR(255) NOT NULL,
    descripcion TEXT NULL,
    color_hex VARCHAR(20) NOT NULL,
    icono VARCHAR(180) NOT NULL,
    tutor_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_grupos_tutor (tutor_id),
    CONSTRAINT fk_grupos_tutor
        FOREIGN KEY (tutor_id) REFERENCES usuarios (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE usuarios
    ADD CONSTRAINT fk_usuarios_grupo
        FOREIGN KEY (grupo_id) REFERENCES grupos_investigacion (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL;

CREATE TABLE solicitudes_ingreso (
    id BIGINT NOT NULL AUTO_INCREMENT,
    estudiante_id BIGINT NOT NULL,
    grupo_id BIGINT NOT NULL,
    estado VARCHAR(20) NOT NULL,
    fecha_solicitud DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_solicitudes_estudiante (estudiante_id),
    KEY idx_solicitudes_grupo (grupo_id),
    KEY idx_solicitudes_estado (estado),
    CONSTRAINT fk_solicitudes_estudiante
        FOREIGN KEY (estudiante_id) REFERENCES usuarios (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_solicitudes_grupo
        FOREIGN KEY (grupo_id) REFERENCES grupos_investigacion (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE publicaciones (
    id BIGINT NOT NULL AUTO_INCREMENT,
    grupo_id BIGINT NOT NULL,
    autor_id BIGINT NOT NULL,
    titulo VARCHAR(180) NOT NULL,
    resumen TEXT NOT NULL,
    etapa VARCHAR(20) NOT NULL,
    anio INT NOT NULL,
    ciudad VARCHAR(100) NOT NULL,
    editorial VARCHAR(120) NOT NULL,
    doi VARCHAR(120) NULL,
    enlace VARCHAR(255) NULL,
    fecha_creacion DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_publicaciones_grupo (grupo_id),
    KEY idx_publicaciones_autor (autor_id),
    KEY idx_publicaciones_etapa (etapa),
    CONSTRAINT fk_publicaciones_grupo
        FOREIGN KEY (grupo_id) REFERENCES grupos_investigacion (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_publicaciones_autor
        FOREIGN KEY (autor_id) REFERENCES usuarios (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE comentarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    publicacion_id BIGINT NOT NULL,
    autor_id BIGINT NOT NULL,
    mensaje TEXT NOT NULL,
    consejo_tutor TINYINT(1) NOT NULL DEFAULT 0,
    atendido_por_tutor TINYINT(1) NOT NULL DEFAULT 0,
    fecha_creacion DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_comentarios_publicacion (publicacion_id),
    KEY idx_comentarios_autor (autor_id),
    CONSTRAINT fk_comentarios_publicacion
        FOREIGN KEY (publicacion_id) REFERENCES publicaciones (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_comentarios_autor
        FOREIGN KEY (autor_id) REFERENCES usuarios (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
