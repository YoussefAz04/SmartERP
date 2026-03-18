export interface DepartementDTO {
  id?: number;
  nom: string;
  description: string;
  responsable: string;
  email: string;
  telephone: string;
  nombreEmployes: number;
  entrepriseId?: number;
  entrepriseNom?: string;
}
