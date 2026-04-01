import { CategorieFournisseur } from './categorieFournisseur.model';

export interface Fournisseur {
  id?: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  adresse: string;
  categorieFournisseur?: CategorieFournisseur;
}
