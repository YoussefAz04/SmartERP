#!/usr/bin/env python3
import re
from pathlib import Path

BASE_DIR = Path("/home/runner/work/pfe-project/pfe-project/frontend/src/app")

# Files with confirm() calls to fix
FILES_WITH_CONFIRMS = [
    "core-component/fournisseur/list-categorie-fournisseur/list-categorie-fournisseur.component.ts",
    "core-component/main/application/calendar/calendar.component.ts",
    "core-component/article-achat/article-achat.component.ts",
    "core-component/article-vente/article-vente.component.ts",
    "core-component/entreprise/entreprise.component.ts",
    "core-component/commande-vente/list-commande-vente/list-commande-vente.component.ts",
    "core-component/client/list-categorie-client/list-categorie-client.component.ts",
    "core-component/budget/depense/depense.component.ts",
    "core-component/budget/budget.component.ts",
    "core-component/documentation/documentation.component.ts"
]

def fix_confirm_pattern_a(content):
    """Replace: if (!confirm(...)) return; with notification.confirm(...).then(ok => { if (!ok) return;"""
    # Pattern: if (!confirm('...')) return;
    pattern = r"if\s*\(\s*!confirm\s*\(\s*(['\"])(.*?)\1\s*\)\s*\)\s*return\s*;"
    
    def replacer(match):
        quote = match.group(1)
        message = match.group(2)
        return f"this.notification.confirm({quote}{message}{quote}).then(ok => {{ if (!ok) return;"
    
    return re.sub(pattern, replacer, content)

def fix_confirm_pattern_b(content):
    """Replace: if (!confirm(...)) { with notification.confirm(...).then(ok => { if (!ok) {"""
    # Pattern: if (!confirm('...')) {
    pattern = r"if\s*\(\s*!confirm\s*\(\s*(['\"])(.*?)\1\s*\)\s*\)\s*\{"
    
    def replacer(match):
        quote = match.group(1)
        message = match.group(2)
        return f"this.notification.confirm({quote}{message}{quote}).then(ok => {{ if (!ok) {{"
    
    return re.sub(pattern, replacer, content)

def fix_confirm_pattern_c(content):
    """Replace: if (confirm(...)) { with notification.confirm(...).then(ok => { if (ok) {"""
    # Pattern: if (confirm('...')) {
    pattern = r"if\s*\(\s*confirm\s*\(\s*(['\"])(.*?)\1\s*\)\s*\)\s*\{"
    
    def replacer(match):
        quote = match.group(1)
        message = match.group(2)
        return f"this.notification.confirm({quote}{message}{quote}).then(ok => {{ if (ok) {{"
    
    return re.sub(pattern, replacer, content)

def add_closing_braces(content):
    """Add closing braces for .then() blocks where needed."""
    # This is a simple heuristic - may need manual adjustment
    # Look for patterns where we have .then(ok => { if ... and need to close
    return content

processed = 0
for rel_path in FILES_WITH_CONFIRMS:
    file_path = BASE_DIR / rel_path
    
    if not file_path.exists():
        print(f"NOT FOUND: {rel_path}")
        continue
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original = content
        
        # Apply confirm replacements
        content = fix_confirm_pattern_a(content)
        content = fix_confirm_pattern_b(content)
        content = fix_confirm_pattern_c(content)
        
        if content != original:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✓ {rel_path}")
            processed += 1
        else:
            print(f"  (no changes) {rel_path}")
            
    except Exception as e:
        print(f"ERROR {rel_path}: {e}")

print(f"\nProcessed {processed} files")
