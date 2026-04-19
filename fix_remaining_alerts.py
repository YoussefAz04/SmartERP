#!/usr/bin/env python3
import re
from pathlib import Path

BASE_DIR = Path("/home/runner/work/pfe-project/pfe-project/frontend/src/app")

def determine_notification_type(message_fragment):
    """Determine notification method based on message content."""
    msg_lower = message_fragment.lower()
    
    # Check for explicit symbols
    if any(s in message_fragment for s in ['✓', '✔', '✅']):
        return 'success'
    if any(s in message_fragment for s in ['✗', '✘', '❌']):
        return 'error'
    if '⚠' in message_fragment:
        return 'warning'
    
    # Check for error keywords
    if any(w in msg_lower for w in ['erreur', 'error', 'échec', 'impossible', 'invalide', 'missing']):
        return 'error'
    
    # Check for success keywords
    if any(w in msg_lower for w in ['succès', 'succ', 'avec succès', 'créée', 'créé', 'modifiée', 'validée', 'enregistrée']):
        return 'success'
    
    return 'show'

def fix_complex_alerts(content):
    """Fix alert() calls with concatenation or variables."""
    lines = content.split('\n')
    new_lines = []
    
    for line in lines:
        if line.strip().startswith('//'):
            new_lines.append(line)
            continue
        
        # Match: alert('...' + variable) or alert(variable + '...')
        # or alert(`...${variable}...`)
        if 'alert(' in line and 'sweetalert' not in line.lower():
            # Try to extract the alert call
            match = re.search(r'alert\s*\((.*?)\)\s*;', line)
            if match:
                alert_content = match.group(1)
                # Determine type from the literal parts
                method = determine_notification_type(alert_content)
                replacement = f'this.notification.{method}({alert_content});'
                line = line.replace(match.group(0), replacement)
        
        new_lines.append(line)
    
    return '\n'.join(new_lines)

# Get all .ts files in core-component and core/service
files_to_process = list(BASE_DIR.glob('core-component/**/*.component.ts'))
files_to_process.extend(list(BASE_DIR.glob('core/service/**/*.service.ts')))

processed = 0
for file_path in files_to_process:
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        if 'alert(' not in content or 'sweetalert' in content.lower():
            continue
        
        original = content
        content = fix_complex_alerts(content)
        
        if content != original:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            rel_path = file_path.relative_to(BASE_DIR)
            print(f"✓ {rel_path}")
            processed += 1
            
    except Exception as e:
        rel_path = file_path.relative_to(BASE_DIR)
        print(f"ERROR {rel_path}: {e}")

print(f"\nProcessed {processed} files")
