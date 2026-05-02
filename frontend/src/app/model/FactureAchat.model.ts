export interface DetaisFactureAchat {
  id?: number;
  quantite?: number;
  prixUnitaireHT?: number;
  tva?: number;
  totalTTC?: number;
  article?: string;

  factureAchat?: FactureAchat;
}

export interface LigneFactureAchat {
  id?: number;
  designation?: string;
  quantite?: number;
  prixUnitaireHT?: number;
  tva?: number;
  montantHT?: number;
  montantTTC?: number;
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

  lignes?: LigneFactureAchat[];

  fournisseur?: {
    id?: number;
    nom?: string;
    email?: string;
    telephone?: string;
    adresse?: string;
    ville?: string;
    pays?: string;
    numeroIdentificationFiscale?: string;
    registreCommerce?: string;
  };

  commandeAchat?: {
    id?: number;
    reference?: string;
    statut?: string;
  };
}
