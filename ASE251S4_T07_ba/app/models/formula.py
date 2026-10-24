from dataclasses import dataclass, field
from typing import List, Optional


@dataclass
class FormulaIngredient:
    supply_id: str
    supply_name: str
    quantity_required: float
    percentage: float


@dataclass
class FormulaInventoryItem:
    gallon_type: int
    stock_quantity: int
    last_update: Optional[str] = None


@dataclass
class Formula:
    id: str
    name: str
    description: Optional[str] = None
    standard_batch: int = 1
    unit: str = "litros"
    production_time: Optional[int] = None
    preparation_cost: Optional[float] = None
    suggested_price: Optional[float] = None
    status: bool = True
    ingredients: List[FormulaIngredient] = field(default_factory=list)
    formula_inventory: List[FormulaInventoryItem] = field(default_factory=list)
