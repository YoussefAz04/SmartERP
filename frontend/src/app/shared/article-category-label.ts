type ArticleCategoryLike =
  | string
  | {
      libelle?: string | null;
      nom?: string | null;
      name?: string | null;
      code?: string | null;
    }
  | null
  | undefined;

const CATEGORY_TRANSLATIONS: Record<string, string> = {
  ali: 'Alimentaire',
  alim: 'Alimentaire',
  alimentaire: 'Alimentaire',
  food: 'Alimentaire',
  foods: 'Alimentaire',

  aut: 'Autres',
  autre: 'Autres',
  autres: 'Autres',
  other: 'Autres',
  others: 'Autres',
  miscellaneous: 'Autres',

  bois: 'Boissons',
  boisson: 'Boissons',
  boissons: 'Boissons',
  beverage: 'Boissons',
  beverages: 'Boissons',
  drinks: 'Boissons',

  bur: 'Bureautique',
  bureau: 'Bureautique',
  bureautique: 'Bureautique',
  office: 'Bureautique',
  'office supplies': 'Bureautique',

  chi: 'Chimie',
  chimie: 'Chimie',
  chemical: 'Chimie',
  chemicals: 'Chimie',
  chemistry: 'Chimie',

  cons: 'Consommables',
  consommable: 'Consommables',
  consommables: 'Consommables',
  consumable: 'Consommables',
  consumables: 'Consommables',

  const: 'Construction',
  construction: 'Construction',
  building: 'Construction',
  'building materials': 'Construction',

  ele: 'Électronique',
  elec: 'Électronique',
  electronique: 'Électronique',
  electronics: 'Électronique',
  electronic: 'Électronique',

  emb: 'Emballage',
  emballage: 'Emballage',
  packaging: 'Emballage',

  hyg: 'Hygiène',
  hygiene: 'Hygiène',
  hygienic: 'Hygiène',

  inf: 'Informatique',
  info: 'Informatique',
  informatique: 'Informatique',
  computer: 'Informatique',
  computers: 'Informatique',
  'computer equipment': 'Informatique',
  it: 'Informatique',
  hardware: 'Informatique',

  mec: 'Mécanique',
  mecanique: 'Mécanique',
  mechanical: 'Mécanique',
  mechanics: 'Mécanique',

  men: 'Ménager',
  menager: 'Ménager',
  household: 'Ménager',

  outi: 'Outillage',
  outillage: 'Outillage',
  tools: 'Outillage',
  tooling: 'Outillage',

  ser: 'Services',
  service: 'Services',
  services: 'Services',

  tex: 'Textile',
  textile: 'Textile',
  textiles: 'Textile',
  clothing: 'Textile'
};

export function getArticleCategoryLabel(category: ArticleCategoryLike): string {
  const rawLabel = getRawCategoryLabel(category);
  const key = normalizeCategoryKey(rawLabel);

  return CATEGORY_TRANSLATIONS[key] || rawLabel;
}

function getRawCategoryLabel(category: ArticleCategoryLike): string {
  if (!category) {
    return '';
  }

  if (typeof category === 'string') {
    return category;
  }

  return category.libelle || category.nom || category.name || category.code || '';
}

function normalizeCategoryKey(value: string): string {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[._-]+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .toLowerCase();
}
