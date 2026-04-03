import { Fournisseur } from './fournisseur.model';
import { CategorieFournisseur } from './categorieFournisseur.model';

export interface ArticleAchat {
  id?: number;
  nom: string;
  description?: string;
  prixUnitaire?: number;
  prixAchat?: number;
  quantite?: number;
  unite?: string;
  sku?: string;
  createdAt?: Date;
  updatedAt?: Date;
  fournisseur?: Fournisseur;
  categorieFournisseur?: CategorieFournisseur;
}
