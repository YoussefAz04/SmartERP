import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { routes } from 'src/app/core/helpers/routes';
import { AuthService } from '../../service/auth.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.scss',
})
export class ForgotPasswordComponent {
  public routes = routes;

  forgotForm: FormGroup;
  loading = false;
  errorMessage = '';
  successMessage = '';
  resetLink = '';
  resetToken = '';

  constructor(
    private router: Router,
    private fb: FormBuilder,
    private authService: AuthService,
  ) {
    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });
  }

  navigation() {
    this.router.navigate(['/auth/signin']);
  }

  submit() {
    this.errorMessage = '';
    this.successMessage = '';
    this.resetLink = '';
    this.resetToken = '';

    this.forgotForm.markAllAsTouched();
    if (!this.forgotForm.valid) {
      this.errorMessage = 'Please enter a valid email address.';
      return;
    }

    this.loading = true;
    const email = this.forgotForm.get('email')?.value;

    this.authService.forgotPassword(email).subscribe({
      next: (response: any) => {
        this.loading = false;
        this.successMessage =
          response?.message ||
          'If an account exists for this email, a reset link has been generated.';
        // Email sending is not configured server-side; the API returns the
        // reset link directly so the user can complete the flow.
        if (response?.resetLink) {
          this.resetLink = response.resetLink;
          this.resetToken = response.token || '';
        }
      },
      error: (error: any) => {
        this.loading = false;
        this.errorMessage =
          (error?.error && typeof error.error === 'string'
            ? error.error
            : error?.error?.message) ||
          'An error occurred. Please try again.';
      },
    });
  }

  goToReset() {
    if (!this.resetToken) {
      return;
    }
    this.router.navigate(['/auth/reset-password'], {
      queryParams: { token: this.resetToken },
    });
  }

  get f() {
    return this.forgotForm.controls;
  }
}
