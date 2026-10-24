import { useState, useEffect, useCallback } from 'react';
import { Supply } from '../types/supply.types';
import { suppliesService } from '../services/suppliesService';

export function useSupplies() {
  const [supplies, setSupplies] = useState<Supply[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [filterStatus, setFilterStatus] = useState<'todos' | 'activos' | 'inactivos'>('todos');

  const fetchSupplies = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await suppliesService.getSuppliesByEstado(filterStatus);
      setSupplies(Array.isArray(data) ? data : []);
    } catch (err: unknown) {
      console.error('Error fetching supplies:', err);
      setError(err instanceof Error ? err.message : 'Error al cargar insumos');
    } finally {
      setIsLoading(false);
    }
  }, [filterStatus]);

  useEffect(() => {
    fetchSupplies();
  }, [fetchSupplies]);

  const saveSupply = async (data: Partial<Supply>, id?: string) => {
    try {
      if (id) {
        await suppliesService.updateSupply(id, data);
      } else {
        await suppliesService.createSupply(data as Omit<Supply, 'id' | '_id'>);
      }
      await fetchSupplies();
    } catch (err: unknown) {
      throw err;
    }
  };

  const deleteSupply = async (id: string) => {
    try {
      await suppliesService.deleteSupply(id);
      await fetchSupplies();
    } catch (err: unknown) {
      throw err;
    }
  };

  const restoreSupply = async (id: string) => {
    try {
      await suppliesService.restoreSupply(id);
      await fetchSupplies();
    } catch (err: unknown) {
      throw err;
    }
  };

  return {
    supplies,
    isLoading,
    error,
    filterStatus,
    setFilterStatus,
    reload: fetchSupplies,
    saveSupply,
    deleteSupply,
    restoreSupply,
  };
}
