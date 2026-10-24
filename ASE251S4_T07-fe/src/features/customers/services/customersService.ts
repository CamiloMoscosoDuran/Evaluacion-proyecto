import { http } from '../../../shared/utils/http';
import { Customer } from '../types/customer.types';

export const customersService = {
  async getCustomers(): Promise<Customer[]> {
    return await http.get<Customer[]>('/customer');
  },

  async getCustomersByEstado(estado: 'activos' | 'inactivos' | 'todos'): Promise<Customer[]> {
    if (estado === 'todos') return this.getCustomers();
    return await http.get<Customer[]>(`/customer/estado/${estado}`);
  },

  async getCustomerById(id: string): Promise<Customer> {
    return await http.get<Customer>(`/customer/${id}`);
  },

  async createCustomer(customer: Omit<Customer, 'id' | '_id'>): Promise<Customer> {
    return await http.post<Customer>('/customer', customer);
  },

  async updateCustomer(id: string, customer: Partial<Customer>): Promise<Customer> {
    return await http.put<Customer>(`/customer/${id}`, customer);
  },

  async deleteCustomer(id: string): Promise<Customer> {
    return await http.patch<Customer>(`/customer/delete/${id}`);
  },

  async restoreCustomer(id: string): Promise<Customer> {
    return await http.patch<Customer>(`/customer/restore/${id}`);
  },

  async exportExcel(estado: string = 'todos'): Promise<void> {
    const endpoint = estado === 'todos' ? '/customer/exportar/excel' : `/customer/exportar/excel/${estado}`;
    await http.downloadBlob(endpoint, `clientes_${estado}.xlsx`);
  },

  async exportPdf(estado: string = 'todos'): Promise<void> {
    const endpoint = estado === 'todos' ? '/customer/exportar/pdf' : `/customer/exportar/pdf/${estado}`;
    await http.downloadBlob(endpoint, `clientes_${estado}.pdf`);
  },

  async importCsv(file: File): Promise<Customer[]> {
    const formData = new FormData();
    formData.append('file', file);
    return await http.post<Customer[]>('/customer/importar', formData);
  },
};
