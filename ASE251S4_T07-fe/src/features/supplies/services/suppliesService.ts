import { http } from '../../../shared/utils/http';
import { Supply } from '../types/supply.types';

export const suppliesService = {
  async getSupplies(): Promise<Supply[]> {
    return await http.get<Supply[]>('/supply');
  },

  async getSuppliesByEstado(estado: 'activos' | 'inactivos' | 'todos'): Promise<Supply[]> {
    if (estado === 'todos') return this.getSupplies();
    const boolVal = estado === 'activos';
    return await http.get<Supply[]>(`/supply/estado/${boolVal}`);
  },

  async getSupplyById(id: string): Promise<Supply> {
    return await http.get<Supply>(`/supply/${id}`);
  },

  async createSupply(supply: Omit<Supply, 'id' | '_id'>): Promise<Supply> {
    return await http.post<Supply>('/supply', supply);
  },

  async updateSupply(id: string, supply: Partial<Supply>): Promise<Supply> {
    return await http.put<Supply>(`/supply/${id}`, supply);
  },

  async deleteSupply(id: string): Promise<Supply> {
    return await http.patch<Supply>(`/supply/delete/${id}`);
  },

  async restoreSupply(id: string): Promise<Supply> {
    return await http.patch<Supply>(`/supply/restore/${id}`);
  },

  async exportExcel(estado: string = 'todos'): Promise<void> {
    const endpoint = estado === 'todos' ? '/supply/exportar/excel' : `/supply/exportar/excel/${estado}`;
    await http.downloadBlob(endpoint, `insumos_${estado}.xlsx`);
  },

  async exportPdf(estado: string = 'todos'): Promise<void> {
    const endpoint = estado === 'todos' ? '/supply/exportar/pdf' : `/supply/exportar/pdf/${estado}`;
    await http.downloadBlob(endpoint, `insumos_${estado}.pdf`);
  },

  async importCsv(file: File): Promise<Supply[]> {
    const formData = new FormData();
    formData.append('file', file);
    return await http.post<Supply[]>('/supply/importar', formData);
  },
};
