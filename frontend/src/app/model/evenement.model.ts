import {CategorieEvenement} from "./categorieEvenement.model";

export interface Evenement {
  id?: number;
  title: string;
  start: string; // format ISO string
  end: string;
  allDay: boolean;
  categorieEvenement?: CategorieEvenement
}
