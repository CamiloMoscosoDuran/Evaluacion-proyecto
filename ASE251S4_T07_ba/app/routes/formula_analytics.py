from flask import Blueprint, jsonify, request
from pymongo.errors import PyMongoError
import pyodbc

from app.repositories.mongodb.formula_repository import CollectionNotFoundError
from app.services.analytics.formula_service import (
    analyze_clients,
    analyze_formulas,
    analyze_low_stock,
    analyze_sql_cobranzas,
    analyze_sql_sales,
    get_formulas_kpi_total,
    get_formulas_kpi_activas,
    get_formulas_kpi_en_revision,
    get_formulas_distribucion_estado,
    get_formulas_evolucion_mensual,
    get_formulas_detalle,
    get_formulas_dashboard,
    get_top_selling_formulas_analysis,
)

analytics_bp = Blueprint("analytics", __name__, url_prefix="/api/analytics")


def _is_empty_result(result):
    if not isinstance(result, dict):
        return False
    for key in ("total_registros", "total_insumos", "total_ordenes", "total_registradas", "total_formulas"):
        if key in result:
            return result[key] == 0
    if "items" in result:
        return len(result["items"]) == 0
    return False


def _run_analysis(function, *args, **kwargs):
    try:
        result = function(*args, **kwargs)
        if _is_empty_result(result):
            result.setdefault("mensaje", "La consulta no devolvió resultados")
        return jsonify(result), 200
    except CollectionNotFoundError as error:
        return jsonify({"error": "coleccion_inexistente", "mensaje": str(error)}), 404
    except (PyMongoError, RuntimeError) as error:
        return jsonify({"error": "base_datos_no_disponible", "mensaje": str(error)}), 503
    except pyodbc.Error as error:
        return jsonify({"error": "sqlserver_no_disponible", "mensaje": f"Error de SQL Server: {error}"}), 503
    except Exception as error:
        return jsonify({"error": "error_interno", "mensaje": f"No se pudo completar el análisis: {error}"}), 500


@analytics_bp.get("/clientes")
def clientes():
    return _run_analysis(analyze_clients)


@analytics_bp.get("/formulas")
def formulas():
    return _run_analysis(analyze_formulas)


# ==============================================================================
# ENDPOINTS ANALÍTICOS DE FÓRMULAS (6 INDICADORES FIGMA)
# ==============================================================================

@analytics_bp.get("/formulas/kpi-total")
def formulas_kpi_total():
    """Indicador 1: Tarjeta KPI - Total de fórmulas registradas y nuevas en el periodo."""
    dias = request.args.get("dias", default=30, type=int)
    return _run_analysis(get_formulas_kpi_total, dias=dias)


@analytics_bp.get("/formulas/kpi-activas")
def formulas_kpi_activas():
    """Indicador 2: Tarjeta KPI - Total y porcentaje de fórmulas activas."""
    return _run_analysis(get_formulas_kpi_activas)


@analytics_bp.get("/formulas/kpi-en-revision")
def formulas_kpi_en_revision():
    """Indicador 3: Tarjeta KPI - Fórmulas en revisión / inactivas que requieren evaluación."""
    return _run_analysis(get_formulas_kpi_en_revision)


@analytics_bp.get("/formulas/distribucion-estado")
def formulas_distribucion_estado():
    """Indicador 4: Gráfico de dona - Distribución porcentual según el estado del registro."""
    return _run_analysis(get_formulas_distribucion_estado)


@analytics_bp.get("/formulas/evolucion-mensual")
def formulas_evolucion_mensual():
    """Indicador 5: Gráfico de barras - Fórmulas registradas por mes (últimos N meses)."""
    meses = request.args.get("meses", default=6, type=int)
    return _run_analysis(get_formulas_evolucion_mensual, meses=meses)


@analytics_bp.get("/formulas/detalle")
def formulas_detalle():
    """Indicador 6: Tabla interactiva - Detalle y auditoría de fórmulas paginada."""
    page = request.args.get("page", default=1, type=int)
    limit = request.args.get("limit", default=10, type=int)
    estado = request.args.get("estado", default="todas", type=str)
    search = request.args.get("search", default=None, type=str)
    return _run_analysis(get_formulas_detalle, page=page, limit=limit, estado=estado, search=search)


@analytics_bp.get("/formulas/dashboard")
def formulas_dashboard():
    """Consolidado de los 6 indicadores del dashboard de fórmulas."""
    dias = request.args.get("dias", default=30, type=int)
    meses = request.args.get("meses", default=6, type=int)
    limit_detalle = request.args.get("limit_detalle", default=4, type=int)
    return _run_analysis(get_formulas_dashboard, dias=dias, meses=meses, limit_detalle=limit_detalle)


# ==============================================================================
# INDICADOR TRANSACCIONAL HÍBRIDO: FÓRMULAS MÁS VENDIDAS (SQL SERVER + MONGODB)
# ==============================================================================

@analytics_bp.get("/formulas/mas-vendidas")
@analytics_bp.get("/formulas/top-selling")
def formulas_mas_vendidas():
    """
    Ranking transaccional: Fórmulas con mayor cantidad de unidades vendidas en un periodo del año.
    Cruza los registros de SQL Server (ventas.order_detail + ventas.sales_order) con MongoDB ('formula').
    """
    year = request.args.get("anio", default=None, type=int)
    if year is None:
        year = request.args.get("year", default=None, type=int)
    start_date = request.args.get("startDate", default=None, type=str)
    end_date = request.args.get("endDate", default=None, type=str)
    limit = request.args.get("limit", default=5, type=int)

    return _run_analysis(
        get_top_selling_formulas_analysis,
        year=year,
        start_date=start_date,
        end_date=end_date,
        limit=limit,
    )


# ==============================================================================
# OTROS ENDPOINTS TRANSACCIONALES
# ==============================================================================

@analytics_bp.get("/insumos-stock-bajo")
def insumos_stock_bajo():
    return _run_analysis(analyze_low_stock)


@analytics_bp.get("/ventas-formulas")
def ventas_formulas():
    return _run_analysis(analyze_sql_sales)


@analytics_bp.get("/cobranzas-mora")
def cobranzas_mora():
    return _run_analysis(analyze_sql_cobranzas)