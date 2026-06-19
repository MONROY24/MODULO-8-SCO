package com.sistemacontable.modulo8.dao;

import com.sistemacontable.modulo8.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * MÓDULO 8 – Análisis Predictivo con IA
 * DAO: Singleton thread-safe para la conexión JDBC.
 *
 * Las credenciales se leen desde {@code config/db.properties}
 * a través de {@link AppConfig}, eliminando valores hardcodeados.
 *
 * Soporta MySQL, MariaDB y PostgreSQL según el driver configurado.
 */
public class ConexionDB {

    // ── Singleton (double-checked locking, thread-safe) ───────────────────────
    private static volatile ConexionDB instancia;
    private Connection conexion;

    private ConexionDB() {}

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
     * Retorna (y abre si es necesario) la conexión JDBC.
     * Lee la configuración desde {@code config/db.properties}.
     *
     * @throws SQLException si el driver no está en el classpath
     *                      o las credenciales son incorrectas.
     */
    public synchronized Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            try {
                Class.forName(AppConfig.getDbDriver());
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                        "Driver JDBC no encontrado: " + AppConfig.getDbDriver() +
                        "\nVerifica que el JAR esté en la carpeta /lib.", e);
            }
            conexion = DriverManager.getConnection(
                    AppConfig.getDbUrl(),
                    AppConfig.getDbUser(),
                    AppConfig.getDbPassword());

            System.out.println("[ConexionDB] Conexión establecida → " + AppConfig.getDbUrl());
        }
        return conexion;
    }

    /**
     * Cierra la conexión de forma segura y limpia el Singleton
     * para permitir reconexión.
     */
    public synchronized void cerrar() {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                    System.out.println("[ConexionDB] Conexión cerrada correctamente.");
                }
            } catch (SQLException e) {
                System.err.println("[ConexionDB] Error al cerrar conexión: " + e.getMessage());
            } finally {
                conexion  = null;
                instancia = null; // Permite reconectar con nueva config si es necesario
            }
        }
    }
}
