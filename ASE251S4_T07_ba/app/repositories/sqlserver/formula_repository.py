from app.database.sqlserver import execute_query


def get_formula_sales_summary():
    """Aggregate total units sold and total revenue grouped by mongo_formula_id from SQL Server."""
    sql = """
        SELECT 
            od.mongo_formula_id,
            COUNT(od.order_detail_id) AS total_orders_containing,
            SUM(od.quantity) AS total_units_sold,
            SUM(od.quantity * od.unit_price) AS total_revenue,
            AVG(od.unit_price) AS avg_unit_price
        FROM ventas.order_detail od
        GROUP BY od.mongo_formula_id
        ORDER BY total_revenue DESC;
    """
    return execute_query(sql)


def get_top_selling_formulas(year=None, start_date=None, end_date=None, limit=5):
    """
    Retrieve top selling formulas aggregated from ventas.order_detail and ventas.sales_order.
    Excludes cancelled orders ('A').
    Supports filtering by year or date range (start_date, end_date).
    """
    conditions = ["so.order_status <> 'A'"]
    params = []

    if year is not None:
        conditions.append("YEAR(so.entry_date) = ?")
        params.append(int(year))
    if start_date:
        conditions.append("so.entry_date >= ?")
        params.append(str(start_date))
    if end_date:
        conditions.append("so.entry_date <= ?")
        params.append(str(end_date))

    where_clause = " AND ".join(conditions)
    top_clause = f"TOP {int(limit)}" if limit else ""

    sql = f"""
        SELECT {top_clause}
            od.mongo_formula_id,
            SUM(od.quantity) AS total_unidades_vendidas,
            COUNT(DISTINCT so.order_id) AS total_ordenes,
            SUM(od.subtotal) AS total_recaudado,
            AVG(od.unit_price) AS precio_promedio_venta,
            MIN(so.entry_date) AS primera_venta,
            MAX(so.entry_date) AS ultima_venta
        FROM ventas.order_detail od
        INNER JOIN ventas.sales_order so ON od.sales_order_order_id = so.order_id
        WHERE {where_clause}
        GROUP BY od.mongo_formula_id
        ORDER BY total_unidades_vendidas DESC;
    """
    return execute_query(sql, tuple(params) if params else None)



def get_order_details_by_formula(formula_id):
    """Retrieve all sales details for a given mongo_formula_id."""
    sql = """
        SELECT 
            od.order_detail_id,
            od.sales_order_order_id AS order_id,
            so.referral_guide,
            so.mongo_customer_id,
            so.entry_date,
            od.quantity,
            od.unit_price,
            (od.quantity * od.unit_price) AS subtotal,
            so.order_status
        FROM ventas.order_detail od
        INNER JOIN ventas.sales_order so ON od.sales_order_order_id = so.order_id
        WHERE od.mongo_formula_id = ?
        ORDER BY so.entry_date DESC;
    """
    return execute_query(sql, (formula_id,))


def get_sales_orders_summary():
    """Retrieve sales orders overview from SQL Server view 'ventas.vw_resumen_ordenes'."""
    sql = """
        SELECT 
            order_id,
            referral_guide,
            mongo_customer_id,
            entry_date,
            total_order,
            order_status,
            total_items
        FROM ventas.vw_resumen_ordenes
        ORDER BY order_id ASC;
    """
    return execute_query(sql)


def get_customer_account_status():
    """Retrieve customer financial status from view 'cobranzas.vw_estado_cuenta_cliente'."""
    sql = """
        SELECT 
            mongo_customer_id,
            total_collections,
            total_financed,
            total_collected,
            total_outstanding_balance
        FROM cobranzas.vw_estado_cuenta_cliente
        ORDER BY total_outstanding_balance DESC;
    """
    return execute_query(sql)


def get_overdue_portfolio():
    """Retrieve overdue installments from view 'cobranzas.vw_cartera_vencida'."""
    sql = """
        SELECT 
            installment_id,
            collection_id,
            order_id,
            mongo_customer_id,
            installment_num,
            due_date,
            amount_due,
            amount_paid,
            balance_pending,
            days_overdue
        FROM cobranzas.vw_cartera_vencida
        ORDER BY days_overdue DESC;
    """
    return execute_query(sql)
