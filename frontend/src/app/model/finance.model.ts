export type FinancialDocumentType = 'SALES' | 'PURCHASE';
export type ReturnType = 'CUSTOMER_RETURN' | 'SUPPLIER_RETURN';
export type ReturnStatus = 'DRAFT' | 'VALIDATED' | 'CANCELLED';
export type PaymentStatus = 'CONFIRMED' | 'CANCELLED';

export interface FinancialSummary {
  accountsReceivable: number;
  accountsPayable: number;
  overdueReceivable: number;
  overduePayable: number;
  overdueCustomerInvoices: number;
  overdueSupplierInvoices: number;
}

export interface InvoiceBalance {
  documentType: FinancialDocumentType;
  invoiceId: number;
  invoiceReference: string;
  partyId: number | null;
  partyName: string;
  dueDate: string | null;
  grossAmount: number;
  creditedAmount: number;
  netAmount: number;
  paidAmount: number;
  balance: number;
  creditBalance: number;
  status: string;
  overdue: boolean;
}

export interface PaymentRecord {
  id: number;
  reference: string;
  documentType: FinancialDocumentType;
  invoiceId: number;
  invoiceReference: string;
  amount: number;
  paymentDate: string;
  paymentMethod: string;
  status: PaymentStatus;
  notes?: string;
  createdAt: string;
}

export interface EligibleReturnLine {
  orderLineId: number;
  articleId: number;
  articleName: string;
  invoicedQuantity: number;
  alreadyReturned: number;
  returnableQuantity: number;
  unitPriceHT: number;
  taxRate: number;
}

export interface ReturnRecord {
  id: number;
  reference: string;
  returnType: ReturnType;
  status: ReturnStatus;
  returnDate: string;
  reason: string;
  invoiceId: number;
  invoiceReference: string;
  amountHT: number;
  amountTax: number;
  amountTotal: number;
  creditNoteReference?: string;
  lines: Array<{
    id: number;
    orderLineId: number;
    articleId: number;
    articleName: string;
    quantity: number;
    unitPriceHT: number;
    taxRate: number;
    amountHT: number;
    amountTotal: number;
  }>;
  createdAt: string;
}
