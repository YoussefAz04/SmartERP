import { ArticleAchat } from './article-achat.model';

export interface Stock {
  id?: number;
  quantiteEntree?: number;
  quantiteDisponible?: number;
  quantiteReserve?: number;
  quantiteVendue?: number;
  localisation?: string;
  dateDernierInventaire?: Date | string;
  createdAt?: Date | string;
  updatedAt?: Date | string;
  articleAchat?: ArticleAchat;
  articleAchatId?: number;
  articleVenteId?: number;
}

export interface StockDto {
  quantiteEntree?: number;
  quantiteDisponible?: number;
  quantiteReserve?: number;
  quantiteVendue?: number;
  localisation?: string;
  dateDernierInventaire?: Date | string;
  articleAchatId?: number;
  articleVenteId?: number;
}
