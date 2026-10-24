from app.database.sqlserver import execute_query


def get_recent_sales_orders(limit=10, order_status=None, days=None):
    """
    Retrieve recent sales orders from ventas.sales_order.
    Supports filtering by order_status ('C', 'P', 'A') and last N days.
    Used for the 'Ordenes de venta recientes' transactional indicator (Insumos dashboard).

    Status codes:
        C -> Completado
        P -> Pendiente
        A -> Anulado
    """
    conditions = []
    params = []

    if order_status and order_status.upper() != "TODOS":
        conditions.append("order_status = ?")
        params.append(order_status.upper())

    if days:
        conditions.append("entry_date >= DATEADD(day, ?, CAST(GETDATE() AS DATE))")
        params.append(-int(days))

    where_clause = ("WHERE " + " AND ".join(conditions)) if conditions else ""
    top_clause = f"TOP {int(limit)}" if limit else "TOP 10"

    sql = f"""
        SELECT {top_clause}
            so.order_id,
            so.referral_guide,
            so.mongo_customer_id,
            so.entry_date,
            so.total_order,
            so.order_status,
            so.total_items
        FROM ventas.vw_resumen_ordenes so
        {where_clause}
        ORDER BY so.entry_date DESC, so.order_id DESC;
    """
    return execute_query(sql, tuple(params) if params else None)

