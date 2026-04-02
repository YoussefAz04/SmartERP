import { Stock } from './stock.model';

export type TypeMouvement = 'ENTREE' | 'SORTIE' | 'AJUSTEMENT' | 'RESERVATION';

export interface StockMouvement {
  id?: number;
  stock?: Stock;
  stockId?: number;
  typeMouvement: TypeMouvement;
  quantite: number;
  referenceCommande?: string;
  motif?: string;
  utilisateur?: { id: number; username?: string; email?: string };
  utilisateurId?: number;
  dateMouvement?: Date | string;
  createdAt?: Date | string;
  updatedAt?: Date | string;
}

export interface StockMouvementDto {
  stockId: number;
  typeMouvement: TypeMouvement;
  quantite: number;
  referenceCommande?: string;
  motif?: string;
  utilisateurId?: number;
  dateMouvement?: Date | string;
}
