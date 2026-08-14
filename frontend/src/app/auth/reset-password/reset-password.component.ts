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
        'The reset token is missing. Please request a new link.';
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
        this.errorMessage = 'Please enter a valid password (at least 6 characters).';
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
          'Password updated successfully. You can now sign in.';
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
          'Unable to reset the password. The link may have expired.';
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
