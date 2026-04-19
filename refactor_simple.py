#!/usr/bin/env python3
import os
import re
from pathlib import Path

BASE_DIR = Path("/home/runner/work/pfe-project/pfe-project/frontend/src/app")

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
    lines = content.split('\n')
    last_import_idx = -1
    for i, line in enumerate(lines):
        if line.strip().startswith('import ') and line.strip().endswith(';'):
            last_import_idx = i
    
    if last_import_idx >= 0:
        new_import = f"import {{ NotificationService }} from '{import_path}';"
        lines.insert(last_import_idx + 1, new_import)
        return '\n'.join(lines)
    
    return content

def inject_service(content):
    """Inject NotificationService in constructor if not already present."""
    if "notification: NotificationService" in content:
        return content
    
    # Find constructor
    constructor_match = re.search(r'constructor\s*\((.*?)\)\s*\{', content, re.DOTALL)
    if not constructor_match:
        return content
    
    params = constructor_match.group(1).strip()
    # Add notification service as last parameter
    if params:
        new_params = params.rstrip(',') + ',\n    private notification: NotificationService'
    else:
        new_params = 'private notification: NotificationService'
    
    new_constructor = f'constructor({new_params}) {{'
    return content.replace(constructor_match.group(0), new_constructor, 1)

def determine_notification_type(message):
    """Determine notification method based on message content."""
    msg = message
    msg_lower = message.lower()
    
    # Check for explicit symbols
    if any(symbol in msg for symbol in ['✓', '✔', '✅']):
        return 'success'
    if any(symbol in msg for symbol in ['✗', '✘', '❌']):
        return 'error'
    if '⚠' in msg:
        return 'warning'
    
    # Check for error keywords
    if any(word in msg_lower for word in ['erreur', 'error', 'échec', 'impossible', 'invalide', 'missing']):
        return 'error'
    
    # Check for info keywords
    if any(phrase in msg_lower for phrase in ['en cours', 'opération en cours']):
        return 'info'
    
    # Check for warning keywords
    if any(word in msg_lower for word in ['veuillez', 'please', 'remplir']) and 'erreur' not in msg_lower and 'error' not in msg_lower:
        return 'warning'
    
    # Check for success keywords
    if any(phrase in msg_lower for word in ['avec succès', 'succ', 'supprimée', 'supprimé', 'ajoutée', 'ajouté', 'mis à jour', 'créée', 'créé', 'envoyée', 'envoyé']):
        return 'success'
    
    return 'show'

def replace_simple_alerts(content):
    """Replace simple alert() calls."""
    # Match alert('...' or alert("...")
    lines = content.split('\n')
    new_lines = []
    
    for line in lines:
        # Skip commented lines
        if line.strip().startswith('//'):
            new_lines.append(line)
            continue
        
        # Simple pattern matching for alert
        if 'alert(' in line and not 'sweetalert' in line.lower():
            # Extract the message
            match = re.search(r"alert\s*\(\s*(['\"])(.*?)\1\s*\)", line)
            if match:
                quote_char = match.group(1)
                message = match.group(2)
                method = determine_notification_type(message)
                replacement = f"this.notification.{method}({quote_char}{message}{quote_char})"
                line = line.replace(match.group(0), replacement)
        
        new_lines.append(line)
    
    return '\n'.join(new_lines)

# Process files
processed_count = 0

for rel_path, import_path in FILE_IMPORTS.items():
    file_path = BASE_DIR / rel_path
    
    if not file_path.exists():
        print(f"NOT FOUND: {rel_path}")
        continue
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # Step 1: Add import
        content = add_import(content, import_path)
        
        # Step 2: Inject service
        content = inject_service(content)
        
        # Step 3: Replace alerts (simple cases)
        content = replace_simple_alerts(content)
        
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✓ {rel_path}")
            processed_count += 1
        else:
            print(f"  (no changes) {rel_path}")
            
    except Exception as e:
        print(f"ERROR {rel_path}: {e}")

print(f"\nProcessed {processed_count} files")
