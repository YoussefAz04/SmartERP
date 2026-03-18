import { Equipe } from './equipe.model';
import {UserRequest} from "./user-request.model";

export interface Membre {
  id?: number;
  membreTitre: string;
  description: string;
  specialite: string;
  users?: UserRequest[];
  equipe?: Equipe;
}
