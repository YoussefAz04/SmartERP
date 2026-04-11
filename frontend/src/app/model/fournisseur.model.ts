import { CategorieFournisseur } from './categorieFournisseur.model';
import { Entreprise } from './entreprise.model';

export interface Fournisseur {
  id?: number;
  nom: string;
  prenom?: string;
  email: string;
  telephone: string;
  adresse: string;
  secteurActivite?: string;
  tauxLivraison?: number;
  notation?: number;
  ville: string;
  pays: string;
  codePostal: string;
  numeroIdentificationFiscale: string;
  registreCommerce: string;
  createdAt?: Date;
  updatedAt?: Date;
  categorieFournisseur?: CategorieFournisseur;
  entreprise?: Entreprise;
}