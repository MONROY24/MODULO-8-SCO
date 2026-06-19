-- ============================================================
-- MÓDULO 8: Análisis Predictivo con IA
-- Script SQL de configuración - Base de datos provisional (Mock)
-- Compatible con MySQL y PostgreSQL
-- ============================================================

-- ---- Para MySQL ----
-- 1. Crear tabla maestra de Empresas
CREATE TABLE IF NOT EXISTS empresa_mock (
    id_empresa   INT          PRIMARY KEY,
    nombre_comercial VARCHAR(100) NOT NULL
);

-- 2. Crear tabla de Historial Financiero Consolidado
CREATE TABLE IF NOT EXISTS historial_financiero_mock (
    id_registro  INT          PRIMARY KEY AUTO_INCREMENT,  -- Cambiar a SERIAL en PostgreSQL
    id_empresa   INT          NOT NULL,
    mes_numero   INT          NOT NULL,   -- 1=Enero ... 12=Diciembre
    anio         INT          NOT NULL,
    total_compras  DECIMAL(12,2) NOT NULL,
    total_ventas   DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (id_empresa) REFERENCES empresa_mock(id_empresa)
);

-- 3. Datos de prueba: Empresa A (crecimiento estable)
INSERT INTO empresa_mock (id_empresa, nombre_comercial) VALUES
    (1, 'Tech Solutions S.A. (Empresa A)');

INSERT INTO historial_financiero_mock
    (id_empresa, mes_numero, anio, total_compras, total_ventas) VALUES
    (1,  1, 2025,  5000.00,  8500.00),
    (1,  2, 2025,  5200.00,  9100.00),
    (1,  3, 2025,  5100.00,  8900.00),
    (1,  4, 2025,  5800.00, 10200.00),
    (1,  5, 2025,  6000.00, 11000.00),
    (1,  6, 2025,  5900.00, 10800.00),
    (1,  7, 2025,  6300.00, 11500.00),
    (1,  8, 2025,  6500.00, 12000.00),
    (1,  9, 2025,  6200.00, 11800.00),
    (1, 10, 2025,  6800.00, 12500.00),
    (1, 11, 2025,  7000.00, 13200.00),
    (1, 12, 2025,  7200.00, 13800.00);

-- 4. Datos de prueba: Empresa B (comportamiento errático)
INSERT INTO empresa_mock (id_empresa, nombre_comercial) VALUES
    (2, 'Comercial El Foco (Empresa B)');

INSERT INTO historial_financiero_mock
    (id_empresa, mes_numero, anio, total_compras, total_ventas) VALUES
    (2,  1, 2025,  2000.00,  2500.00),
    (2,  2, 2025,  4000.00,  3000.00),
    (2,  3, 2025,  1500.00,  4200.00),
    (2,  4, 2025,  5000.00,  2100.00),
    (2,  5, 2025,  3200.00,  3800.00),
    (2,  6, 2025,  4800.00,  2700.00),
    (2,  7, 2025,  1800.00,  4500.00),
    (2,  8, 2025,  5500.00,  2000.00),
    (2,  9, 2025,  2200.00,  4000.00),
    (2, 10, 2025,  4100.00,  2900.00),
    (2, 11, 2025,  3000.00,  3500.00),
    (2, 12, 2025,  4500.00,  2600.00);

-- ============================================================
-- CONSULTA DE INTEGRACIÓN FUTURA (reemplaza la mock)
-- Descomenta esto cuando el proyecto esté unificado:
-- ============================================================
/*
SELECT
    MONTH(v.fecha_emision) AS mes_numero,
    YEAR(v.fecha_emision)  AS anio,
    SUM(c.total_factura)   AS total_compras,
    SUM(v.total_factura)   AS total_ventas
FROM ventas_facturas v
JOIN compras_facturas c
    ON MONTH(v.fecha_emision) = MONTH(c.fecha_emision)
   AND YEAR(v.fecha_emision)  = YEAR(c.fecha_emision)
   AND v.id_empresa = c.id_empresa
WHERE v.id_empresa = ?
  AND YEAR(v.fecha_emision) = 2025
GROUP BY MONTH(v.fecha_emision), YEAR(v.fecha_emision)
ORDER BY mes_numero;
*/
