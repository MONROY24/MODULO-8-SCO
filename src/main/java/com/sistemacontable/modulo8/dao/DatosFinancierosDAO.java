package com.sistemacontable.modulo8.dao;

import com.sistemacontable.modulo8.modelo.RegistroFinanciero;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MÓDULO 8 – Análisis Predictivo con IA
 * DAO: Extrae el historial financiero de la base de datos.
 *
 * REGLA MULTI-EMPRESA: Todos los métodos reciben id_empresa
 * para garantizar el aislamiento de datos entre entidades.
 *
 * INTEGRACIÓN FUTURA: Solo hay que modificar las constantes SQL_*
 * para apuntar a las tablas reales del proyecto unificado.
 */
public class DatosFinancierosDAO {

    // ── Consultas SQL (Mock – tablas provisionales) ───────────────────────────
    private static final String SQL_HISTORIAL_MOCK =
            "SELECT id_empresa, mes_numero, anio, total_compras, total_ventas " +
            "FROM historial_financiero_mock " +
            "WHERE id_empresa = ? " +
            "ORDER BY anio ASC, mes_numero ASC";

    private static final String SQL_EMPRESAS_MOCK =
            "SELECT id_empresa, nombre_comercial " +
            "FROM empresa_mock " +
            "ORDER BY nombre_comercial";

    private static final String SQL_NOMBRE_EMPRESA_MOCK =
            "SELECT nombre_comercial FROM empresa_mock WHERE id_empresa = ?";

    // ── Consultas SQL integración futura (descomenta cuando el proyecto se unifique)
    /*
    private static final String SQL_HISTORIAL_REAL =
        "SELECT " +
        "    MONTH(v.fecha_emision) AS mes_numero, " +
        "    YEAR(v.fecha_emision)  AS anio, " +
        "    COALESCE(SUM(c.total_factura), 0) AS total_compras, " +
        "    COALESCE(SUM(v.total_factura), 0) AS total_ventas " +
        "FROM ventas_facturas v " +
        "LEFT JOIN compras_facturas c " +
        "    ON MONTH(v.fecha_emision) = MONTH(c.fecha_emision) " +
        "   AND YEAR(v.fecha_emision)  = YEAR(c.fecha_emision) " +
        "   AND v.id_empresa           = c.id_empresa " +
        "WHERE v.id_empresa = ? " +
        "  AND YEAR(v.fecha_emision) = 2025 " +
        "GROUP BY MONTH(v.fecha_emision), YEAR(v.fecha_emision) " +
        "ORDER BY anio ASC, mes_numero ASC";
    */

    // ── Dependencia ───────────────────────────────────────────────────────────
    private final ConexionDB conexionDB;

    public DatosFinancierosDAO() {
        this.conexionDB = ConexionDB.getInstancia();
    }

    // ── Métodos públicos ──────────────────────────────────────────────────────

    /**
     * Retorna el historial financiero mensual de una empresa.
     *
     * @param idEmpresa Identificador de la empresa (parámetro obligatorio Multi-Empresa).
     * @return Lista ordenada cronológicamente de registros financieros.
     * @throws SQLException si hay error de acceso a la BD.
     */
    public List<RegistroFinanciero> obtenerHistorial(int idEmpresa) throws SQLException {
        List<RegistroFinanciero> lista = new ArrayList<>();
        Connection conn = conexionDB.getConexion();

        try (PreparedStatement ps = conn.prepareStatement(SQL_HISTORIAL_MOCK)) {
            ps.setInt(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RegistroFinanciero r = new RegistroFinanciero(
                            rs.getInt("id_empresa"),
                            rs.getInt("mes_numero"),
                            rs.getInt("anio"),
                            rs.getDouble("total_compras"),
                            rs.getDouble("total_ventas")
                    );
                    lista.add(r);
                }
            }
        }
        return lista;
    }

    /**
     * Retorna el listado de todas las empresas disponibles.
     * @return Mapa ID → Nombre para poblar el selector de empresa en la UI.
     */
    public List<String[]> obtenerEmpresas() throws SQLException {
        List<String[]> empresas = new ArrayList<>();
        Connection conn = conexionDB.getConexion();

        try (PreparedStatement ps = conn.prepareStatement(SQL_EMPRESAS_MOCK);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                empresas.add(new String[]{
                        String.valueOf(rs.getInt("id_empresa")),
                        rs.getString("nombre_comercial")
                });
            }
        }
        return empresas;
    }

    /**
     * Obtiene el nombre comercial de una empresa por su ID.
     * @param idEmpresa Identificador.
     * @return Nombre comercial o "Empresa desconocida" si no existe.
     */
    public String obtenerNombreEmpresa(int idEmpresa) throws SQLException {
        Connection conn = conexionDB.getConexion();
        try (PreparedStatement ps = conn.prepareStatement(SQL_NOMBRE_EMPRESA_MOCK)) {
            ps.setInt(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nombre_comercial");
                }
            }
        }
        return "Empresa desconocida";
    }
}
