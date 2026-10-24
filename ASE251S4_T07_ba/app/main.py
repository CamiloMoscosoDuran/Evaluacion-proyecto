from flask import Flask

from app.database import mongodb, sqlserver
from app.routes.formula_analytics import analytics_bp
from app.routes.insumo_analytics import insumo_analytics_bp
from app.routes.status import status_bp
from app.swagger import swagger_bp, swagger_ui_bp

app = Flask(__name__)
app.json.ensure_ascii = False

app.register_blueprint(status_bp)
app.register_blueprint(analytics_bp)
app.register_blueprint(insumo_analytics_bp)
app.register_blueprint(swagger_bp)
app.register_blueprint(swagger_ui_bp)

# Validación de conexión inicial con MongoDB
try:
    mongo_info = mongodb.validate_connection()
    print(
        f"[MongoDB] Conectado exitosamente | Base de datos: '{mongo_info['database']}' | "
        f"Colecciones: {', '.join(mongo_info['collections'])}"
    )
except Exception as error:
    message = mongodb.connection_error_message(error)
    print(f"[MongoDB] Estado de conexión: {message}")

# Validación de conexión inicial con SQL Server
try:
    sql_info = sqlserver.validate_connection()
    print(
        f"[SQL Server] Conectado exitosamente | Base de datos: '{sql_info['database']}' | "
        f"Esquemas: {', '.join(sql_info['schemas'])}"
    )
except Exception as error:
    message = sqlserver.connection_error_message(error)
    print(f"[SQL Server] Estado de conexión: {message}")

if __name__ == "__main__":
    app.run(
        host="0.0.0.0",
        port=5000,
        debug=True,
    )