import os

from dotenv import load_dotenv

load_dotenv()

# MongoDB Configuration
MONGODB_URI = os.getenv("MONGODB_URI") or os.getenv(
    "MONGO_URI",
    "mongodb://admin:Mongo12345!@localhost:27017/AgroTecnoDB?authSource=admin",
)
MONGODB_DATABASE = os.getenv("MONGODB_DATABASE", "agrotecno_db")
MONGODB_COLLECTIONS = ("customer", "formula", "supply")

# SQL Server Configuration
SQLSERVER_HOST = os.getenv("SQLSERVER_HOST", "localhost")
SQLSERVER_PORT = int(os.getenv("SQLSERVER_PORT", "1433"))
SQLSERVER_DATABASE = os.getenv("SQLSERVER_DATABASE", "agroTecno_db")
SQLSERVER_USER = os.getenv("SQLSERVER_USER", "sa")
SQLSERVER_PASSWORD = os.getenv("SQLSERVER_PASSWORD", "SqlServer12345!")
SQLSERVER_DRIVER = os.getenv("SQLSERVER_DRIVER", "ODBC Driver 18 for SQL Server")
SQLSERVER_TRUST_SERVER_CERTIFICATE = os.getenv("SQLSERVER_TRUST_SERVER_CERTIFICATE", "yes")
