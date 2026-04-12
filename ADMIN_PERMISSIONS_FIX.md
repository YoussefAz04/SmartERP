# Admin Permissions Fix Guide

## Problem Summary
Admin users cannot see CRUD (Create, Edit, Delete) buttons for:
- Clients
- Fournisseurs  
- Article Achat
- Article Vente

**Root Cause**: Database permission records were not properly set up, causing the permission lookup to fail.

## Solution Steps

### Step 1: Run the Fix Script
Execute the `fixAdminPermissions.sql` script in SQL Server Management Studio:

1. Open **SQL Server Management Studio**
2. Connect to your **BusinessManager** database
3. Open the `fixAdminPermissions.sql` file
4. Click **Execute** (F5)

**What this script does:**
- ✅ Creates ADMIN and SUPER_ADMIN roles if they don't exist
- ✅ Removes duplicate module_title entries
- ✅ Inserts correct module titles
- ✅ Removes duplicate module entries
- ✅ Inserts correct modules
- ✅ Creates CRUD permissions for ADMIN role
- ✅ Creates CRUD permissions for SUPER_ADMIN role
- ✅ Verifies all changes

### Step 2: Clear Browser Cache
Clear your browser's localStorage to force a fresh login:

```javascript
// Open browser console (F12) and run:
localStorage.clear()
```

### Step 3: Login Again
1. Clear your browser cache/cookies
2. Log out from the application
3. Log in again as **admin user**

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
User connecté: admin
Create permission: true  ← Should be TRUE now
Permissions: Array(40)
```

## What Was Fixed

### Before (Broken):
```sql
-- Database had:
- No ADMIN role
- Duplicate module_title IDs (100, 200, 300)
- No permission records for ADMIN role
- Frontend searches failed silently
- Result: Create permission = false
```

### After (Fixed):
```sql
-- Database now has:
✅ ADMIN role created
✅ SUPER_ADMIN role created
✅ Module titles: gestion de Client, gestion de Fournisseur, gestion Article
✅ Modules: Client, Fournisseur, Article Achat, Article Vente
✅ Complete CRUD permissions for both ADMIN and SUPER_ADMIN roles
✅ Results: Create permission = true
```

## Database Structure

### Roles Table
```
ID | Name
---|----------
1  | SUPER_ADMIN
2  | ADMIN
```

### Module Titles Table
```
ID  | Title
----|----------------------------
100 | gestion de Client
200 | gestion de Fournisseur
300 | gestion Article
```

### Modules Table
```
ID  | Title                          | Module Title ID
----|--------------------------------|----------------
100 | gestion de Client              | 100
101 | gestion de Categorie Client    | 100
200 | gestion de Fournisseur         | 200
201 | gestion de Categorie Fournisseur| 200
300 | gestion Article Achat          | 300
301 | gestion Article Vente          | 300
```

### Permissions Table
```
Module ID | Role    | Create | View | Update | Delete
----------|---------|--------|------|--------|--------
100       | ADMIN   | 1      | 1    | 1      | 1
101       | ADMIN   | 1      | 1    | 1      | 1
200       | ADMIN   | 1      | 1    | 1      | 1
201       | ADMIN   | 1      | 1    | 1      | 1
300       | ADMIN   | 1      | 1    | 1      | 1
301       | ADMIN   | 1      | 1    | 1      | 1
```

## Frontend Code (How Permissions Work)

### Client Component Example
```typescript
// File: frontend/src/app/core-component/client/client.component.ts

loadRolePermissions(): void {
  const item = localStorage.getItem('myLSkey');
  if (item) {
    const currentUser: ResponseDto = JSON.parse(decodeURIComponent(escape(atob(item))));
    const roles: string[] = Array.isArray(currentUser?.roles) ? currentUser.roles : [];
    
    // Role-based access
    const isSuperAdmin = roles.includes('SUPER_ADMIN');
    const isAdmin = roles.includes('ADMIN');
    this.canCreateClient = isSuperAdmin || isAdmin;  // ← Now TRUE!
    this.canEditClient = isSuperAdmin || isAdmin;
    this.canDeleteClient = isSuperAdmin || isAdmin;
  }
}
```

### Template (How Buttons Render)
```html
<!-- File: frontend/src/app/core-component/client/client.component.html -->

<button *ngIf="canCreateClient" type="button" class="btn-hero btn-hero-primary" (click)="addClient()">
  <i class="feather icon-plus-circle"></i> Ajouter Client
</button>
```

## Troubleshooting

### Issue: Buttons still not showing after running script

**Solution:**
1. ✅ Verify script ran without errors
2. ✅ Check that 6 permission records were created for ADMIN
3. ✅ Clear browser cache: `localStorage.clear()`
4. ✅ Log out and log in again
5. ✅ Check browser console for `Create permission: true`

### Issue: Getting database errors

**Solution:**
- The script is **idempotent** (safe to run multiple times)
- If you still get duplicate key errors, run script again
- Or manually delete old records:
```sql
DELETE FROM [dbo].[permission] WHERE [module_id] IN (100, 101, 200, 201, 300, 301)
DELETE FROM [dbo].[module] WHERE [id] IN (100, 101, 200, 201, 300, 301)
DELETE FROM [dbo].[module_tittle] WHERE [id] IN (100, 200, 300)
```

## After This Fix

You'll have:
1. ✅ Admin permissions properly configured in database
2. ✅ CRUD buttons visible for all admin users
3. ✅ Consistent permission system
4. ✅ Foundation for future role-based access control

## Questions?

If permissions still don't work:
1. Check browser console (F12) for the permission values
2. Verify ADMIN role is assigned to your user
3. Run the verification queries in the script to check data
4. Contact the development team with the console output