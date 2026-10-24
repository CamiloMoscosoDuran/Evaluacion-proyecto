export interface FieldMeeting {
  id?: string;
  _id?: string;
  meeting_date_time: string;
  customer_id: string;
  farm_name: string;
  farm_address?: Record<string, unknown> | string;
  order_sales_id: string;
  fruit_quality?: string;
  observation?: string;
  visit_status: 'Pendiente' | 'Completa' | 'Pospuesta' | 'Cancelada' | string;
  created_at?: string;
  update_at?: string;
  deleted_at?: string;
  restored_at?: string;
}
