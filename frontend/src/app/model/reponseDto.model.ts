import { PermissionDto } from "./permissionDto.model";
import { Entreprise } from "./entreprise.model";

export interface ResponseDto {
  // ✅ New fields (added for users page fix)
  id: number;
  username: string;
  email: string;
  enabled: boolean;
  roles: string[];
  permission: PermissionDto[];

  // ✅ Keep existing fields (used across 30+ components)
  token: string;
  name: string;
  userId: number;        // ← used in departement.component.ts, profile.component.ts
  roleId: number;
  entreprise: Entreprise; // ← used in header, budget, client, equipe, facture, projet, etc.
}