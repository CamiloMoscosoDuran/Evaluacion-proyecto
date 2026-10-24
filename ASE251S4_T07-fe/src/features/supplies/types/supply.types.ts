export interface SupplyInventory {
  current_stock: number;
  minimum_stock: number;
  expiration_date?: string;
}

export interface Supply {
  id?: string;
  _id?: string;
  name: string;
  description?: string;
  category: string;
  unit: string;
  location: string;
  status: boolean;
  inventory: SupplyInventory;
  created_at?: string;
  update_at?: string;
  deleted_at?: string;
  restored_at?: string;
}
