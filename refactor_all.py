#!/usr/bin/env python3
"""
Comprehensive refactoring script to replace all alert() and confirm() calls
with NotificationService across the Angular frontend.
"""
import os
import re
from pathlib import Path

BASE_DIR = Path("/home/runner/work/pfe-project/pfe-project/frontend/src/app")

# Files already processed (skip these)
PROCESSED_FILES = {
    "core-component/commande-achat/list-commande-achat/list-commande-achat.component.ts",
    "core-component/commande-achat/add-commande-achat/add-commande-achat.component.ts"
}

# Map files to their NotificationService import paths
FILE_IMPORTS = {
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

def add_import(content, import_path):
    """Add NotificationService import if not already present."""
    if "NotificationService" in content:
        return content
    
    # Find the last import statement
    import_pattern = r"(import .*?;\n)(?!import)"
    matches = list(re.finditer(import_pattern, content, re.DOTALL))
    
    if matches:
        last_import = matches[-1]
        insert_pos = last_import.end()
        new_import = f"import {{ NotificationService }} from '{import_path}';\n"
        return content[:insert_pos] + new_import + content[insert_pos:]
    
    return content

def inject_service(content):
    """Inject NotificationService in constructor if not already present."""
    if "private notification: NotificationService" in content:
        return content
    
    # Find constructor
    constructor_match = re.search(r'constructor\s*\((.*?)\)\s*\{', content, re.DOTALL)
    if not constructor_match:
        return content
    
    params = constructor_match.group(1)
    # Add notification service as last parameter
    if params.strip():
        new_params = params.rstrip() + ',\n    private notification: NotificationService'
    else:
        new_params = 'private notification: NotificationService'
    
    new_constructor = f'constructor({new_params}) {{'
    return content.replace(constructor_match.group(0), new_constructor)

def determine_notification_type(message):
    """Determine which notification method to use based on message content."""
    msg_lower = message.lower()
    
    # Check for explicit symbols
    if any(symbol in message for symbol in ['✓', '✔', '✅']):
        return 'success'
    if any(symbol in message for symbol in ['✗', '✘', '❌']):
        return 'error'
    if '⚠' in message:
        return 'warning'
    
    # Check for error keywords
    if any(word in msg_lower for word in ['erreur', 'error', 'échec', 'impossible', 'invalide', 'missing']):
        return 'error'
    
    # Check for info keywords
    if any(phrase in msg_lower for phrase in ['en cours', 'opération en cours']):
        return 'info'
    
    # Check for warning keywords
    if any(word in msg_lower for word in ['veuillez', 'please', 'remplir']) and 'erreur' not in msg_lower:
        return 'warning'
    
    # Check for success keywords
    if any(phrase in msg_lower for phrase in ['avec succès', 'supprimée', 'supprimé', 'ajoutée', 'ajouté', 'mis à jour', 'créée', 'créé', 'envoyée', 'envoyé']):
        return 'success'
    
    # Default to show() for auto-detection
    return 'show'

def replace_alerts(content):
    """Replace all alert() calls with appropriate notification calls."""
    # Pattern to match alert calls (not in comments)
    pattern = r"(?<!//\s{0,10})alert\s*\(\s*(['\"])((?:(?!\1).)*)\1\s*\)"
    
    def replacer(match):
        message = match.group(2)
        method = determine_notification_type(message)
        return f"this.notification.{method}('{message}')"
    
    return re.sub(pattern, replacer, content)

def replace_confirms(content):
    """Replace confirm() calls with notification.confirm() returning Promise."""
    
    # Pattern A: if (!confirm(...)) return; ...rest
    # Replace with: this.notification.confirm(...).then(ok => { if (!ok) return; ...rest });
    pattern_a = r'if\s*\(\s*!confirm\s*\(\s*(["\'])((?:(?!\1).)*)\1\s*\)\s*\)\s*return\s*;'
    
    def replacer_a(match):
        message = match.group(2)
        return f"this.notification.confirm('{message}').then(ok => {{ if (!ok) return;"
    
    # First pass: replace pattern A
    content = re.sub(pattern_a, replacer_a, content)
    
    # Pattern B: if (confirm(...)) { body }
    # This is more complex and needs manual handling in some cases
    # For now, we'll handle simple cases
    
    # Pattern C: const confirmed = confirm(...); if (!confirmed) return;
    # Replace with: this.notification.confirm(...).then(confirmed => { if (!confirmed) return;
    
    return content

# Process files
processed_count = 0
skipped_count = 0
error_count = 0

for rel_path, import_path in FILE_IMPORTS.items():
    if rel_path in PROCESSED_FILES:
        print(f"SKIP (already processed): {rel_path}")
        skipped_count += 1
        continue
    
    file_path = BASE_DIR / rel_path
    
    if not file_path.exists():
        print(f"ERROR (not found): {rel_path}")
        error_count += 1
        continue
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # Apply transformations
        content = add_import(content, import_path)
        content = inject_service(content)
        content = replace_alerts(content)
        content = replace_confirms(content)
        
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✓ PROCESSED: {rel_path}")
            processed_count += 1
        else:
            print(f"  NO CHANGES: {rel_path}")
            skipped_count += 1
            
    except Exception as e:
        print(f"ERROR processing {rel_path}: {e}")
        error_count += 1

print(f"\n=== SUMMARY ===")
print(f"Processed: {processed_count}")
print(f"Skipped: {skipped_count}")
print(f"Errors: {error_count}")
print(f"Total: {processed_count + skipped_count + error_count}")
