import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { routes } from 'src/app/core/helpers/routes';
import { AuthService } from '../service/auth.service';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.scss',
})
export class ResetPasswordComponent implements OnInit {
  public routes = routes;

  resetForm: FormGroup;
  loading = false;
  errorMessage = '';
  successMessage = '';
  token = '';
  tokenMissing = false;

  passwordVisible = false;
  confirmVisible = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
  ) {
    this.resetForm = this.fb.group(
      {
        newPassword: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', [Validators.required]],
      },
      { validators: [this.matchPasswords] },
    );
  }

  ngOnInit(): void {
    this.token = (this.route.snapshot.queryParamMap.get('token') || '').trim();
    if (!this.token) {
      this.tokenMissing = true;
      this.errorMessage =
        'Le jeton de réinitialisation est manquant. Veuillez demander un nouveau lien.';
    }
  }

  matchPasswords(group: AbstractControl): ValidationErrors | null {
    const pwd = group.get('newPassword')?.value;
    const confirm = group.get('confirmPassword')?.value;
    if (!pwd || !confirm) {
      return null;
    }
    return pwd === confirm ? null : { mismatch: true };
  }

  submit() {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.tokenMissing) {
      return;
    }

    this.resetForm.markAllAsTouched();
    if (!this.resetForm.valid) {
      if (this.resetForm.errors?.['mismatch']) {
        this.errorMessage = 'Les mots de passe ne correspondent pas.';
      } else {
        this.errorMessage = 'Veuillez saisir un mot de passe valide (au moins 6 caractères).';
      }
      return;
    }

    this.loading = true;
    const newPassword = this.resetForm.get('newPassword')?.value;

    this.authService.resetPassword(this.token, newPassword).subscribe({
      next: (response: any) => {
        this.loading = false;
        this.successMessage =
          response?.message ||
          'Mot de passe mis à jour avec succès. Vous pouvez désormais vous connecter.';
        // Redirect to sign-in shortly so the user sees the confirmation first.
        setTimeout(() => {
          this.router.navigate(['/auth/signin']);
        }, 1500);
      },
      error: (error: any) => {
        this.loading = false;
        this.errorMessage =
          (error?.error && typeof error.error === 'string'
            ? error.error
            : error?.error?.message) ||
          'Impossible de réinitialiser le mot de passe. Le lien a peut-être expiré.';
      },
    });
  }

  togglePassword() {
    this.passwordVisible = !this.passwordVisible;
  }

  toggleConfirm() {
    this.confirmVisible = !this.confirmVisible;
  }

  get f() {
    return this.resetForm.controls;
  }
}
