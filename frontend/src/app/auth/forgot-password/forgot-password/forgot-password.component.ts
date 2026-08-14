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

    this.forgotForm.markAllAsTouched();
    if (!this.forgotForm.valid) {
      this.errorMessage = 'Veuillez saisir une adresse e-mail valide.';
      return;
    }

    this.loading = true;
    const email = this.forgotForm.get('email')?.value;

    this.authService.forgotPassword(email).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage =
          'If an account exists for this address, a reset link has been sent by email. Please check your inbox.';
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

  get f() {
    return this.forgotForm.controls;
  }
}
