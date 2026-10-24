from flask import Blueprint, jsonify, request
from pymongo.errors import PyMongoError

from app.repositories.mongodb.formula_repository import CollectionNotFoundError
from app.services.analytics.insumo_service import (
    get_insumos_kpi_total,
    get_insumos_kpi_stock_bajo,
    get_insumos_kpi_disponibles,
    get_insumos_estado_inventario,
    get_insumos_por_categoria,
    get_insumos_detalle_inventario,
    get_insumos_dashboard,
    get_recent_sales_orders_analysis,
)

insumo_analytics_bp = Blueprint("insumo_analytics", __name__, url_prefix="/api/analytics")


def _run(function, *args, **kwargs):
    try:
        result = function(*args, **kwargs)
        return jsonify(result), 200
    except CollectionNotFoundError as error:
        return jsonify({"error": "coleccion_inexistente", "mensaje": str(error)}), 404
    except PyMongoError as error:
        return jsonify({"error": "base_datos_no_disponible", "mensaje": str(error)}), 503
    except Exception as error:
        return jsonify({"error": "error_interno", "mensaje": f"No se pudo completar el analisis: {error}"}), 500


# ==============================================================================
# ENDPOINTS - DASHBOARD DE INSUMOS (6 INDICADORES FIGMA)
# ==============================================================================

@insumo_analytics_bp.get("/insumos/kpi-total")
def insumos_kpi_total():
    """Indicador 1: KPI Card - Total de insumos registrados y nuevos en el periodo."""
    dias = request.args.get("dias", default=30, type=int)
    return _run(get_insumos_kpi_total, dias=dias)


@insumo_analytics_bp.get("/insumos/kpi-stock-bajo")
def insumos_kpi_stock_bajo():
    """Indicador 2: KPI Card - Insumos con stock por debajo del minimo."""
    return _run(get_insumos_kpi_stock_bajo)


@insumo_analytics_bp.get("/insumos/kpi-disponibles")
def insumos_kpi_disponibles():
    """Indicador 3: KPI Card - Insumos disponibles (stock >= minimo)."""
    return _run(get_insumos_kpi_disponibles)


@insumo_analytics_bp.get("/insumos/estado-inventario")
def insumos_estado_inventario():
    """Indicador 4: Grafico de dona - Estado del inventario (disponibles vs stock bajo)."""
    return _run(get_insumos_estado_inventario)


@insumo_analytics_bp.get("/insumos/por-categoria")
def insumos_por_categoria():
    """Indicador 5: Grafico de barras - Cantidad de insumos agrupados por categoria."""
    return _run(get_insumos_por_categoria)


@insumo_analytics_bp.get("/insumos/detalle-inventario")
def insumos_detalle_inventario():
    """Indicador 6: Tabla interactiva - Detalle del inventario con estado de stock."""
    page = request.args.get("page", default=1, type=int)
    limit = request.args.get("limit", default=10, type=int)
    estado = request.args.get("estado", default="todos", type=str)
    search = request.args.get("search", default=None, type=str)
    return _run(get_insumos_detalle_inventario, page=page, limit=limit, estado=estado, search=search)


@insumo_analytics_bp.get("/insumos/ordenes-recientes")
@insumo_analytics_bp.get("/insumos/ordenes-venta-recientes")
def insumos_ordenes_recientes():
    """Indicador Transaccional: Órdenes de venta recientes (SQL Server + MongoDB)."""
    limit = request.args.get("limit", default=10, type=int)
    estado = request.args.get("estado", default=None, type=str)
    days = request.args.get("dias", default=None, type=int)
    return _run(get_recent_sales_orders_analysis, limit=limit, order_status=estado, days=days)


@insumo_analytics_bp.get("/insumos/dashboard")
def insumos_dashboard():
    """Consolidado de los indicadores del dashboard de Insumos en una sola llamada."""
    dias = request.args.get("dias", default=30, type=int)
    limit_detalle = request.args.get("limit_detalle", default=4, type=int)
    limit_ordenes = request.args.get("limit_ordenes", default=5, type=int)
    return _run(get_insumos_dashboard, dias=dias, limit_detalle=limit_detalle, limit_ordenes=limit_ordenes)

