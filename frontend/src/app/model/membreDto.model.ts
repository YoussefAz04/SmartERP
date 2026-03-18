import {UserRequest} from "./user-request.model";


export interface MembreDto {
  id: number;
  membreTitre: string;
  description: string;
  specialite: string;
  users: UserRequest[];
  equipeId: number;
}
