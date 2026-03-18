import { Entreprise } from './entreprise.model';
import {CategorieClient} from "./categorieClient.model";

export interface Client {
  id?: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  adresse: string;
  secteurActivite: string;
  categorieClient?: CategorieClient;
  entreprises?: Entreprise[];
}
