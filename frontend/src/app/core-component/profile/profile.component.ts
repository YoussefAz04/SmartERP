import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { UserService } from '../../auth/service/user/user.service';
import { UserDTO } from '../../model/userDTO.model';
import { ResponseDto } from '../../model/reponseDto.model';
import { NotificationService } from '../../shared/notification/notification.service';
import { RoleService } from '../../core/service/role/role.service';
import { WebstorageService } from '../../shared/webstorage.service';

/**
 * Profile page – shows the authenticated user's account information and lets
 * them update their own basic info (username, email, phone). Editing is
 * available for ADMIN / SUPER_ADMIN. EMPLOYEE users see a read-only profile.
 */
@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss',
})
export class ProfileComponent implements OnInit {
  user: UserDTO | null = null;
  userId: number | null = null;
  roles: string[] = [];
  entrepriseName = '';
  isLoading = true;
  isSubmitting = false;
  loadError = false;

  // Read-only flag: only ADMIN / SUPER_ADMIN can update username/email/phone
  canEdit = false;

  // Avatar initials derived from username
  initials = '';

  // Change-password form (available to all roles)
  showPasswordForm = false;
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';
  isChangingPassword = false;

  constructor(
    private userService: UserService,
    private notification: NotificationService,
    private roleService: RoleService,
    private webStorage: WebstorageService,
  ) {}

  ngOnInit(): void {
    this.canEdit = this.roleService.isAdminOrAbove();
    this.loadCurrentUser();
  }

  // --------------------------------------------------------------
  // Session helpers
  // --------------------------------------------------------------

  private getSession(): ResponseDto | null {
    const raw = localStorage.getItem('myLSkey');
    if (!raw) return null;
    try {
      return JSON.parse(decodeURIComponent(escape(atob(raw)))) as ResponseDto;
    } catch {
      return null;
    }
  }

  // --------------------------------------------------------------
  // Data loading
  // --------------------------------------------------------------

  loadCurrentUser(): void {
    const session = this.getSession();
    if (!session) {
      this.isLoading = false;
      this.loadError = true;
      this.notification.error('Session expirée, veuillez vous reconnecter.');
      return;
    }

    this.userId = session.userId ?? session.id;
    this.roles = Array.isArray(session.roles) ? session.roles : [];
    this.entrepriseName =
      session.entreprise?.nom || session.entrepriseName || '';

    if (!this.userId) {
      this.isLoading = false;
      this.loadError = true;
      this.notification.error("Impossible de déterminer l'utilisateur courant.");
      return;
    }

    this.isLoading = true;
    this.loadError = false;
    this.userService.getUserById(this.userId).subscribe({
      next: (data) => {
        this.user = data;
        this.roles = data.roles && data.roles.length ? data.roles : this.roles;
        if (data.entrepriseNom) {
          this.entrepriseName = data.entrepriseNom;
        }
        this.computeInitials();
        this.isLoading = false;
      },
      error: () => {
        // Fall back to session info when the API is unavailable so the page
        // remains useful instead of looking broken.
        this.user = {
          id: session.id,
          username: session.username || session.name || '',
          email: session.email || '',
          phone: '',
          enabled: session.enabled ?? true,
          roles: this.roles,
          entrepriseId: session.entrepriseId,
          entrepriseNom: this.entrepriseName,
        };
        this.computeInitials();
        this.loadError = true;
        this.isLoading = false;
        this.notification.error(
          'Erreur lors du chargement du profil. Affichage des informations de session.',
        );
      },
    });
  }

  private computeInitials(): void {
    const name = this.user?.username || '';
    this.initials = name
      .split(/\s+/)
      .filter(Boolean)
      .map((p) => p.charAt(0))
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  // --------------------------------------------------------------
  // Save
  // --------------------------------------------------------------

  onSubmit(form: NgForm): void {
    if (!this.user || !this.userId || !this.canEdit || form.invalid) {
      return;
    }
    this.isSubmitting = true;
    this.userService.updateUser(this.userId, this.user).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.notification.success('Profil mis à jour avec succès.');
        // Keep session display name in sync with the new username.
        this.syncSessionUsername(this.user!.username);
      },
      error: (err) => {
        this.isSubmitting = false;
        const message =
          (err && err.error && (err.error.message || err.error)) ||
          'Erreur lors de la mise à jour du profil.';
        this.notification.error(
          typeof message === 'string' ? message : 'Erreur lors de la mise à jour du profil.',
        );
      },
    });
  }

  private syncSessionUsername(username: string): void {
    const session = this.getSession();
    if (!session) return;
    session.username = username;
    session.name = username;
    try {
      const encoded = btoa(unescape(encodeURIComponent(JSON.stringify(session))));
      localStorage.setItem('myLSkey', encoded);
    } catch {
      // ignore – display will refresh on the next login
    }
  }

  // --------------------------------------------------------------
  // UI helpers
  // --------------------------------------------------------------

  cancel(): void {
    this.loadCurrentUser();
  }

  togglePasswordForm(): void {
    this.showPasswordForm = !this.showPasswordForm;
    if (!this.showPasswordForm) {
      this.currentPassword = '';
      this.newPassword = '';
      this.confirmPassword = '';
    }
  }

  onChangePassword(): void {
    if (!this.currentPassword || !this.newPassword || !this.confirmPassword) {
      this.notification.error('Veuillez remplir tous les champs.');
      return;
    }
    if (this.newPassword.length < 6) {
      this.notification.error('Le nouveau mot de passe doit contenir au moins 6 caractères.');
      return;
    }
    if (this.newPassword !== this.confirmPassword) {
      this.notification.error('Les nouveaux mots de passe ne correspondent pas.');
      return;
    }
    this.isChangingPassword = true;
    this.userService.changePassword(this.currentPassword, this.newPassword).subscribe({
      next: () => {
        this.isChangingPassword = false;
        this.notification.success('Mot de passe mis à jour avec succès.');
        this.togglePasswordForm();
      },
      error: (err) => {
        this.isChangingPassword = false;
        const message =
          (err?.error?.error || err?.error?.message || err?.error) ||
          'Erreur lors du changement de mot de passe.';
        this.notification.error(typeof message === 'string' ? message : 'Erreur lors du changement de mot de passe.');
      },
    });
  }

  logout(): void {
    this.webStorage.Logout();
  }

  get primaryRole(): string {
    return this.roles && this.roles.length ? this.roles[0] : 'USER';
  }
}
