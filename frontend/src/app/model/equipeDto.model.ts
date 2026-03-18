import { Membre } from './membre.model';

export interface EquipeDto {
  id: number;
  nom: string;
  description: string;
  dateCreation: string;
  membres: Membre[];
}
