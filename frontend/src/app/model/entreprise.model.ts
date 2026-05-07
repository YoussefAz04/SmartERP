import {CaracteristiqueFactureEntreprise} from "./caracteristiqueFactureEntreprise .model";

export interface Entreprise {
  id?: number;
  nom: string;
  raisonSociale: string;
  description: string;
  secteurActivite: string;
  statutJuridique: string;
  numeroIdentificationFiscale: string;
  registreCommerce: string;
  siteWeb: string;
  dateCreation: string;
  dateDerniereModification: string;
  telephone : string ;
  adresse : string ;
  email : string ;
  active?: boolean;
  caracteristiqueFactureEntreprise?: CaracteristiqueFactureEntreprise ;
}
