import { Customer } from '../../customers/types/customer.types';

export interface OrderDetail {
  orderDetailId?: number;
  salesOrderOrderId?: number;
  mongoFormulaId: string;
  quantity: number;
  unitPrice: number;
  subtotal?: number;
  createdAt?: string;
}

export interface SalesOrderRequest {
  mongoCustomerId: string;
  entryDate?: string;
  totalOrder?: number;
  orderStatus?: string;
  installments_count: number;
  orderDetails: OrderDetail[];
}

export interface SalesOrderResponse {
  orderId: number;
  referralGuide?: string;
  mongoCustomerId: string;
  customerInfo?: Customer;
  entryDate?: string;
  totalOrder: number;
  orderStatus: string;
  createdAt?: string;
  updatedAt?: string;
  deletedAt?: string;
  restoredAt?: string;
  orderDetails: OrderDetail[];
  paymentCollection?: {
    collectionId?: number;
    totalAmount?: number;
    installmentsCount?: number;
    paymentStatus?: string;
  };
}
