#!/usr/bin/env python3
"""
Add closing braces for notification.confirm().then() blocks
"""
from pathlib import Path
import re

BASE_DIR = Path("/home/runner/work/pfe-project/pfe-project/frontend/src/app")

# Files that have .then(ok => { if (!ok) return; pattern
FILES_NEEDING_CLOSING = [
    "core-component/fournisseur/list-categorie-fournisseur/list-categorie-fournisseur.component.ts",
    "core-component/article-achat/article-achat.component.ts",
    "core-component/article-vente/article-vente.component.ts",
    "core-component/commande-vente/list-commande-vente/list-commande-vente.component.ts",
    "core-component/client/list-categorie-client/list-categorie-client.component.ts",
    "core-component/documentation/documentation.component.ts"
]

for rel_path in FILES_NEEDING_CLOSING:
    file_path = BASE_DIR / rel_path
    
    if not file_path.exists():
        print(f"NOT FOUND: {rel_path}")
        continue
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        modified = False
        new_lines = []
        i = 0
        
        while i < len(lines):
            line = lines[i]
            new_lines.append(line)
            
            # Look for pattern: .then(ok => { if (!ok) return;
            if '.then(ok => { if (!ok) return;' in line:
                # Find the next closing brace of the method
                indent_level = len(line) - len(line.lstrip())
                j = i + 1
                brace_count = 0
                found_end = False
                
                while j < len(lines):
                    next_line = lines[j]
                    if '{' in next_line:
                        brace_count += next_line.count('{')
                    if '}' in next_line:
                        brace_count -= next_line.count('}')
                        # Check if we're at the method's closing brace
                        next_indent = len(next_line) - len(next_line.lstrip())
                        if next_indent <= indent_level and brace_count <= 0:
                            # Insert closing }); before this line
                            new_lines.append(' ' * (indent_level + 2) + '});\n')
                            modified = True
                            found_end = True
                            break
                    new_lines.append(next_line)
                    j += 1
                
                i = j
                continue
            
            i += 1
        
        if modified:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.writelines(new_lines)
            print(f"✓ {rel_path}")
        else:
            print(f"  (no changes needed) {rel_path}")
            
    except Exception as e:
        print(f"ERROR {rel_path}: {e}")

print("\nDone")
