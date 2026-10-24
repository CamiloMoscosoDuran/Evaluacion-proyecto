from flask import Blueprint, jsonify

from app.database import mongodb, sqlserver

status_bp = Blueprint("status", __name__)


@status_bp.route("/")
def inicio():
    return {
        "estado": "Operativo",
        "servicio": "Backend Analítico Agrotecno",
        "mensaje": "API funcionando correctamente 🚀",
    }


@status_bp.route("/health")
def health():
    status = {
        "servicio": "Backend Analítico Agrotecno",
        "mongodb": {"conectado": False},
        "sqlserver": {"conectado": False},
    }

    # Verificar MongoDB
    try:
        mongo_info = mongodb.validate_connection()
        status["mongodb"] = {
            "conectado": True,
            "database": mongo_info.get("database"),
            "colecciones": mongo_info.get("collections"),
        }
    except Exception as e:
        status["mongodb"] = {
            "conectado": False,
            "error": mongodb.connection_error_message(e),
        }

    # Verificar SQL Server
    try:
        sql_info = sqlserver.validate_connection()
        status["sqlserver"] = {
            "conectado": True,
            "database": sql_info.get("database"),
            "esquemas": sql_info.get("schemas"),
        }
    except Exception as e:
        status["sqlserver"] = {
            "conectado": False,
            "error": sqlserver.connection_error_message(e),
        }

    http_status = 200 if (status["mongodb"]["conectado"] and status["sqlserver"]["conectado"]) else 207
    return jsonify(status), http_status