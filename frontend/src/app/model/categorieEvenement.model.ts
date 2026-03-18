export enum ColorCategorie {
  ROUGE = 'ROUGE',
  BLEU = 'BLEU',
  VERT = 'VERT',
  JAUNE = 'JAUNE'
}

export interface CategorieEvenement {
  id?: number;
  titre: string;
  colorCategorie: ColorCategorie;
}
