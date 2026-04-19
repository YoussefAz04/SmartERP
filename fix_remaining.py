#!/usr/bin/env python3
import re
from pathlib import Path

BASE_DIR = Path("/home/runner/work/pfe-project/pfe-project/frontend/src/app")

REMAINING_FILES = {
    "core-component/documentation/documentation.component.ts": "../../shared/notification/notification.service",
    "core-component/documentation/pdf-zone-selector/pdf-zone-selector.component.ts": "../../../shared/notification/notification.service",
    "core/service/factureDeVente/facture-de-vente-service.service.ts": "../../../shared/notification/notification.service",
    "core-component/entreprise/entreprise.component.ts": "../../shared/notification/notification.service",
    "core-component/entreprise/add-entreprise/add-entreprise.component.ts": "../../../shared/notification/notification.service"
}

def add_import(content, import_path):
    if "NotificationService" in content:
        return content
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
    if "notification: NotificationService" in content:
        return content
    constructor_match = re.search(r'constructor\s*\((.*?)\)\s*\{', content, re.DOTALL)
    if not constructor_match:
        return content
    params = constructor_match.group(1).strip()
    if params:
        new_params = params.rstrip(',') + ',\n    private notification: NotificationService'
    else:
        new_params = 'private notification: NotificationService'
    new_constructor = f'constructor({new_params}) {{'
    return content.replace(constructor_match.group(0), new_constructor, 1)

def determine_notification_type(message):
    msg_lower = message.lower()
    if any(s in message for s in ['✓', '✔', '✅']):
        return 'success'
    if any(s in message for s in ['✗', '✘', '❌']):
        return 'error'
    if '⚠' in message:
        return 'warning'
    if any(w in msg_lower for w in ['erreur', 'error', 'échec', 'impossible', 'invalide', 'missing']):
        return 'error'
    if 'en cours' in msg_lower or 'opération en cours' in msg_lower:
        return 'info'
    if any(w in msg_lower for w in ['veuillez', 'please', 'remplir']) and 'erreur' not in msg_lower:
        return 'warning'
    if any(w in msg_lower for w in ['succès', 'succ', 'supprimée', 'supprimé', 'ajoutée', 'ajouté', 'mis à jour', 'créée', 'créé']):
        return 'success'
    return 'show'

def replace_simple_alerts(content):
    lines = content.split('\n')
    new_lines = []
    for line in lines:
        if line.strip().startswith('//'):
            new_lines.append(line)
            continue
        if 'alert(' in line and 'sweetalert' not in line.lower():
            match = re.search(r"alert\s*\(\s*(['\"])(.*?)\1\s*\)", line)
            if match:
                quote_char = match.group(1)
                message = match.group(2)
                method = determine_notification_type(message)
                replacement = f"this.notification.{method}({quote_char}{message}{quote_char})"
                line = line.replace(match.group(0), replacement)
        new_lines.append(line)
    return '\n'.join(new_lines)

for rel_path, import_path in REMAINING_FILES.items():
    file_path = BASE_DIR / rel_path
    if not file_path.exists():
        print(f"NOT FOUND: {rel_path}")
        continue
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        original = content
        content = add_import(content, import_path)
        content = inject_service(content)
        content = replace_simple_alerts(content)
        if content != original:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✓ {rel_path}")
        else:
            print(f"  (no changes) {rel_path}")
    except Exception as e:
        print(f"ERROR {rel_path}: {e}")
