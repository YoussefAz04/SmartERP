# Admin Permissions Fix Guide

## Problem Summary
Admin users cannot see CRUD (Create, Edit, Delete) buttons for:
- Clients
- Fournisseurs
- Article Achat
- Article Vente

**Root Causes:**
1. `ADMIN` role did not exist in the `roles` table
2. Role name was stored as `Super_admin` instead of `SUPER_ADMIN` (case mismatch with frontend)
3. Module titles in `module_tittle` table were missing the word "de" (e.g., `gestion Client` instead of `gestion de Client`)
4. Module titles for Fournisseur were also missing "de" (`gestion Fournisseur` → `gestion de Fournisseur`)
5. No permission records existed for the ADMIN role

## Solution

### Option A — Fresh Database (scriptYoussef.sql already fixed)
The `scriptYoussef.sql` script has been updated to include all the correct data.
Simply drop and recreate the database using the updated script.

### Option B — Existing Database (run the migration script)
Execute `database/fixAdminPermissions.sql` in SQL Server Management Studio:

1. Open **SQL Server Management Studio**
2. Connect to your **BusinessManager** database
3. Open `database/fixAdminPermissions.sql`
4. Click **Execute** (F5)

**What this script does:**
- ✅ Renames `Super_admin` role to `SUPER_ADMIN` if needed
- ✅ Creates `ADMIN` role if it does not exist
- ✅ Removes duplicate/incorrect module_tittle entries
- ✅ Inserts correct module titles (`gestion de Client`, `gestion de Fournisseur`, `gestion Article`)
- ✅ Re-inserts correct module records
- ✅ Re-inserts sous_module navigation entries
- ✅ Restores SUPER_ADMIN permissions for these modules
- ✅ Creates full CRUD permissions for the ADMIN role
- ✅ Runs verification queries

### Step 2: Clear Browser Cache
Clear your browser's localStorage to force a fresh login:

```javascript
// Open browser console (F12) and run:
localStorage.clear()
```

### Step 3: Login Again
1. Log out from the application
2. Log in again as an **admin user**

### Step 4: Verify Fix
You should now see:
- ✅ **"Ajouter Client"** button on clients page
- ✅ **"Ajouter Fournisseur"** button on fournisseurs page
- ✅ **"Ajouter Article"** button on article achat page
- ✅ **"Ajouter Article"** button on article vente page
- ✅ **Edit (✏️)** and **Delete (🗑️)** buttons on all items

### Step 5: Check Browser Console
Open **Developer Tools** (F12) → **Console** tab and verify:

```
Create permission: true  ← Should be TRUE now
```

## What Was Fixed

### scriptYoussef.sql Changes

| Change | Before | After |
|--------|--------|-------|
| Role name | `Super_admin` | `SUPER_ADMIN` |
| ADMIN role | Missing | Added (id=2) |
| module_tittle 100 | `gestion Client` | `gestion de Client` |
| module_tittle 200 | `gestion Fournisseur` | `gestion de Fournisseur` |
| module 200 title | `gestion Fournisseur` | `gestion de Fournisseur` |
| module 201 title | `gestion Categorie Fournisseur` | `gestion de Categorie Fournisseur` |
| ADMIN permissions | Missing | Added for modules 100, 101, 200, 201, 300, 301 |

## Database Structure After Fix

### Roles Table
```
ID | Name
---|------------
1  | SUPER_ADMIN
2  | ADMIN
```

### Module Titles Table
```
ID  | Title
----|---------------------------
100 | gestion de Client
200 | gestion de Fournisseur
300 | gestion Article
```

### Modules Table
```
ID  | Title                           | Module Title ID
----|---------------------------------|----------------
100 | gestion de Client               | 100
101 | gestion de Categorie Client     | 100
200 | gestion de Fournisseur          | 200
201 | gestion de Categorie Fournisseur| 200
300 | gestion Article Achat           | 300
301 | gestion Article Vente           | 300
```

### Permissions Table (ADMIN role)
```
Permission ID | Module ID | Role  | Create | View | Update | Delete
--------------|-----------|-------|--------|------|--------|--------
1100          | 100       | ADMIN | 1      | 1    | 1      | 1
1101          | 101       | ADMIN | 1      | 1    | 1      | 1
1200          | 200       | ADMIN | 1      | 1    | 1      | 1
1201          | 201       | ADMIN | 1      | 1    | 1      | 1
1300          | 300       | ADMIN | 1      | 1    | 1      | 1
1301          | 301       | ADMIN | 1      | 1    | 1      | 1
```

## How the Frontend Permission Check Works

### Button Visibility
Buttons are shown when the user's JWT contains `ADMIN` or `SUPER_ADMIN` role:
```typescript
const isSuperAdmin = roles.includes('SUPER_ADMIN');
const isAdmin = roles.includes('ADMIN');
this.canCreateClient = isSuperAdmin || isAdmin;  // ← Now TRUE for ADMIN users
this.canEditClient   = isSuperAdmin || isAdmin;
this.canDeleteClient = isSuperAdmin || isAdmin;
```

### Template Example
```html
<button *ngIf="canCreateClient" ...>
  <i class="feather icon-plus-circle"></i> Ajouter Client
</button>
```

## Troubleshooting

### Buttons still not showing after running the script

1. ✅ Verify the script ran without errors
2. ✅ Check that 6 permission records exist for ADMIN role (the verification query at the end of the script shows this)
3. ✅ Clear browser localStorage: open DevTools (F12) → Console → `localStorage.clear()`
4. ✅ Log out and log back in
5. ✅ Verify your user account has the `ADMIN` role assigned in `user_roles`

### Checking user role assignment
```sql
SELECT u.username, r.name AS role
FROM app_user u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'your_admin_username'
```

### Getting a "duplicate key" error when running the migration
The script uses `IF NOT EXISTS` checks to be idempotent. If you still get errors:
```sql
-- Manual cleanup if needed
DELETE FROM [dbo].[permission] WHERE [id] IN (1100, 1101, 1200, 1201, 1300, 1301)
-- Then run the migration script again
```
