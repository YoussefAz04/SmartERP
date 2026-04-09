import { ArticleAchat } from './article-achat.model';
import { Client } from './client.model';
import { Stock } from './stock.model';

export interface ArticleVente {
  id?: number;
  nom: string;
  description?: string;
  prixUnitaire?: number;
  quantite?: number;
  unite?: string;
  sku?: string;
  markup?: number;
  createdAt?: Date;
  updatedAt?: Date;
  articleAchat?: ArticleAchat;
  client?: Client;
  stock?: Stock;
}
