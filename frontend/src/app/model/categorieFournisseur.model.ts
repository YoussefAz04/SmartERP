import { Fournisseur } from './fournisseur.model';

export interface CategorieFournisseur {
  id?: number;
  nom: string;
  description: string;
  fournisseurs?: Fournisseur[];
}
