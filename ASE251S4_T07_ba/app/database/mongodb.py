from datetime import date, datetime
from decimal import Decimal
from bson.decimal128 import Decimal128
from pymongo import MongoClient
from pymongo.errors import PyMongoError

from app.config.settings import (
    MONGODB_COLLECTIONS,
    MONGODB_DATABASE,
    MONGODB_URI,
)

_client = None


def get_client():
    """Return the process-wide MongoDB client, creating it on first use."""
    global _client

    if _client is None:
        if not MONGODB_URI:
            raise RuntimeError("Falta configurar MONGODB_URI en las variables de entorno")
        _client = MongoClient(
            MONGODB_URI,
            serverSelectionTimeoutMS=5000,
            connectTimeoutMS=5000,
            socketTimeoutMS=30000,
            maxPoolSize=20,
        )
    return _client


def get_database():
    return get_client()[MONGODB_DATABASE]


def validate_connection():
    """Verify MongoDB access and that the required collections exist."""
    database = get_database()
    get_client().admin.command("ping")
    existing_collections = set(database.list_collection_names())
    missing_collections = [
        name for name in MONGODB_COLLECTIONS if name not in existing_collections
    ]
    if missing_collections:
        raise RuntimeError(
            f"Faltan colecciones requeridas en {MONGODB_DATABASE}: "
            + ", ".join(missing_collections)
        )
    return {"database": MONGODB_DATABASE, "collections": list(MONGODB_COLLECTIONS)}


def close_connection():
    global _client

    if _client is not None:
        _client.close()
        _client = None


def connection_error_message(error):
    if isinstance(error, PyMongoError):
        return f"Error de conexión o consulta con MongoDB: {error}"
    return str(error)


def serialize_value(val):
    """Convert MongoDB BSON types (Decimal128, datetime, etc.) to JSON serializable Python types."""
    if isinstance(val, Decimal128):
        return float(val.to_decimal())
    if isinstance(val, Decimal):
        return float(val)
    if isinstance(val, (datetime, date)):
        return val.isoformat()
    if isinstance(val, dict):
        return {k: serialize_value(v) for k, v in val.items()}
    if isinstance(val, list):
        return [serialize_value(item) for item in val]
    return val


def serialize_doc(doc):
    """Serialize a single document keeping _id as a string if needed."""
    if not doc:
        return doc
    return serialize_value(doc)


def serialize_docs(docs):
    return [serialize_doc(doc) for doc in docs]
