export interface Ingredient {
  supply_id?: string;
  supply_name: string;
  quantity_required: number;
  percentage: number;
}

export interface Recipe {
  version: string;
  ingredients: Ingredient[];
}

export interface FormulaInventory {
  gallon_type: number; // 1 o 20 litros
  stock_quantity: number;
  last_update?: string;
}

export interface Formula {
  _id?: string;
  name: string;
  description?: string;
  standard_batch: number;
  unit: string;
  production_time: number;
  preparation_cost: number;
  suggested_price: number;
  status: boolean;
  recipe?: Recipe;
  formula_inventory?: FormulaInventory[];
  created_at?: string;
  update_at?: string;
  deleted_at?: string;
  restored_at?: string;
}
