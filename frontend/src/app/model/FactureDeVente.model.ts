import {Client} from "./client.model";

export interface DetaisFactureVente {
  id?: number;
  quantite?: number;
  prixUnitaireHT?: number;
  tva?: number;
  totalTTC?: number;
  article?: string;

  factureAchat?: FactureVente;
}

export interface LigneCommandeVente {
  id?: number;
  quantite?: number;
  prixUnitaireHT?: number;
  tva?: number;
  montantHT?: number;
  montantTTC?: number;
  articleVente?: {
    id?: number;
    nom?: string;
  };
}




export interface FactureVente {
  id?: number;
  dateAchat?: string;
  referance?: string;
  montantTotal?: number;
  montantHT?: number;
  montantTVA?: number;
  montantTTC?: number;
  remise?: number;
  modePaiement?: string;
  statut?: string;
  description?: string;
  cheminFichier?: string;
  detaisFactureDeVente?: DetaisFactureVente;

  client ?: Client ;

  commandeVente?: {
    id?: number;
    reference?: string;
    statut?: string;
    lignes?: LigneCommandeVente[];
  };
}
