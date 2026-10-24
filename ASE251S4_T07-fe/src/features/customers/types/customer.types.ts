export interface FarmPlot {
  farm_name: string;
  hectareas: number;
  type_crop: string;
}

export interface AddressSpecifics {
  settlement_type?: string;
  settlement_name?: string;
  street_type?: string;
  street_name?: string;
  reference?: string;
}

export interface CustomerAddress {
  department: string;
  province: string;
  district: string;
  especifics?: AddressSpecifics;
}

export interface Customer {
  id?: string;
  _id?: string;
  first_name: string;
  last_name: string;
  document_type: string;
  document_number: string;
  phone?: string[];
  email?: string;
  registration_date?: string;
  status: boolean;
  farm_plot?: FarmPlot[];
  address?: CustomerAddress;
  created_at?: string;
  update_at?: string;
  deleted_at?: string;
  restored_at?: string;
}
