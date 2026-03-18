import {Depense} from "./depense.model";

export interface Budget {
  id?: number;
  nom: string;
  montant: number;
  dateDebut: string;
  dateFin: string;
  idProjet: number;
  depenses?: Depense[];
}
