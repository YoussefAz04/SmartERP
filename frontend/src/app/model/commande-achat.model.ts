import { Fournisseur } from './fournisseur.model';
import { ArticleAchat } from './article-achat.model';

export interface LigneCommandeAchat {
  id?: number;
  quantite?: number;
  prixUnitaireHT?: number;
  tva?: number;
  montantHT?: number;
  montantTTC?: number;
  articleAchat?: ArticleAchat;
  articleAchatId?: number;
}

export interface CommandeAchat {
  id?: number;
  reference?: string;
  dateCommande?: string;
  dateLivraisonPrevue?: string;
  statut?: string;
  montantHT?: number;
  montantTVA?: number;
  montantTTC?: number;
  note?: string;
  fournisseur?: Fournisseur;
  fournisseurId?: number;
  entrepriseId?: number;
  lignes?: LigneCommandeAchat[];
  createdAt?: Date;
  updatedAt?: Date;
}
