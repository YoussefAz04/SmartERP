import {Entreprise} from "./entreprise.model";
import {Role} from "./role.model";

export interface UserRequest {
  id?: number;
  email?: string;
  phone?: number;
  username: string;
  password: string;
  roles?: Role[];
  enabled?: boolean;
  entreprise?: Entreprise;
}
