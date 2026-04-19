import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../../../auth/service/user/user.service';
import { UserDTO } from '../../../model/userDTO.model';
import { ResponseDto } from '../../../model/reponseDto.model';
import { PermissionDto } from '../../../model/permissionDto.model';
import { NotificationService } from '../../../shared/notification/notification.service';

@Component({
  selector: 'app-detais-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './detais-users.component.html',
  styleUrl: './detais-users.component.scss'
})
export class DetaisUsersComponent implements OnInit {
  user: UserDTO | null = null;
  isLoading = true;
  userId!: number;
  isSubmitting = false;
  rolesString: string = '';
  
  permissionUsers: PermissionDto = {
    create: false,
    view: false,
    update: false,
    delete: false,
    moduleTitle: ''
  } as PermissionDto;

  constructor(private userService: UserService,
    private route: ActivatedRoute,
    private router: Router,
    private notification: NotificationService) {}

  ngOnInit(): void {
    this.userId = this.route.snapshot.params['id'];
    this.loadUserDetails();
    this.getUserAuthData();
  }

  loadUserDetails(): void {
    this.isLoading = true;
    this.userService.getUserById(this.userId).subscribe({
      next: (data) => {
        this.user = data;
        this.rolesString = data.roles?.join(', ') ?? '';
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Error loading user:', err);
        this.notification.error('Erreur lors du chargement de l\'utilisateur');
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (!this.user || !this.userId) {
      this.notification.error('❌ Données utilisateur manquantes');
      return;
    }

    if (this.isSubmitting) {
      this.notification.info('Opération en cours...');
      return;
    }

    this.isSubmitting = true;

    this.user.roles = this.rolesString
      .split(',')
      .map(r => r.trim())
      .filter(r => r.length > 0);

    this.userService.updateUser(this.userId, this.user).subscribe({
      next: (response: any) => {
        this.isSubmitting = false;
        this.notification.success('✓ Utilisateur mis à jour avec succès');
        this.router.navigate(['/user-management/users']);
      },
      error: (err: any) => {
        this.isSubmitting = false;
        const errorMsg = err.error?.message || err.message || 'Erreur inconnue';
        this.notification.error('✗ Erreur lors de la mise à jour: ' + errorMsg);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/user-management/users']);
  }

  getUserAuthData(): void {
    const item = localStorage.getItem('myLSkey');
    
    if (item) {
      try {
        const currentUser: ResponseDto = JSON.parse(decodeURIComponent(escape(atob(item))));

        if (currentUser?.permission && Array.isArray(currentUser.permission)) {
          currentUser.permission.forEach(per => {
            if (per.moduleTitle.toLowerCase().includes("gestion users") || 
                per.moduleTitle.toLowerCase().includes("users")) {
              this.permissionUsers = per;
            }
          });
        }
      } catch (error) {
        console.error('Error parsing user data:', error);
      }
    }
  }
}