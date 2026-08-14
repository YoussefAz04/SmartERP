export type SavStatus =
  | 'OUVERT'
  | 'QUALIFICATION'
  | 'EN_ATTENTE_PRODUIT'
  | 'DIAGNOSTIC'
  | 'EN_ATTENTE_ACCORD'
  | 'EN_REPARATION'
  | 'PRET_RESTITUTION'
  | 'EN_COURS'
  | 'EN_ATTENTE_CLIENT'
  | 'RESOLU'
  | 'FERME'
  | 'ANNULE';

export type SavPriority = 'BASSE' | 'NORMALE' | 'HAUTE' | 'URGENTE';

export type SavType = 'GARANTIE' | 'REPARATION' | 'RETOUR' | 'ECHANGE' | 'SUPPORT' | 'AUTRE';

export type SavApprovalStatus = 'NON_REQUIS' | 'EN_ATTENTE' | 'ACCEPTE' | 'REFUSE';

export type SavResolutionType = 'REPARATION' | 'ECHANGE' | 'REMBOURSEMENT' | 'AVOIR' | 'RETOUR_SANS_ACTION' | 'REFUS_GARANTIE' | 'ASSISTANCE_RESOLUE' | 'AUCUN_DEFAUT';

export type SavHistoryType = 'CREATED' | 'UPDATED' | 'STATUS_CHANGED' | 'ASSIGNED' | 'NOTE' | 'SALE_VERIFIED' | 'APPROVAL' | 'INTERVENTION';

export interface SavTicket {
  id?: number;
  reference?: string;
  subject: string;
  description?: string;
  diagnostic?: string;
  resolution?: string;
  status: SavStatus;
  priority: SavPriority;
  type: SavType;
  estimatedCost?: number;
  actualCost?: number;
  serialNumber?: string;
  purchaseDate?: string;
  warrantyMonths?: number;
  warrantyEndDate?: string;
  underWarranty?: boolean;
  overdue?: boolean;
  contactChannel?: string;
  approvalStatus?: SavApprovalStatus;
  resolutionType?: SavResolutionType;
  customerApprovedAt?: string;
  productReceivedAt?: string;
  productReturnedAt?: string;
  dueDate?: string;
  closedAt?: string;
  clientId?: number;
  clientName?: string;
  articleVenteId?: number;
  articleVenteName?: string;
  commandeVenteId?: number;
  commandeVenteReference?: string;
  saleVerified?: boolean;
  createdByUserId?: number;
  createdByUsername?: string;
  assignedToUserId?: number;
  assignedToUsername?: string;
  entrepriseId?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface SavTicketRequest {
  subject: string;
  description?: string;
  diagnostic?: string;
  resolution?: string;
  status?: SavStatus;
  priority?: SavPriority;
  type?: SavType;
  estimatedCost?: number;
  actualCost?: number;
  serialNumber?: string;
  purchaseDate?: string;
  warrantyMonths?: number;
  contactChannel?: string;
  approvalStatus?: SavApprovalStatus;
  resolutionType?: SavResolutionType;
  dueDate?: string;
  clientId?: number | null;
  articleVenteId?: number | null;
  commandeVenteId?: number | null;
  assignedToUserId?: number | null;
}

export interface SavStats {
  total: number;
  open: number;
  inProgress: number;
  waitingCustomer: number;
  resolved: number;
  closed: number;
  cancelled: number;
  overdue: number;
  unassigned: number;
  actualCostTotal: number;
}

export interface SavAgent {
  id: number;
  username: string;
  email: string;
}

export interface SavHistory {
  id: number;
  type: SavHistoryType;
  message: string;
  createdByUserId?: number;
  createdByUsername?: string;
  createdAt: string;
}

export interface SavSaleItem {
  orderId: number;
  orderReference: string;
  orderDate: string;
  orderStatus: string;
  articleVenteId: number;
  articleName: string;
  sku?: string;
  quantity: number;
}

export interface SavInterventionRequest {
  description: string;
  durationMinutes: number;
  laborCost?: number;
  partsCost?: number;
  technicianUserId?: number | null;
}

export interface SavIntervention extends SavInterventionRequest {
  id: number;
  totalCost: number;
  technicianUsername?: string;
  performedAt: string;
}
