import re
from collections import Counter
from numbers import Number

from app.repositories.mongodb.client_repository import get_clients
from app.repositories.mongodb.formula_repository import find_all
from app.repositories.mongodb.insumo_repository import get_insumos
import app.repositories.sqlserver.formula_repository as sql_formula_repo


def _field_names(documents):
    return sorted({key for document in documents for key in document})


def _field_paths(documents):
    paths = set()

    def collect(value, prefix):
        if isinstance(value, dict):
            for key, nested_value in value.items():
                path = f"{prefix}.{key}" if prefix else key
                paths.add(path)
                collect(nested_value, path)

    for document in documents:
        collect(document, "")
    return sorted(paths)


def _get_path(document, path):
    value = document
    for part in path.split("."):
        if not isinstance(value, dict):
            return None
        value = value.get(part)
    return value


def _numeric_summary(documents):
    summary = {}
    for field in _field_paths(documents):
        values = [
            _get_path(document, field)
            for document in documents
            if isinstance(_get_path(document, field), Number)
            and not isinstance(_get_path(document, field), bool)
        ]
        if values:
            summary[field] = {
                "min": min(values),
                "max": max(values),
                "promedio": round(sum(values) / len(values), 2),
            }
    return summary


def _value_counts(documents, field):
    values = [document.get(field) for document in documents if document.get(field) is not None]
    if not values or any(isinstance(value, (dict, list)) for value in values):
        return None
    return dict(Counter(map(str, values)).most_common(10))


def _general_analysis(documents):
    analysis = {
        "total_registros": len(documents),
        "campos_detectados": _field_names(documents),
        "resumen_numerico": _numeric_summary(documents),
    }
    categorical = {}
    for field in analysis["campos_detectados"]:
        counts = _value_counts(documents, field)
        if counts:
            categorical[field] = counts
    if categorical:
        analysis["distribuciones_categoricas"] = categorical
    return analysis


def analyze_clients():
    """Analyze customer documents from MongoDB."""
    return _general_analysis(get_clients())


def analyze_formulas():
    """Analyze formulas from MongoDB."""
    documents = find_all("formula")
    analysis = _general_analysis(documents)
    costs = [
        value
        for value in (_number(document.get("preparation_cost")) for document in documents)
        if value is not None
    ]
    prices = [
        value
        for value in (_number(document.get("suggested_price")) for document in documents)
        if value is not None
    ]
    if costs or prices:
        indicators = {}
        if costs:
            indicators["costo_preparacion_promedio"] = round(sum(costs) / len(costs), 2)
        if prices:
            indicators["precio_sugerido_promedio"] = round(sum(prices) / len(prices), 2)
        if costs and prices and len(costs) == len(prices):
            indicators["margen_promedio_estimado"] = round(
                sum(price - cost for cost, price in zip(costs, prices)) / len(costs),
                2,
            )
        analysis["indicadores_formula"] = indicators
    return analysis


def _normalized(value):
    return re.sub(r"[^a-z0-9]", "", str(value).lower())


def _number(value):
    try:
        return float(value)
    except (TypeError, ValueError):
        return None


def _find_field(field_names, candidates):
    normalized = {_normalized(field): field for field in field_names}
    for candidate in candidates:
        if _normalized(candidate) in normalized:
            return normalized[_normalized(candidate)]
    return None


def analyze_low_stock():
    """Analyze supplies with current stock below minimum stock."""
    documents = get_insumos()
    fields = _field_paths(documents)
    stock_field = _find_field(
        fields,
        (
            "stock",
            "stock_actual",
            "existencias",
            "cantidad",
            "inventory.current_stock",
        ),
    )
    minimum_field = _find_field(
        fields,
        (
            "stock_minimo",
            "stock_min",
            "minimo",
            "min_stock",
            "cantidad_minima",
            "inventory.minimum_stock",
        ),
    )

    if not stock_field or not minimum_field:
        return {
            "total_insumos": len(documents),
            "insumos_stock_bajo": [],
            "campos_detectados": fields,
            "criterio": None,
            "mensaje": "No se encontraron campos de stock actual y stock mínimo en los documentos",
        }

    low_stock = []
    for document in documents:
        stock = _get_path(document, stock_field)
        minimum = _get_path(document, minimum_field)
        if isinstance(stock, Number) and isinstance(minimum, Number) and stock < minimum:
            low_stock.append(document)

    return {
        "total_insumos": len(documents),
        "insumos_stock_bajo": low_stock,
        "campos_utilizados": {
            "stock_actual": stock_field,
            "stock_minimo": minimum_field,
        },
        "criterio": f"{stock_field} < {minimum_field}",
        "total_stock_bajo": len(low_stock),
    }


def analyze_sql_sales():
    """Analyze sales and formula performance from SQL Server."""
    formula_sales = sql_formula_repo.get_formula_sales_summary()
    orders = sql_formula_repo.get_sales_orders_summary()
    total_sales_amount = sum(float(order.get("total_order", 0)) for order in orders)
    return {
        "total_ordenes": len(orders),
        "monto_total_ventas": round(total_sales_amount, 2),
        "ventas_por_formula": formula_sales,
        "resumen_ordenes": orders,
    }


def analyze_sql_cobranzas():
    """Analyze overdue installments and accounts receivable from SQL Server."""
    overdue = sql_formula_repo.get_overdue_portfolio()
    accounts = sql_formula_repo.get_customer_account_status()
    total_overdue = sum(float(row.get("balance_pending", 0)) for row in overdue)
    return {
        "total_cuotas_vencidas": len(overdue),
        "deuda_vencida_total": round(total_overdue, 2),
        "cartera_vencida": overdue,
        "estado_cuentas_clientes": accounts,
    }


# ==============================================================================
# INDICADORES DEL DASHBOARD DE FÓRMULAS (FIGMA - MAESTRO FÓRMULAS)
# ==============================================================================

import math
from datetime import datetime, timezone, timedelta

_MESES_ABREV = ["", "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"]
_MESES_COMPLETO = ["", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"]


def _parse_iso_date(value):
    if not value:
        return None
    if isinstance(value, datetime):
        return value
    try:
        clean = str(value).replace("Z", "")
        return datetime.fromisoformat(clean)
    except Exception:
        return None


def _format_date_dmy(value):
    dt = _parse_iso_date(value)
    if not dt:
        return "N/D"
    return dt.strftime("%d/%m/%Y")


def _infer_formula_type(doc):
    """Infer formula functional type for dashboard presentation."""
    if doc.get("type"):
        return str(doc["type"]).strip().capitalize()
    text = f"{doc.get('name', '')} {doc.get('description', '')}".lower()
    if "foliar" in text:
        return "Foliar"
    if "enraiz" in text or "root" in text:
        return "Enraizador"
    if "nutri" in text or "balance" in text:
        return "Nutricional"
    if "fructif" in text or "potasio" in text:
        return "Fructificación"
    if "bio" in text or "humico" in text:
        return "Biofertilizante"
    if "protector" in text or "shield" in text or "silicio" in text:
        return "Protector"
    if "cobre" in text or "fung" in text:
        return "Preventivo"
    if "flor" in text:
        return "Floración"
    return "Especialidad"


def _get_initials(name):
    words = re.findall(r"[A-Za-z0-9]+", name or "")
    if len(words) >= 2:
        return (words[0][0] + words[1][0]).upper()
    elif words:
        return words[0][:2].upper()
    return "FO"


def get_formulas_kpi_total(dias=30):
    """
    Indicador 1: Tarjeta KPI - Total de fórmulas registradas.
    Calcula el total general de fórmulas y las registradas en el periodo.
    """
    docs = find_all("formula")
    total = len(docs)
    if total == 0:
        return {
            "indicador": "Fórmulas registradas",
            "total_registradas": 0,
            "nuevas_en_periodo": 0,
            "periodo_dias": dias,
            "comparativa_texto": "+ 0 nuevas en el periodo",
        }

    # Fecha de referencia: fecha más reciente registrada o actual
    dates = [_parse_iso_date(d.get("created_at")) for d in docs]
    valid_dates = [d for d in dates if d is not None]
    ref_date = max(valid_dates) if valid_dates else datetime.now()
    cutoff_date = ref_date - timedelta(days=dias)

    nuevas = sum(1 for d in valid_dates if d >= cutoff_date)

    return {
        "indicador": "Fórmulas registradas",
        "total_registradas": total,
        "nuevas_en_periodo": nuevas,
        "periodo_dias": dias,
        "comparativa_texto": f"+ {nuevas} nuevas en el periodo",
    }


def get_formulas_kpi_activas():
    """
    Indicador 2: Tarjeta KPI - Fórmulas activas.
    Calcula la cantidad y el porcentaje de fórmulas activas del total.
    """
    docs = find_all("formula")
    total = len(docs)
    if total == 0:
        return {
            "indicador": "Fórmulas activas",
            "total_activas": 0,
            "total_registradas": 0,
            "porcentaje_activas": 0.0,
            "porcentaje_texto": "0.0% del total",
        }

    activas = sum(1 for d in docs if bool(d.get("status", True)))
    pct = round((activas / total) * 100, 1)

    return {
        "indicador": "Fórmulas activas",
        "total_activas": activas,
        "total_registradas": total,
        "porcentaje_activas": pct,
        "porcentaje_texto": f"{pct}% del total",
    }


def get_formulas_kpi_en_revision():
    """
    Indicador 3: Tarjeta KPI - Fórmulas en revisión / inactivas.
    Calcula cuántas fórmulas requieren evaluación o validación técnica.
    """
    docs = find_all("formula")
    total = len(docs)
    if total == 0:
        return {
            "indicador": "En revisión",
            "total_en_revision": 0,
            "total_registradas": 0,
            "porcentaje_en_revision": 0.0,
            "mensaje": "Requieren evaluación",
        }

    revision = sum(1 for d in docs if not bool(d.get("status", True)))
    pct = round((revision / total) * 100, 1)

    return {
        "indicador": "En revisión",
        "total_en_revision": revision,
        "total_registradas": total,
        "porcentaje_en_revision": pct,
        "mensaje": "Requieren evaluación",
    }


def get_formulas_distribucion_estado():
    """
    Indicador 4: Gráfico de dona (Donut Chart) - Distribución según estado del registro.
    Retorna el desglose de activas vs en revisión con cantidades y porcentajes.
    """
    docs = find_all("formula")
    total = len(docs)
    if total == 0:
        return {
            "indicador": "Estado de fórmulas",
            "total_formulas": 0,
            "activas": {"cantidad": 0, "porcentaje": 0.0, "etiqueta": "0 activas (0.0% del total)"},
            "en_revision": {"cantidad": 0, "porcentaje": 0.0, "etiqueta": "0 en revisión (0.0% del total)"},
            "serie_grafico": [],
        }

    activas = sum(1 for d in docs if bool(d.get("status", True)))
    revision = total - activas
    pct_activas = round((activas / total) * 100, 1)
    pct_revision = round((revision / total) * 100, 1)

    return {
        "indicador": "Estado de fórmulas",
        "descripcion": "Distribución según el estado del registro",
        "total_formulas": total,
        "activas": {
            "cantidad": activas,
            "porcentaje": pct_activas,
            "etiqueta": f"{activas} activas",
            "porcentaje_texto": f"{pct_activas}% del total",
        },
        "en_revision": {
            "cantidad": revision,
            "porcentaje": pct_revision,
            "etiqueta": f"{revision} en revisión",
            "porcentaje_texto": f"{pct_revision}% del total",
        },
        "serie_grafico": [
            {
                "estado": "Activas",
                "cantidad": activas,
                "porcentaje": pct_activas,
                "color": "#1b4d3e",
            },
            {
                "estado": "En revisión",
                "cantidad": revision,
                "porcentaje": pct_revision,
                "color": "#d9822b",
            },
        ],
    }


def get_formulas_evolucion_mensual(meses=6):
    """
    Indicador 5: Gráfico de barras (Bar Chart) - Fórmulas registradas por mes.
    Agrupa los nuevos registros en los últimos N meses en orden cronológico.
    """
    docs = find_all("formula")
    dates = [_parse_iso_date(d.get("created_at")) for d in docs]
    valid_dates = [d for d in dates if d is not None]

    # Determinamos el mes de referencia a partir del último registro o de la fecha actual
    ref_date = max(valid_dates) if valid_dates else datetime.now()

    # Construimos la secuencia de los últimos `meses`
    month_slots = []
    curr_year = ref_date.year
    curr_month = ref_date.month

    for i in range(meses - 1, -1, -1):
        m = curr_month - i
        y = curr_year
        while m <= 0:
            m += 12
            y -= 1
        month_slots.append((y, m))

    # Contabilizamos registros por (año, mes)
    counts = Counter((d.year, d.month) for d in valid_dates)

    items = []
    for y, m in month_slots:
        qty = counts.get((y, m), 0)
        items.append({
            "anio": y,
            "mes": m,
            "mes_nombre": _MESES_ABREV[m],
            "mes_completo": _MESES_COMPLETO[m],
            "cantidad_registros": qty,
        })

    return {
        "indicador": "Fórmulas registradas por mes",
        "descripcion": "Cantidad de nuevos registros por mes en orden cronológico",
        "total_meses_evaluados": len(items),
        "total_registros_periodo": sum(item["cantidad_registros"] for item in items),
        "items": items,
    }


def get_formulas_detalle(page=1, limit=10, estado="todas", search=None):
    """
    Indicador 6: Tabla de datos - Detalle de fórmulas.
    Proporciona listado paginado con código, nombre, tipo, última actualización y estado.
    """
    docs = find_all("formula")

    # Filtrado por estado si aplica
    if estado == "activas":
        docs = [d for d in docs if bool(d.get("status", True))]
    elif estado == "en_revision":
        docs = [d for d in docs if not bool(d.get("status", True))]

    # Filtrado por búsqueda en código, nombre o descripción
    if search:
        term = str(search).lower().strip()
        docs = [
            d for d in docs
            if term in str(d.get("_id", "")).lower()
            or term in str(d.get("name", "")).lower()
            or term in str(d.get("description", "")).lower()
        ]

    total_filtrados = len(docs)

    # Ordenar por fecha de última actualización descendente
    def _sort_key(doc):
        dt = _parse_iso_date(doc.get("update_at") or doc.get("created_at"))
        return dt or datetime.min

    docs_sorted = sorted(docs, key=_sort_key, reverse=True)

    # Paginación
    page = max(1, int(page))
    limit = max(1, int(limit))
    start_idx = (page - 1) * limit
    paginated_docs = docs_sorted[start_idx : start_idx + limit]
    total_paginas = math.ceil(total_filtrados / limit) if limit > 0 else 1

    formatted_items = []
    for d in paginated_docs:
        is_active = bool(d.get("status", True))
        formatted_items.append({
            "codigo": d.get("_id"),
            "nombre": d.get("name", ""),
            "descripcion": d.get("description", ""),
            "iniciales": _get_initials(d.get("name", "")),
            "tipo": _infer_formula_type(d),
            "ultima_actualizacion": _format_date_dmy(d.get("update_at") or d.get("created_at")),
            "estado": is_active,
            "estado_texto": "Activa" if is_active else "En revisión",
            "unidad": d.get("unit", "litros"),
            "lote_estandar": d.get("standard_batch", 1),
            "tiempo_produccion": d.get("production_time"),
            "costo_preparacion": d.get("preparation_cost"),
            "precio_sugerido": d.get("suggested_price"),
        })

    return {
        "indicador": "Detalle de fórmulas",
        "descripcion": "Listado de fórmulas para revisar los registros y su estado",
        "items": formatted_items,
        "paginacion": {
            "total_registros": total_filtrados,
            "pagina_actual": page,
            "limite_por_pagina": limit,
            "total_paginas": total_paginas,
            "mostrando_mensaje": f"Mostrando {len(formatted_items)} de {total_filtrados} fórmulas",
        },
    }


def get_formulas_dashboard(dias=30, meses=6, limit_detalle=4):
    """
    Consolida los 6 indicadores del dashboard de fórmulas en un único objeto de respuesta
    ideal para alimentar la vista Figma completa en una sola llamada.
    """
    return {
        "panel": "Análisis de fórmulas - AgroTecno",
        "periodo_filtro": f"Últimos {dias} días",
        "indicadores": {
            "indicador_1_kpi_total": get_formulas_kpi_total(dias=dias),
            "indicador_2_kpi_activas": get_formulas_kpi_activas(),
            "indicador_3_kpi_en_revision": get_formulas_kpi_en_revision(),
            "indicador_4_distribucion_estado": get_formulas_distribucion_estado(),
            "indicador_5_evolucion_mensual": get_formulas_evolucion_mensual(meses=meses),
            "indicador_6_detalle_reciente": get_formulas_detalle(page=1, limit=limit_detalle),
        },
    }


def get_top_selling_formulas_analysis(year=None, start_date=None, end_date=None, limit=5):
    """
    Ranking transaccional: Fórmulas más vendidas en un periodo del año (Híbrido SQL Server + MongoDB).
    - Consulta agregaciones de ventas en SQL Server (ventas.order_detail + ventas.sales_order).
    - Cruza con MongoDB (colección 'formula') para enriquecer con nombres, tipos y detalles de catálogo.
    """
    sales_rows = sql_formula_repo.get_top_selling_formulas(
        year=year,
        start_date=start_date,
        end_date=end_date,
        limit=limit,
    )

    mongo_docs = {d.get("_id"): d for d in find_all("formula")}

    items = []
    for rank, row in enumerate(sales_rows, start=1):
        formula_id = row.get("mongo_formula_id")
        mongo_doc = mongo_docs.get(formula_id)

        nombre = mongo_doc.get("name") if mongo_doc else f"Fórmula {formula_id}"
        descripcion = mongo_doc.get("description", "") if mongo_doc else ""
        tipo = _infer_formula_type(mongo_doc) if mongo_doc else "Especialidad"
        iniciales = _get_initials(nombre)

        unidades = float(row.get("total_unidades_vendidas", 0))
        recaudado = float(row.get("total_recaudado", 0))
        ordenes = int(row.get("total_ordenes", 0))
        precio_prom = float(row.get("precio_promedio_venta", 0)) if row.get("precio_promedio_venta") else None

        items.append({
            "posicion": rank,
            "formula_id": formula_id,
            "formula_name": nombre,
            "descripcion": descripcion,
            "tipo": tipo,
            "iniciales": iniciales,
            "unidades_vendidas": unidades,
            "etiqueta_unidades": f"{int(unidades)} unidades",
            "total_ordenes": ordenes,
            "monto_total_recaudado": round(recaudado, 2),
            "precio_promedio_venta": round(precio_prom, 2) if precio_prom else None,
            "precio_sugerido_catalogo": mongo_doc.get("suggested_price") if mongo_doc else None,
            "costo_preparacion_catalogo": mongo_doc.get("preparation_cost") if mongo_doc else None,
            "unidad": mongo_doc.get("unit", "litros") if mongo_doc else "litros",
            "estado_formula": bool(mongo_doc.get("status", True)) if mongo_doc else True,
            "primera_venta": _format_date_dmy(row.get("primera_venta")),
            "ultima_venta": _format_date_dmy(row.get("ultima_venta")),
        })

    total_unidades = sum(item["unidades_vendidas"] for item in items)
    total_recaudado = sum(item["monto_total_recaudado"] for item in items)

    return {
        "indicador": "Fórmulas más vendidas",
        "descripcion": "Ranking de fórmulas por unidades vendidas en el periodo seleccionado (SQL Server + MongoDB)",
        "periodo": {
            "anio": year,
            "fecha_inicio": str(start_date) if start_date else None,
            "fecha_fin": str(end_date) if end_date else None,
            "filtro_aplicado": f"Año {year}" if year else ("Rango personalizado" if start_date or end_date else "Todo el histórico"),
        },
        "fuente_datos": {
            "transaccional": "SQL Server (ventas.order_detail, ventas.sales_order)",
            "catalogo": "MongoDB (colección 'formula')",
        },
        "total_formulas_en_ranking": len(items),
        "total_unidades_vendidas": total_unidades,
        "monto_total_recaudado": round(total_recaudado, 2),
        "items": items,
    }


