export interface Installment {
  installmentId?: number;
  collectionId?: number;
  installmentNum: number;
  dueDate: string;
  amountDue: number;
  amountPaid?: number;
  paymentType?: string;
  paymentDate?: string;
  observation?: string;
  status: string; // 'P': pendiente, 'A': pagado, 'C': cancelado
  createdAt?: string;
}

export interface PaymentCollection {
  collectionId: number;
  orderId: number;
  mongoCustomerId: string;
  totalAmount: number;
  installmentsCount: number;
  paymentStatus: string;
  createdAt?: string;
  updatedAt?: string;
  deletedAt?: string;
  restoredAt?: string;
}
