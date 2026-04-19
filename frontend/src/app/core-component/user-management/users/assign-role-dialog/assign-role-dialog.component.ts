import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { Role } from '../../../../model/role.model';
import { NotificationService } from '../../../../shared/notification/notification.service';

@Component({
  selector: 'app-assign-role-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatSelectModule,
    MatButtonModule,
    MatFormFieldModule
  ],
  templateUrl: './assign-role-dialog.component.html',
  styleUrl: './assign-role-dialog.component.scss'
})
export class AssignRoleDialogComponent {
  selectedRoleId: number | null = null;
  userId: number;
  roles: Role[] = [];

  constructor(public dialogRef: MatDialogRef<AssignRoleDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private notification: NotificationService) {
    this.userId = data.userId;
    this.roles = data.roles || [];
    console.log('Dialog opened with roles:', this.roles);
  }

  assignRole(): void {
    if (!this.selectedRoleId) {
      this.notification.warning('Veuillez sélectionner un rôle');
      return;
    }
    this.dialogRef.close({ roleId: this.selectedRoleId });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
