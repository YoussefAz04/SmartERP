#!/usr/bin/env python3
import os
import re

# Map of files to their relative paths to notification.service.ts
notification_imports = {
    "core-component/article-achat/article-achat.component.ts": "../../shared/notification/notification.service",
    "core-component/article-achat/edit-article-achat/edit-article-achat.component.ts": "../../../shared/notification/notification.service",
    "core-component/user-management/detais-users/detais-users.component.ts": "../../../shared/notification/notification.service",
    "core-component/user-management/users/users.component.ts": "../../../shared/notification/notification.service",
    "core-component/user-management/users/assign-role-dialog/assign-role-dialog.component.ts": "../../../../shared/notification/notification.service",
    "core-component/user-management/add-user/add-user.component.ts": "../../../shared/notification/notification.service",
    "core-component/documentation/documentation.component.ts": "../../shared/notification/notification.service",
    "core-component/documentation/pdf-zone-selector/pdf-zone-selector.component.ts": "../../../shared/notification/notification.service",
    "core-component/main/application/calendar/calendar.component.ts": "../../../../shared/notification/notification.service",
    "core-component/fournisseur/edit-categorie-fournisseur/edit-categorie-fournisseur.component.ts": "../../../shared/notification/notification.service",
    "core-component/fournisseur/add-categorie-fournisseur/add-categorie-fournisseur.component.ts": "../../../shared/notification/notification.service",
    "core-component/fournisseur/add-fournisseur/add-fournisseur.component.ts": "../../../shared/notification/notification.service",
    "core-component/fournisseur/edit-fournisseur/edit-fournisseur.component.ts": "../../../shared/notification/notification.service",
    "core-component/fournisseur/fournisseur.component.ts": "../../shared/notification/notification.service",
    "core-component/fournisseur/list-categorie-fournisseur/list-categorie-fournisseur.component.ts": "../../../shared/notification/notification.service",
    "core-component/client/add-client/add-client.component.ts": "../../../shared/notification/notification.service",
    "core-component/client/add-categorie-client/add-categorie-client.component.ts": "../../../shared/notification/notification.service",
    "core-component/client/edit-client/edit-client.component.ts": "../../../shared/notification/notification.service",
    "core-component/client/list-categorie-client/list-categorie-client.component.ts": "../../../shared/notification/notification.service",
    "core-component/client/client.component.ts": "../../shared/notification/notification.service",
    "core/service/factureDeVente/facture-de-vente-service.service.ts": "../../../shared/notification/notification.service",
    "core-component/commande-vente/list-commande-vente/list-commande-vente.component.ts": "../../../shared/notification/notification.service",
    "core-component/commande-vente/add-commande-vente/add-commande-vente.component.ts": "../../../shared/notification/notification.service",
    "core-component/entreprise/entreprise.component.ts": "../../shared/notification/notification.service",
    "core-component/entreprise/add-entreprise/add-entreprise.component.ts": "../../../shared/notification/notification.service",
    "core-component/article-vente/article-vente.component.ts": "../../shared/notification/notification.service",
    "core-component/article-vente/edit-article-vente/edit-article-vente.component.ts": "../../../shared/notification/notification.service",
    "core-component/budget/budget.component.ts": "../../shared/notification/notification.service",
    "core-component/budget/depense/depense.component.ts": "../../../shared/notification/notification.service"
}

print(f"Will process {len(notification_imports)} files")
for file in notification_imports:
    print(f"- {file}")
