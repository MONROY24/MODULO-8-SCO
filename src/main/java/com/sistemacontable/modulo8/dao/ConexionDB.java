package com.sistemacontable.modulo8.dao;

import com.sistemacontable.modulo8.config.AppConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MÓDULO 8 – Análisis Predictivo con IA
 * DAO: Singleton thread-safe para la conexión JDBC con pool HikariCP.
 *
 * Las credenciales se leen desde {@code config/db.properties}
 * a través de {@link AppConfig}, eliminando valores hardcodeados.
 *
 * Soporta MySQL, MariaDB y PostgreSQL según el driver configurado.
 */
public class ConexionDB {

    private static final Logger LOGGER = Logger.getLogger(ConexionDB.class.getName());

    // ── Singleton (double-checked locking, thread-safe) ───────────────────────
    private static volatile ConexionDB instancia;
    private HikariDataSource dataSource;

    private ConexionDB() {
        try {
            Class.forName(AppConfig.getDbDriver());
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Driver JDBC no encontrado: " + AppConfig.getDbDriver(), e);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(AppConfig.getDbUrl());
        config.setUsername(AppConfig.getDbUser());
        config.setPassword(AppConfig.getDbPassword());
        
        // Configuraciones recomendadas para un pool robusto
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000); // 30 segundos
        config.setIdleTimeout(600000);      // 10 minutos
        config.setMaxLifetime(1800000);     // 30 minutos

        this.dataSource = new HikariDataSource(config);
        LOGGER.info("Pool de conexiones (HikariCP) inicializado → " + AppConfig.getDbUrl());
    }

    public static ConexionDB getInstancia() {
        if (instancia == null) {
            synchronized (ConexionDB.class) {
                if (instancia == null) {
                    instancia = new ConexionDB();
                }
            }
        }
        return instancia;
    }

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Retorna una conexión JDBC desde el pool HikariCP.
     * HikariCP garantiza que la conexión devuelta sea válida.
     *
     * @throws SQLException si el driver no está en el classpath
     *                      o las credenciales son incorrectas.
     */
    public Connection getConexion() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Cierra el pool de conexiones de forma segura y limpia el Singleton
     * para permitir su recreación si es necesario.
     */
    public synchronized void cerrar() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.info("Pool de conexiones HikariCP cerrado correctamente.");
        }
        instancia = null;
    }
}
