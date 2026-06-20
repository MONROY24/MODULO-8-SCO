package com.sistemacontable.modulo8.dao;

import com.sistemacontable.modulo8.modelo.EmpresaItem;
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
        "    v.mes_numero, " +
        "    v.anio, " +
        "    COALESCE(c.total_compras, 0) AS total_compras, " +
        "    v.total_ventas " +
        "FROM (" +
        "    SELECT MONTH(fecha_emision) AS mes_numero, YEAR(fecha_emision) AS anio, SUM(total_factura) AS total_ventas " +
        "    FROM ventas_facturas " +
        "    WHERE id_empresa = ? AND YEAR(fecha_emision) = 2025 " +
        "    GROUP BY YEAR(fecha_emision), MONTH(fecha_emision) " +
        ") v " +
        "LEFT JOIN (" +
        "    SELECT MONTH(fecha_emision) AS mes_numero, YEAR(fecha_emision) AS anio, SUM(total_factura) AS total_compras " +
        "    FROM compras_facturas " +
        "    WHERE id_empresa = ? AND YEAR(fecha_emision) = 2025 " +
        "    GROUP BY YEAR(fecha_emision), MONTH(fecha_emision) " +
        ") c " +
        "    ON v.mes_numero = c.mes_numero AND v.anio = c.anio " +
        "ORDER BY v.anio ASC, v.mes_numero ASC";
    // Nota al integrar: Esta consulta requiere setear el parámetro idEmpresa dos veces (ps.setInt(1, id); ps.setInt(2, id)).
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

        try (Connection conn = conexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(SQL_HISTORIAL_MOCK)) {
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
    public List<EmpresaItem> obtenerEmpresas() throws SQLException {
        List<EmpresaItem> empresas = new ArrayList<>();

        try (Connection conn = conexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(SQL_EMPRESAS_MOCK);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                empresas.add(new EmpresaItem(
                        rs.getInt("id_empresa"),
                        rs.getString("nombre_comercial")
                ));
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
        try (Connection conn = conexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(SQL_NOMBRE_EMPRESA_MOCK)) {
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
