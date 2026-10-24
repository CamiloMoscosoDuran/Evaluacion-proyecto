import { useState, useEffect, useCallback } from 'react';
import { SalesOrderResponse, SalesOrderRequest } from '../types/salesOrder.types';
import { salesOrdersService } from '../services/salesOrdersService';

export function useSalesOrders() {
  const [orders, setOrders] = useState<SalesOrderResponse[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchOrders = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await salesOrdersService.getOrders();
      setOrders(Array.isArray(data) ? data : []);
    } catch (err: unknown) {
      console.error('Error fetching sales orders:', err);
      setError(err instanceof Error ? err.message : 'Error al cargar órdenes de venta');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  const saveOrder = async (data: SalesOrderRequest, id?: number) => {
    try {
      if (id) {
        await salesOrdersService.updateOrder(id, data);
      } else {
        await salesOrdersService.createOrder(data);
      }
      await fetchOrders();
    } catch (err: unknown) {
      throw err;
    }
  };

  return {
    orders,
    isLoading,
    error,
    reload: fetchOrders,
    saveOrder,
  };
}
