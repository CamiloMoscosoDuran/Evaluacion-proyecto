def format_formula_response(doc):
    """Format MongoDB formula document for API response."""
    if not doc:
        return None
    recipe = doc.get("recipe", {})
    return {
        "id": doc.get("_id"),
        "name": doc.get("name"),
        "description": doc.get("description"),
        "standard_batch": doc.get("standard_batch"),
        "unit": doc.get("unit"),
        "production_time": doc.get("production_time"),
        "preparation_cost": doc.get("preparation_cost"),
        "suggested_price": doc.get("suggested_price"),
        "status": doc.get("status"),
        "version": recipe.get("version"),
        "ingredients": recipe.get("ingredients", []),
        "formula_inventory": doc.get("formula_inventory", []),
    }


def format_formulas_response(docs):
    return [format_formula_response(doc) for doc in docs]
