import { Budget } from "./budget.model";
export interface CategorieDepense {
  id?: number;
  nom: string;
  description?: string;
}

export interface Depense {
  id?: number;
  description: string;
  montant: number;
  dateDepense: string;
  budget?: Budget;
  categorieDepense?: CategorieDepense;
}
