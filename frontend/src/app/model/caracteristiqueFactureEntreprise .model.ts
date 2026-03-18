import {Entreprise} from "./entreprise.model";

export interface CaracteristiqueFactureEntreprise {
  id?: number;

  fontName?: string;
  fontStyle?: string;

  fillColorR?: number;
  fillColorG?: number;
  fillColorB?: number;

  textColorR?: number;
  textColorG?: number;
  textColorB?: number;

  colorHeaderR?: number;
  colorHeaderG?: number;
  colorHeaderB?: number;

  colorFooterR?: number;
  colorFooterG?: number;
  colorFooterB?: number;

  messageFooter?: string;

  entreprise?: Entreprise;
}
