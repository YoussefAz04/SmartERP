import { CategorieFournisseur } from './categorieFournisseur.model';
import { Entreprise } from './entreprise.model';

export interface Fournisseur {
  id?: number;
  nom: string;
  email: string;
  telephone: string;
  adresse: string;
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