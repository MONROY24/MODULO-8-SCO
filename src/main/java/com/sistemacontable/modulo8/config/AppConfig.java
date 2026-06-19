package com.sistemacontable.modulo8.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
                System.err.println("[AppConfig] ADVERTENCIA: No se encontró '"
                        + CONFIG_FILE + "' en el classpath. Usando valores por defecto.");
                cargarValoresPorDefecto();
            }
        } catch (IOException e) {
            System.err.println("[AppConfig] Error al leer configuración: " + e.getMessage());
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

    /** URL JDBC completa (ej: {@code jdbc:mysql://localhost:3306/dbsco}). */
    public static String getDbUrl() {
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
