import { useState, useEffect, useCallback } from 'react';
import { Formula } from '../types/product.types';
import { productsService } from '../services/productsService';

export function useProducts() {
  const [formulas, setFormulas] = useState<Formula[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [filterStatus, setFilterStatus] = useState<'todos' | 'activos' | 'inactivos'>('todos');

  const fetchFormulas = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await productsService.getFormulasByEstado(filterStatus);
      setFormulas(Array.isArray(data) ? data : []);
    } catch (err: unknown) {
      console.error('Error fetching formulas:', err);
      setError(err instanceof Error ? err.message : 'Error al cargar fórmulas');
    } finally {
      setIsLoading(false);
    }
  }, [filterStatus]);

  useEffect(() => {
    fetchFormulas();
  }, [fetchFormulas]);

  const saveFormula = async (data: Partial<Formula>, id?: string) => {
    try {
      if (id) {
        await productsService.updateFormula(id, data);
      } else {
        await productsService.createFormula(data as Omit<Formula, '_id'>);
      }
      await fetchFormulas();
    } catch (err: unknown) {
      throw err;
    }
  };

  const deleteFormula = async (id: string) => {
    try {
      await productsService.deleteFormula(id);
      await fetchFormulas();
    } catch (err: unknown) {
      throw err;
    }
  };

  const restoreFormula = async (id: string) => {
    try {
      await productsService.restoreFormula(id);
      await fetchFormulas();
    } catch (err: unknown) {
      throw err;
    }
  };

  return {
    formulas,
    isLoading,
    error,
    filterStatus,
    setFilterStatus,
    reload: fetchFormulas,
    saveFormula,
    deleteFormula,
    restoreFormula,
  };
}
