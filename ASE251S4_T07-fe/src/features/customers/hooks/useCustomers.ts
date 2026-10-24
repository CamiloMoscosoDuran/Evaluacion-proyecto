import { useState, useEffect, useCallback } from 'react';
import { Customer } from '../types/customer.types';
import { customersService } from '../services/customersService';

export function useCustomers() {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [filterStatus, setFilterStatus] = useState<'todos' | 'activos' | 'inactivos'>('todos');

  const fetchCustomers = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await customersService.getCustomersByEstado(filterStatus);
      setCustomers(Array.isArray(data) ? data : []);
    } catch (err: unknown) {
      console.error('Error fetching customers:', err);
      setError(err instanceof Error ? err.message : 'Error al cargar clientes');
    } finally {
      setIsLoading(false);
    }
  }, [filterStatus]);

  useEffect(() => {
    fetchCustomers();
  }, [fetchCustomers]);

  const saveCustomer = async (data: Partial<Customer>, id?: string) => {
    try {
      if (id) {
        await customersService.updateCustomer(id, data);
      } else {
        await customersService.createCustomer(data as Omit<Customer, 'id' | '_id'>);
      }
      await fetchCustomers();
    } catch (err: unknown) {
      throw err;
    }
  };

  const deleteCustomer = async (id: string) => {
    try {
      await customersService.deleteCustomer(id);
      await fetchCustomers();
    } catch (err: unknown) {
      throw err;
    }
  };

  const restoreCustomer = async (id: string) => {
    try {
      await customersService.restoreCustomer(id);
      await fetchCustomers();
    } catch (err: unknown) {
      throw err;
    }
  };

  return {
    customers,
    isLoading,
    error,
    filterStatus,
    setFilterStatus,
    reload: fetchCustomers,
    saveCustomer,
    deleteCustomer,
    restoreCustomer,
  };
}
