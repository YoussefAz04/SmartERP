export interface PermissionDto {
  id: number;
  create: boolean;
  delete: boolean;
  update: boolean;
  view: boolean;

  moduleId: number;
  moduleTitle: string;

  roleId: number;
}
