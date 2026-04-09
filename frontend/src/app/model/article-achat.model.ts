export interface ArticleAchatCategorie {
  id?: number;
  nom?: string;
}

export interface ArticleAchatFournisseur {
  id?: number;
  nom?: string;
}

export interface ArticleAchat {
  id?: number;
  reference?: string;
  nom: string;
  description?: string;
  prixUnitaire?: number;
  prixAchat?: number;
  quantite?: number;
  unite?: string;
  sku?: string;
  actif?: boolean;
  entrepriseId?: number;
  categorieArticleId?: number;
  mesureId?: number;
  fournisseurId?: number;
  createdAt?: Date;
  updatedAt?: Date;
  fournisseur?: ArticleAchatFournisseur;
  categorieArticle?: ArticleAchatCategorie;
}
