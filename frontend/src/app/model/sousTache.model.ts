import {Tache} from "./tache.model";
export enum TypeSousTache {
  TECHNIQUE='TECHNIQUE',
  DOCUMENTATION='DOCUMENTATION',
  TEST='TEST',
  REVUE='REVUE'
}
export interface SousTache {
  id?: number;
  titre: string;
  description?: string;
  type: TypeSousTache;
  tache?: Tache;
  dateDebut : Date ;
  dateFin : Date ;
  progres : number ;
  duree : number ;
}
