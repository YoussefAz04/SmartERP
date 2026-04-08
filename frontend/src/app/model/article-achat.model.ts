import { Fournisseur } from './fournisseur.model';
import { CategorieFournisseur } from './categorieFournisseur.model';

export interface ArticleAchat {
  id?: number;
  reference?: string;
  nom: string;
  description?: string;
  prixUnitaire?: number;
  prixAchat?: number;
  prixVente?: number;
  quantite?: number;
  unite?: string;
  sku?: string;
  actif?: boolean;
  entrepriseId?: number;
  categorieArticleId?: number;
  mesureId?: number;
  createdAt?: Date;
  updatedAt?: Date;
  fournisseur?: Fournisseur;
  categorieFournisseur?: CategorieFournisseur;
}
