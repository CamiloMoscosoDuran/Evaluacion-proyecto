from app.repositories.mongodb.formula_repository import find_all, find_by_id


def get_clients(limit=1000):
    """Retrieve all clients from the MongoDB 'customer' collection."""
    return find_all("customer", limit=limit)


def get_client_by_id(client_id):
    """Retrieve a single customer by _id (e.g. 'CUS001')."""
    return find_by_id("customer", client_id)


# Alias
get_customers = get_clients
get_customer_by_id = get_client_by_id