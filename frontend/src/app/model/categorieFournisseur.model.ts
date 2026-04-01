import { Fournisseur } from './fournisseur.model';

export interface CategorieFournisseur {
  id?: number;
  nom: string;
  description: string;
  createdAt?: Date;
  updatedAt?: Date;
  fournisseurs?: Fournisseur[];
}
