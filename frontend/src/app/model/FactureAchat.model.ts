export interface DetaisFactureAchat {
  id?: number;
  quantite?: number;
  prixUnitaireHT?: number;
  tva?: number;
  totalTTC?: number;
  article?: string;

  factureAchat?: FactureAchat;
}




export interface FactureAchat {
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

  detaisFactureAchat?: DetaisFactureAchat;

  fournisseur?: {
    id?: number;
    nom?: string;
  };

  commandeAchat?: {
    id?: number;
    reference?: string;
    statut?: string;
  };
}
