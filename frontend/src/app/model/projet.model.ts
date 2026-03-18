import {CategorieProjet} from "./CategorieProjet.model";
import {Tache} from "./tache.model";
import {Equipe} from "./equipe.model";

export enum StatutProjet {
  EN_ATTENTE = 'EN_ATTENTE',
  EN_COURS = 'EN_COURS',
  TERMINE = 'TERMINE',
  ANNULE = 'ANNULE'
}
export interface Projet {
  id?: number;
  nom: string;
  description: string;
  dateDebut: Date;
  dateFin: Date;
  statut: StatutProjet;
  categorieProjet?: CategorieProjet;
  taches?: Tache[];
  idEquipe?: Equipe ;
}
