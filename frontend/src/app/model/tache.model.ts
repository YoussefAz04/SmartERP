import { Projet } from './projet.model';
import {CategorieTache} from "./categorieTache.model";
import {SousTache} from "./sousTache.model";



export  enum  PrioriteDeTache {
  FAIBLE = 'FAIBLE',
  MOYENNE = 'MOYENNE',
  ELEVEE = 'ELEVEE',
  URGENTE = 'URGENTE'
}
export enum StatutTache {
  A_FAIRE ='A_FAIRE',
  EN_COURS='EN_COURS',
  TERMINEE='TERMINEE',
  BLOQUEE ='BLOQUEE'
}
export interface Tache {
  id?: number;
  titre: string;
  description?: string;
  dateDebut: Date;
  dateFin: Date;
  statut : StatutTache ;
  priorite : PrioriteDeTache ;
  projet?: Projet;
  categorieTache?: CategorieTache ;
  sousTaches ?: SousTache[] ;
  duree: number ;
  progres : number ;
  predecesseur:string;
}
