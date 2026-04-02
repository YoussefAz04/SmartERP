import { Client } from './client.model';
import { ArticleVente } from './article-vente.model';

export interface LigneCommandeVente {
  id?: number;
  quantite?: number;
  prixUnitaireHT?: number;
  tva?: number;
  montantHT?: number;
  montantTTC?: number;
  articleVente?: ArticleVente;
  articleVenteId?: number;
}

export interface CommandeVente {
  id?: number;
  reference?: string;
  dateCommande?: string;
  dateLivraisonPrevue?: string;
  statut?: string;
  montantHT?: number;
  montantTVA?: number;
  montantTTC?: number;
  note?: string;
  client?: Client;
  clientId?: number;
  entrepriseId?: number;
  lignes?: LigneCommandeVente[];
  createdAt?: Date;
  updatedAt?: Date;
}
