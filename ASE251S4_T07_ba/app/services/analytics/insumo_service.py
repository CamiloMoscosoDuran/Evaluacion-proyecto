"""
Servicio analítico de Insumos (Supply) - Dashboard Figma
=========================================================
Implementa los 6 indicadores definidos en el prototipo Figma:
  1. KPI  - Insumos registrados (total + nuevos en periodo)
  2. KPI  - Insumos con stock bajo (requieren reposición)
  3. KPI  - Insumos disponibles (stock >= mínimo)
  4. Dona - Estado del inventario (disponibles vs stock bajo)
  5. Barras - Insumos por categoría
  6. Tabla  - Detalle de inventario (stock actual vs mínimo + estado)
"""

import re
from datetime import datetime, timedelta
from numbers import Number

from app.repositories.mongodb.insumo_repository import (
    get_insumos,
    get_supplies_low_stock,
)
from app.repositories.mongodb.client_repository import get_clients
import app.repositories.sqlserver.insumo_repository as sql_insumo_repo


# ---------------------------------------------------------------------------
# Helpers internos
# ---------------------------------------------------------------------------

def _get_path(document, path):
    """Navega un documento anidado usando notación de punto."""
    value = document
    for part in path.split("."):
        if not isinstance(value, dict):
            return None
        value = value.get(part)
    return value


def _number(value):
    try:
        return float(value)
    except (TypeError, ValueError):
        return None


def _parse_iso_date(value):
    if not value:
        return None
    if isinstance(value, datetime):
        return value
    try:
        return datetime.fromisoformat(str(value).replace("Z", ""))
    except Exception:
        return None


def _get_initials(name):
    words = re.findall(r"[A-Za-z0-9]+", name or "")
    if len(words) >= 2:
        return (words[0][0] + words[1][0]).upper()
    if words:
        return words[0][:2].upper()
    return "IN"


def _stock_status(current, minimum):
    """Determina el estado de stock de un insumo."""
    if current is None or minimum is None:
        return "Sin datos"
    if current < minimum:
        return "Stock bajo"
    return "Disponible"


def _clean_quantity(value):
    """
    Extrae el valor numérico y la unidad de un campo de cantidad.
    Soporta formatos: 12, "12 kg", "8 L", 8.5
    """
    if value is None:
        return None, ""
    if isinstance(value, (int, float)):
        return float(value), ""
    text = str(value).strip()
    match = re.match(r"^([\d.]+)\s*([a-zA-Z]*)$", text)
    if match:
        return float(match.group(1)), match.group(2)
    num = _number(text)
    return num, ""


def _format_quantity(value):
    """Retorna cantidad como string legible, ej: '12 kg' o '8 L'."""
    num, unit = _clean_quantity(value)
    if num is None:
        return "N/D"
    num_fmt = int(num) if num == int(num) else round(num, 2)
    return f"{num_fmt} {unit}".strip() if unit else str(num_fmt)


def _infer_category(doc):
    """Infiere la categoría del insumo desde los campos del documento."""
    for field in ("category", "categoria", "type", "tipo", "group", "grupo"):
        val = doc.get(field)
        if val and isinstance(val, str):
            return val.strip().capitalize()

    text = f"{doc.get('name', '')} {doc.get('description', '')}".lower()
    if any(w in text for w in ("organico", "organico", "humus", "melaza", "alga")):
        return "Organico"
    if any(w in text for w in ("mineral", "calcio", "boro", "zinc", "magnesio", "hierro", "azufre")):
        return "Mineral"
    if any(w in text for w in ("liquid", "solucion", "solucion")):
        return "Liquido"
    if any(w in text for w in ("base", "corrector", "buffer")):
        return "Base"
    return "Otros"


# ---------------------------------------------------------------------------
# Indicador 1: KPI - Insumos registrados
# ---------------------------------------------------------------------------

def get_insumos_kpi_total(dias=30):
    """
    Indicador 1 (KPI Card): Total de insumos registrados y nuevos en el periodo.
    Corresponde al card 'Insumos registrados' del Figma.
    """
    docs = get_insumos()
    total = len(docs)

    if total == 0:
        return {
            "indicador": "Insumos registrados",
            "total_insumos": 0,
            "nuevos_en_periodo": 0,
            "periodo_dias": dias,
            "comparativa_texto": "+ 0 nuevos en el periodo",
        }

    dates = [_parse_iso_date(d.get("created_at")) for d in docs]
    valid_dates = [d for d in dates if d is not None]
    ref_date = max(valid_dates) if valid_dates else datetime.now()
    cutoff = ref_date - timedelta(days=dias)
    nuevos = sum(1 for d in valid_dates if d >= cutoff)

    return {
        "indicador": "Insumos registrados",
        "total_insumos": total,
        "nuevos_en_periodo": nuevos,
        "periodo_dias": dias,
        "comparativa_texto": f"+ {nuevos} nuevos en el periodo",
    }


# ---------------------------------------------------------------------------
# Indicador 2: KPI - Insumos con stock bajo
# ---------------------------------------------------------------------------

def get_insumos_kpi_stock_bajo():
    """
    Indicador 2 (KPI Card): Insumos cuyo stock actual esta por debajo del minimo.
    Corresponde al card 'Insumos con stock bajo' del Figma.
    """
    todos = get_insumos()
    total = len(todos)
    bajo = get_supplies_low_stock()
    total_bajo = len(bajo)
    pct = round((total_bajo / total) * 100, 1) if total else 0.0

    return {
        "indicador": "Insumos con stock bajo",
        "total_stock_bajo": total_bajo,
        "total_insumos": total,
        "porcentaje_stock_bajo": pct,
        "porcentaje_texto": f"{pct}% del inventario",
        "alerta": "Requieren reposicion",
    }


# ---------------------------------------------------------------------------
# Indicador 3: KPI - Insumos disponibles
# ---------------------------------------------------------------------------

def get_insumos_kpi_disponibles():
    """
    Indicador 3 (KPI Card): Insumos con stock >= minimo (disponibles).
    Corresponde al card 'Insumos disponibles' del Figma.
    """
    todos = get_insumos()
    total = len(todos)
    bajo = get_supplies_low_stock()
    total_bajo = len(bajo)
    disponibles = total - total_bajo
    pct = round((disponibles / total) * 100, 1) if total else 0.0

    return {
        "indicador": "Insumos disponibles",
        "total_disponibles": disponibles,
        "total_insumos": total,
        "porcentaje_disponibles": pct,
        "porcentaje_texto": f"{pct}% del inventario",
    }


# ---------------------------------------------------------------------------
# Indicador 4: Grafico circular (Dona) - Estado del inventario
# ---------------------------------------------------------------------------

def get_insumos_estado_inventario():
    """
    Indicador 4 (Grafico de Dona): Distribucion visual del estado del inventario.
    Muestra la proporcion de insumos disponibles vs con stock bajo.
    Corresponde al grafico 'Estado del inventario' del Figma.
    """
    todos = get_insumos()
    total = len(todos)
    bajo = get_supplies_low_stock()
    total_bajo = len(bajo)
    disponibles = total - total_bajo

    pct_disp = round((disponibles / total) * 100, 1) if total else 0.0
    pct_bajo = round((total_bajo / total) * 100, 1) if total else 0.0

    return {
        "indicador": "Estado del inventario",
        "descripcion": "Disponibilidad segun el stock minimo",
        "total_insumos": total,
        "disponibles": {
            "cantidad": disponibles,
            "porcentaje": pct_disp,
            "etiqueta": f"{disponibles} disponibles",
            "porcentaje_texto": f"{pct_disp}% del total",
        },
        "stock_bajo": {
            "cantidad": total_bajo,
            "porcentaje": pct_bajo,
            "etiqueta": f"{total_bajo} con stock bajo",
            "porcentaje_texto": f"{pct_bajo}% del total",
        },
        "serie_grafico": [
            {"nombre": "Disponibles", "valor": disponibles, "porcentaje": pct_disp, "color": "#2D6A4F"},
            {"nombre": "Stock bajo", "valor": total_bajo, "porcentaje": pct_bajo, "color": "#D4A017"},
        ],
    }


# ---------------------------------------------------------------------------
# Indicador 5: Grafico de barras - Insumos por categoria
# ---------------------------------------------------------------------------

def get_insumos_por_categoria():
    """
    Indicador 5 (Grafico de Barras): Cantidad de insumos agrupados por categoria.
    Corresponde al grafico 'Insumos por categoria' del Figma.
    """
    docs = get_insumos()
    total = len(docs)

    if total == 0:
        return {
            "indicador": "Insumos por categoria",
            "total_insumos": 0,
            "categorias": [],
            "serie_grafico": [],
        }

    conteo = {}
    for doc in docs:
        cat = _infer_category(doc)
        conteo[cat] = conteo.get(cat, 0) + 1

    categorias_ordenadas = sorted(conteo.items(), key=lambda x: x[1], reverse=True)

    serie = [
        {
            "categoria": cat,
            "cantidad": cnt,
            "porcentaje": round((cnt / total) * 100, 1),
        }
        for cat, cnt in categorias_ordenadas
    ]

    return {
        "indicador": "Insumos por categoria",
        "descripcion": "Cantidad de registros por grupo",
        "total_insumos": total,
        "total_categorias": len(serie),
        "categorias": [s["categoria"] for s in serie],
        "serie_grafico": serie,
    }


# ---------------------------------------------------------------------------
# Indicador 6: Tabla - Detalle de inventario
# ---------------------------------------------------------------------------

def get_insumos_detalle_inventario(page=1, limit=10, estado="todos", search=None):
    """
    Indicador 6 (Tabla): Detalle paginado del inventario comparando stock actual
    vs stock minimo para identificar reposiciones prioritarias.
    Corresponde a la tabla 'Detalle de inventario' del Figma.

    Columnas: Insumo | Codigo | Categoria | Stock actual | Stock minimo | Estado
    """
    docs = get_insumos()

    filas = []
    for doc in docs:
        nombre = doc.get("name") or doc.get("nombre") or "Sin nombre"
        codigo = doc.get("_id") or doc.get("code") or doc.get("codigo") or "N/D"
        categoria = _infer_category(doc)
        subtipo = doc.get("sub_type") or doc.get("subtipo") or doc.get("description") or ""

        current_raw = (
            _get_path(doc, "inventory.current_stock")
            or doc.get("stock_actual")
            or doc.get("stock")
            or doc.get("cantidad")
        )
        minimum_raw = (
            _get_path(doc, "inventory.minimum_stock")
            or doc.get("stock_minimo")
            or doc.get("stock_min")
            or doc.get("minimo")
        )

        current_num, unit = _clean_quantity(current_raw)
        minimum_num, min_unit = _clean_quantity(minimum_raw)
        unit = unit or min_unit

        status = _stock_status(current_num, minimum_num)

        filas.append({
            "codigo": str(codigo),
            "nombre": nombre,
            "iniciales": _get_initials(nombre),
            "subtipo": str(subtipo),
            "categoria": categoria,
            "stock_actual": _format_quantity(current_raw),
            "stock_minimo": _format_quantity(minimum_raw),
            "stock_actual_num": current_num,
            "stock_minimo_num": minimum_num,
            "unidad": unit,
            "estado": status,
        })

    # Filtro por estado
    estado_lower = estado.lower()
    if estado_lower == "stock_bajo":
        filas = [f for f in filas if f["estado"] == "Stock bajo"]
    elif estado_lower == "disponible":
        filas = [f for f in filas if f["estado"] == "Disponible"]

    # Filtro por busqueda
    if search:
        search_lower = search.lower()
        filas = [
            f for f in filas
            if search_lower in f["nombre"].lower() or search_lower in f["codigo"].lower()
        ]

    # Stock bajo al tope de la tabla
    filas.sort(key=lambda f: (0 if f["estado"] == "Stock bajo" else 1, f["nombre"]))

    total_filtrado = len(filas)
    total_paginas = max(1, -(-total_filtrado // limit))
    start = (page - 1) * limit
    pagina = filas[start: start + limit]

    todos_docs = get_insumos()
    total_global = len(todos_docs)

    return {
        "indicador": "Detalle de inventario",
        "descripcion": "Compara el stock actual con el minimo para identificar reposiciones prioritarias",
        "total_insumos": total_global,
        "total_filtrado": total_filtrado,
        "pagina_actual": page,
        "total_paginas": total_paginas,
        "limit": limit,
        "filtro_estado": estado,
        "filtro_busqueda": search,
        "columnas": ["insumo", "codigo", "categoria", "stock_actual", "stock_minimo", "estado"],
        "items": pagina,
        "nota_pie": f"Mostrando {len(pagina)} de {total_filtrado} insumos",
    }


# ---------------------------------------------------------------------------
# Indicador Transaccional: Órdenes de venta recientes (SQL Server + MongoDB)
# ---------------------------------------------------------------------------

def get_recent_sales_orders_analysis(limit=10, order_status=None, days=None):
    """
    Indicador Transaccional del Dashboard Figma: 'Órdenes de venta recientes'.
    Obtiene las transacciones desde la vista SQL Server (ventas.vw_resumen_ordenes)
    y enriquece los datos del cliente cruzando con la colección 'customer' en MongoDB.
    """
    status_map = {
        "C": {"label": "Completado", "badge": "success", "color": "#10B981"},
        "P": {"label": "Pendiente", "badge": "warning", "color": "#F59E0B"},
        "A": {"label": "Anulado", "badge": "danger", "color": "#EF4444"},
    }

    # 1. Obtener órdenes de SQL Server
    raw_orders = sql_insumo_repo.get_recent_sales_orders(
        limit=limit,
        order_status=order_status,
        days=days
    )

    # 2. Obtener clientes de MongoDB para el cruce híbrido
    try:
        clients = get_clients(limit=1000)
        client_map = {c["_id"]: c for c in clients if isinstance(c, dict) and "_id" in c}
    except Exception:
        client_map = {}

    items = []
    conteo_estados = {"Completado": 0, "Pendiente": 0, "Anulado": 0}
    monto_total = 0.0

    for row in raw_orders:
        oid = row.get("order_id")
        codigo_orden = f"ORD-{int(oid):04d}" if oid is not None else "ORD-0000"
        guia_remision = row.get("referral_guide") or "-"
        cid = row.get("mongo_customer_id")
        cust = client_map.get(cid, {})

        first = cust.get("first_name", "")
        last = cust.get("last_name", "")
        nombre_cliente = f"{first} {last}".strip() or cid or "Cliente no identificado"

        plots = cust.get("farm_plot") or []
        fundo = plots[0].get("farm_name") if plots and isinstance(plots[0], dict) else None
        cultivo = plots[0].get("type_crop") if plots and isinstance(plots[0], dict) else None

        addr = cust.get("address") or {}
        departamento = addr.get("department") if isinstance(addr, dict) else None

        st_code = (row.get("order_status") or "").upper()
        st_info = status_map.get(st_code, {"label": "Desconocido", "badge": "neutral", "color": "#6B7280"})
        estado_label = st_info["label"]

        if estado_label in conteo_estados:
            conteo_estados[estado_label] += 1

        total_ord = float(row.get("total_order") or 0)
        monto_total += total_ord
        cant_items = int(row.get("total_items") or 0)

        dt = _parse_iso_date(row.get("entry_date"))
        fecha_formateada = dt.strftime("%d/%m/%Y") if dt else str(row.get("entry_date") or "")[:10]

        items.append({
            "orden": codigo_orden,
            "order_id": oid,
            "guia_remision": guia_remision,
            "cliente": nombre_cliente,
            "fundo": fundo,
            "cultivo": cultivo,
            "departamento": departamento,
            "cliente_id": cid,
            "fecha": fecha_formateada,
            "fecha_iso": dt.isoformat() if dt else str(row.get("entry_date")),
            "importe": round(total_ord, 2),
            "importe_etiqueta": f"S/ {total_ord:,.2f}",
            "total_items": cant_items,
            "estado": estado_label,
            "estado_codigo": st_code,
            "badge": st_info["badge"],
            "color": st_info["color"],
        })

    return {
        "indicador": "Órdenes de venta recientes",
        "descripcion": "Transacciones de órdenes de venta con detalle de cliente y estado operativo (SQL Server + MongoDB)",
        "fuente_datos": {
            "transaccional": "SQL Server (ventas.vw_resumen_ordenes)",
            "catalogo": "MongoDB (colección 'customer')",
        },
        "parametros": {
            "limit": limit,
            "order_status": order_status or "todos",
            "days": days,
        },
        "total_ordenes_mostradas": len(items),
        "monto_total_acumulado": round(monto_total, 2),
        "monto_total_etiqueta": f"S/ {monto_total:,.2f}",
        "resumen_estados": conteo_estados,
        "items": items,
    }


# ---------------------------------------------------------------------------
# Dashboard consolidado
# ---------------------------------------------------------------------------

def get_insumos_dashboard(dias=30, limit_detalle=4, limit_ordenes=5):
    """
    Consolidado de los indicadores del dashboard de Insumos.
    Permite obtener toda la informacion en una sola llamada HTTP.
    """
    return {
        "seccion": "Insumos",
        "generado_en": datetime.now().isoformat(),
        "kpi_total": get_insumos_kpi_total(dias=dias),
        "kpi_stock_bajo": get_insumos_kpi_stock_bajo(),
        "kpi_disponibles": get_insumos_kpi_disponibles(),
        "estado_inventario": get_insumos_estado_inventario(),
        "por_categoria": get_insumos_por_categoria(),
        "detalle_inventario": get_insumos_detalle_inventario(
            page=1, limit=limit_detalle, estado="todos"
        ),
        "ordenes_recientes": get_recent_sales_orders_analysis(limit=limit_ordenes),
    }

