import { Client } from './client.model';
import { ArticleVente } from './article-vente.model';
import { FactureVente } from './FactureDeVente.model';

export type SavType =
  | 'GARANTIE'
  | 'REPARATION'
  | 'RETOUR'
  | 'ECHANGE'
  | 'RECLAMATION';

export type SavStatut =
  | 'NOUVEAU'
  | 'EN_COURS'
  | 'EN_ATTENTE_PIECE'
  | 'RESOLU'
  | 'CLOTURE'
  | 'ANNULE';

export type SavPriorite = 'BASSE' | 'NORMALE' | 'HAUTE' | 'URGENTE';

export interface SavIntervention {
  id?: number;
  type?: string;
  statut?: string;
  commentaire?: string;
  auteur?: string;
  createdAt?: string;
  savId?: number;
}

export interface Sav {
  id?: number;
  reference?: string;
  dateOuverture?: string;
  dateCloture?: string;
  type?: SavType | string;
  priorite?: SavPriorite | string;
  statut?: SavStatut | string;
  sujet?: string;
  description?: string;
  resolution?: string;
  coutEstime?: number;
  coutReel?: number;

  client?: Client;
  clientId?: number;
  entrepriseId?: number;
  factureDeVente?: FactureVente;
  factureDeVenteId?: number;
  articleVente?: ArticleVente;
  articleVenteId?: number;
  technicien?: { id?: number; username?: string; email?: string };
  technicienId?: number;

  interventions?: SavIntervention[];

  createdAt?: string;
  updatedAt?: string;
}
