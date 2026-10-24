import { http } from '../../../shared/utils/http';
import { PaymentCollection, Installment } from '../types/collection.types';

export const collectionsService = {
  async getCollections(): Promise<PaymentCollection[]> {
    return await http.get<PaymentCollection[]>('/payment-collections');
  },

  async getCollectionById(id: number): Promise<PaymentCollection> {
    return await http.get<PaymentCollection>(`/payment-collections/${id}`);
  },

  async getInstallmentsByCollection(collectionId: number): Promise<Installment[]> {
    return await http.get<Installment[]>(`/installments/collection/${collectionId}`);
  },

  async createInstallment(collectionId: number, installment: Installment): Promise<Installment> {
    return await http.post<Installment>(`/payment-collections/${collectionId}/installments`, installment);
  },

  async updateInstallment(id: number, installment: Partial<Installment>): Promise<Installment> {
    return await http.put<Installment>(`/installments/${id}`, installment);
  },
};
