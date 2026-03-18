
export interface UserDTO {
  id: number;
  username: string;
  phone: string;
  email: string;
  enabled: boolean;
  roles?: string[];
  entrepriseId?: number;
  entrepriseNom?: string;
}
