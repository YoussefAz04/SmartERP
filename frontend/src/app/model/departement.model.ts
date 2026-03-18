import {Entreprise} from "./entreprise.model";

export interface Departement {
  id?: number;
  nom: string;
  description: string;
  responsable: string;
  email: string;
  telephone: string;
  nombreEmployes: number;
  entreprise?: Entreprise;
}
