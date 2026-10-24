from contextlib import contextmanager
from datetime import date, datetime
from decimal import Decimal
import pyodbc

from app.config.settings import (
    SQLSERVER_DATABASE,
    SQLSERVER_DRIVER,
    SQLSERVER_HOST,
    SQLSERVER_PASSWORD,
    SQLSERVER_PORT,
    SQLSERVER_TRUST_SERVER_CERTIFICATE,
    SQLSERVER_USER,
)


def _detect_driver():
    """Detect available ODBC Driver for SQL Server installed on system."""
    available = pyodbc.drivers()
    preferred = [
        SQLSERVER_DRIVER,
        "ODBC Driver 18 for SQL Server",
        "ODBC Driver 17 for SQL Server",
        "SQL Server",
    ]
    for drv in preferred:
        if drv in available:
            return drv
    if available:
        return available[0]
    return "ODBC Driver 18 for SQL Server"


def get_connection_string():
    driver = _detect_driver()
    conn_str = (
        f"DRIVER={{{driver}}};"
        f"SERVER={SQLSERVER_HOST},{SQLSERVER_PORT};"
        f"DATABASE={SQLSERVER_DATABASE};"
        f"UID={SQLSERVER_USER};"
        f"PWD={SQLSERVER_PASSWORD};"
    )
    if "ODBC Driver" in driver:
        conn_str += f"TrustServerCertificate={SQLSERVER_TRUST_SERVER_CERTIFICATE};"
        conn_str += "Encrypt=yes;"
    return conn_str


def get_connection(timeout=5):
    """Create and return a new pyodbc connection to SQL Server."""
    conn_str = get_connection_string()
    return pyodbc.connect(conn_str, timeout=timeout)


@contextmanager
def get_cursor():
    """Context manager for obtaining a connection and cursor with auto-commit/rollback."""
    conn = get_connection()
    cursor = conn.cursor()
    try:
        yield cursor
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        cursor.close()
        conn.close()


def validate_connection():
    """Verify connectivity to SQL Server and check required database schemas."""
    with get_cursor() as cursor:
        cursor.execute("SELECT 1")
        row = cursor.fetchone()
        if not row or row[0] != 1:
            raise RuntimeError("Respuesta inválida al verificar conexión a SQL Server")

        cursor.execute("SELECT schema_name FROM information_schema.schemata")
        schemas = {r[0].lower() for r in cursor.fetchall()}
        required_schemas = {"seguridad", "ventas", "cobranzas"}
        missing = required_schemas - schemas
        if missing:
            raise RuntimeError(
                f"Faltan esquemas requeridos en {SQLSERVER_DATABASE}: {', '.join(missing)}"
            )

    return {"database": SQLSERVER_DATABASE, "schemas": list(required_schemas)}


def connection_error_message(error):
    if isinstance(error, pyodbc.Error):
        return f"Error de conexión o consulta con SQL Server: {error}"
    return str(error)


def row_to_dict(cursor, row):
    """Convert pyodbc Row to dict with JSON serializable values."""
    columns = [col[0] for col in cursor.description]
    res = {}
    for col, val in zip(columns, row):
        if isinstance(val, Decimal):
            res[col] = float(val)
        elif isinstance(val, (datetime, date)):
            res[col] = val.isoformat()
        else:
            res[col] = val
    return res


def execute_query(sql, params=None):
    """Execute SQL query and return all results as list of dictionaries."""
    with get_cursor() as cursor:
        cursor.execute(sql, params or ())
        if cursor.description:
            return [row_to_dict(cursor, row) for row in cursor.fetchall()]
        return []
