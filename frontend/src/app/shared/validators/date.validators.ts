import { AbstractControl, ValidationErrors } from '@angular/forms';

export const DATE_LIVRAISON_ERROR_MESSAGE =
  'La date de livraison doit être postérieure ou égale à la date de commande';

/**
 * Form-group validator: ensures dateLivraisonPrevue is not before dateCommande.
 * Returns { dateLivraisonInvalid: true } when the rule is violated.
 */
export function dateLivraisonValidator(group: AbstractControl): ValidationErrors | null {
  const dateCommande = group.get('dateCommande')?.value;
  const dateLivraison = group.get('dateLivraisonPrevue')?.value;
  if (dateCommande && dateLivraison && dateLivraison < dateCommande) {
    return { dateLivraisonInvalid: true };
  }
  return null;
}
