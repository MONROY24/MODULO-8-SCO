package com.sistemacontable.modulo8.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MÓDULO 8 – Análisis Predictivo con IA
 * Configuración centralizada: lee credenciales y parámetros de
 * {@code config/db.properties} para evitar valores hardcodeados.
 *
 * Orden de búsqueda del archivo:
 *   1. Classpath raíz (útil al ejecutar con Ant desde /config)
 *   2. Variable de entorno DB_URL / DB_USER / DB_PASSWORD (override)
 */
public final class AppConfig {

    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final String CONFIG_FILE = "db.properties";
    private static final Properties props    = new Properties();

    static {
        cargarProperties();
    }

    private AppConfig() {} // Clase de utilidad – no instanciar

    // ── Carga ─────────────────────────────────────────────────────────────────

    private static void cargarProperties() {
        try (InputStream is = AppConfig.class.getClassLoader()
                                             .getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                props.load(is);
            } else {
                LOGGER.warning("No se encontró '" + CONFIG_FILE + "' en el classpath. Usando valores por defecto.");
                cargarValoresPorDefecto();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al leer configuración", e);
            cargarValoresPorDefecto();
        }
    }

    private static void cargarValoresPorDefecto() {
        props.setProperty("db.driver", "org.mariadb.jdbc.Driver");
        props.setProperty("db.url",    "jdbc:mariadb://localhost:3306/dbsco?useSSL=false&serverTimezone=UTC");
        props.setProperty("db.user",   "root");
        props.setProperty("db.password", "");
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    /** Clase del driver JDBC (ej: {@code com.mysql.cj.jdbc.Driver}). */
    public static String getDbDriver() {
        return getEnvOrProp("DB_DRIVER", "db.driver");
    }

    /** URL JDBC completa construida con variables de Render (o la propiedad por defecto). */
    public static String getDbUrl() {
        String host = System.getenv("DB_HOST");
        String port = System.getenv("DB_PORT");
        String name = System.getenv("DB_NAME");

        // Si Render inyecta DB_HOST, construimos la URL dinámicamente como lo haría Spring
        if (host != null && !host.isBlank()) {
            String p = (port != null && !port.isBlank()) ? port : "3306";
            String n = (name != null && !name.isBlank()) ? name : "dbsco";
            return "jdbc:mariadb://" + host + ":" + p + "/" + n + "?useSSL=false&allowPublicKeyRetrieval=true";
        }

        // Fallback a variable de entorno completa o archivo properties local
        return getEnvOrProp("DB_URL", "db.url");
    }

    /** Usuario de base de datos. */
    public static String getDbUser() {
        return getEnvOrProp("DB_USER", "db.user");
    }

    /** Contraseña de base de datos. */
    public static String getDbPassword() {
        return getEnvOrProp("DB_PASSWORD", "db.password");
    }

    // ── Helper: variable de entorno tiene precedencia sobre properties ─────────

    private static String getEnvOrProp(String envKey, String propKey) {
        String envVal = System.getenv(envKey);
        return (envVal != null && !envVal.isBlank()) ? envVal : props.getProperty(propKey, "");
    }
}
