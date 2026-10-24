from app.database.mongodb import get_database, serialize_docs, serialize_doc


class CollectionNotFoundError(RuntimeError):
    pass


def _collection(name):
    database = get_database()
    if name not in database.list_collection_names():
        raise CollectionNotFoundError(
            f"La colección '{name}' no existe en la base de datos configurada"
        )
    return database[name]


def find_all(collection_name, query=None, limit=1000):
    """Find documents in collection, preserving _id and serializing BSON types."""
    cursor = _collection(collection_name).find(query or {}).limit(limit)
    return serialize_docs(list(cursor))


def find_by_id(collection_name, doc_id):
    """Find a single document by its _id."""
    doc = _collection(collection_name).find_one({"_id": doc_id})
    return serialize_doc(doc)


def count_documents(collection_name, query=None):
    """Count documents matching query."""
    return _collection(collection_name).count_documents(query or {})
