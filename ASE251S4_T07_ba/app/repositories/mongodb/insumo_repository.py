from app.repositories.mongodb.formula_repository import find_all, find_by_id


def get_insumos(limit=1000):
    """Retrieve all supplies from the MongoDB 'supply' collection."""
    return find_all("supply", limit=limit)


def get_insumo_by_id(supply_id):
    """Retrieve a single supply by _id (e.g. 'SUP001')."""
    return find_by_id("supply", supply_id)


def get_supplies_low_stock():
    """Query supplies where current stock is lower than minimum stock using MongoDB expr."""
    query = {
        "$expr": {
            "$lt": ["$inventory.current_stock", "$inventory.minimum_stock"]
        }
    }
    return find_all("supply", query=query)


# Alias
get_supplies = get_insumos
get_supply_by_id = get_insumo_by_id