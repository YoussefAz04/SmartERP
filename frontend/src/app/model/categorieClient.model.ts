import { Client } from './client.model';

export interface CategorieClient {
  id?: number;
  nom: string;
  description: string;
  clients?: Client[];
}
