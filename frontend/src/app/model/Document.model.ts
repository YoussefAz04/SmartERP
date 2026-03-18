import {CategorieDocument} from "./CategorieDocument.model";


export interface FileChemin {
  id: number;
  urLPdf: string ;
}
export interface Document {
  id?: number;
  nom: string;
  type: string;
  dateAjout?: string;
  categorieDocument?: CategorieDocument;
  fileChemin?: FileChemin;
}
