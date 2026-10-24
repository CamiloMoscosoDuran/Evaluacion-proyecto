import { http } from '../../../shared/utils/http';
import { Formula } from '../types/product.types';

export const productsService = {
  async getFormulas(): Promise<Formula[]> {
    return await http.get<Formula[]>('/formula');
  },

  async getFormulasByEstado(estado: 'activos' | 'inactivos' | 'todos'): Promise<Formula[]> {
    if (estado === 'todos') return this.getFormulas();
    const boolVal = estado === 'activos';
    try {
      const data = await http.get<Formula[]>(`/formula/estado/${boolVal}`);
      return Array.isArray(data) ? data : [];
    } catch {
      const all = await this.getFormulas();
      return all.filter((f) => (f.status !== false) === boolVal);
    }
  },

  async getFormulaById(id: string): Promise<Formula> {
    return await http.get<Formula>(`/formula/${id}`);
  },

  async createFormula(formula: Omit<Formula, '_id' | 'created_at' | 'update_at' | 'deleted_at' | 'restored_at'>): Promise<Formula> {
    return await http.post<Formula>('/formula', formula);
  },

  async updateFormula(id: string, formula: Partial<Formula>): Promise<Formula> {
    return await http.put<Formula>(`/formula/${id}`, formula);
  },

  async deleteFormula(id: string): Promise<Formula> {
    return await http.patch<Formula>(`/formula/delete/${id}`);
  },

  async restoreFormula(id: string): Promise<Formula> {
    return await http.patch<Formula>(`/formula/restore/${id}`);
  },

  async exportExcel(estado: string = 'todos'): Promise<void> {
    const endpoint = estado === 'todos' ? '/formula/exportar/excel' : `/formula/exportar/excel/${estado}`;
    await http.downloadBlob(endpoint, `formulas_${estado}.xlsx`);
  },

  async exportPdf(estado: string = 'todos'): Promise<void> {
    const endpoint = estado === 'todos' ? '/formula/exportar/pdf' : `/formula/exportar/pdf/${estado}`;
    await http.downloadBlob(endpoint, `formulas_${estado}.pdf`);
  },

  async importCsv(file: File): Promise<Formula[]> {
    const formData = new FormData();
    formData.append('file', file);
    return await http.post<Formula[]>('/formula/importar', formData);
  },
};
