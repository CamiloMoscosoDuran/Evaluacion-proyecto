import { http } from '../../../shared/utils/http';
import { SalesOrderResponse, SalesOrderRequest } from '../types/salesOrder.types';

export const salesOrdersService = {
  async getOrders(): Promise<SalesOrderResponse[]> {
    return await http.get<SalesOrderResponse[]>('/sales-orders');
  },

  async getOrderById(id: number): Promise<SalesOrderResponse> {
    return await http.get<SalesOrderResponse>(`/sales-orders/${id}`);
  },

  async createOrder(request: SalesOrderRequest): Promise<SalesOrderResponse> {
    return await http.post<SalesOrderResponse>('/sales-orders', request);
  },

  async updateOrder(id: number, request: SalesOrderRequest): Promise<SalesOrderResponse> {
    return await http.put<SalesOrderResponse>(`/sales-orders/${id}`, request);
  },
};
