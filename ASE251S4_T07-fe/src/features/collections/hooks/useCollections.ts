import { useState, useEffect, useCallback } from 'react';
import { PaymentCollection, Installment } from '../types/collection.types';
import { collectionsService } from '../services/collectionsService';

export function useCollections() {
  const [collections, setCollections] = useState<PaymentCollection[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchCollections = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await collectionsService.getCollections();
      setCollections(Array.isArray(data) ? data : []);
    } catch (err: unknown) {
      console.error('Error fetching collections:', err);
      setError(err instanceof Error ? err.message : 'Error al cargar cobranzas');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCollections();
  }, [fetchCollections]);

  return {
    collections,
    isLoading,
    error,
    reload: fetchCollections,
  };
}
