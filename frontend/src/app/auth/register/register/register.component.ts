import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { routes } from 'src/app/core/helpers/routes';
import { AuthService } from '../../service/auth.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  public routes = routes;
<<<<<<< Updated upstream
  public password: boolean[] = [false, false, false];

  registerForm: FormGroup;
  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private router: Router,
    private fb: FormBuilder,
    private authService: AuthService
  ) {
    this.registerForm = this.fb.group(
      {
        name: ['', [Validators.required, Validators.minLength(3)]],
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', [Validators.required]]
      },
      { validators: this.passwordMatchValidator }
    );
  }

  passwordMatchValidator(group: FormGroup) {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    if (password && confirmPassword && password !== confirmPassword) {
      return { passwordMismatch: true };
    }
    return null;
  }

  navigation() {
    this.registerForm.markAllAsTouched();

    if (!this.registerForm.valid) {
      this.errorMessage = 'Please fill in all fields correctly';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const formData = this.registerForm.value;
    const registerData = {
      username: formData.email,
      email: formData.email,
      password: formData.password,
      name: formData.name
    };

    this.authService.register(registerData).subscribe({
      next: (response) => {
        console.log('Registration successful:', response?.message);
        this.successMessage = 'Account created successfully! Redirecting to sign in...';
        this.registerForm.reset();
        setTimeout(() => {
          this.router.navigate([routes.signIn]);
        }, 2000);
      },
      error: (error) => {
        console.error('Registration error:', error?.status);
        if (error?.status === 0) {
          this.errorMessage = 'Cannot connect to server. Is backend running on http://localhost:8020?';
        } else if (error?.status === 409) {
          this.errorMessage = error?.error || 'Email or username already exists. Please use different credentials.';
        } else if (error?.status === 400) {
          this.errorMessage = error?.error || 'Invalid input. Please check your data.';
        } else {
          this.errorMessage = error?.error?.message || error?.error || 'Registration failed. Please try again.';
        }
        this.loading = false;
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  public togglePassword(index: number) {
    this.password[index] = !this.password[index];
  }

  get f() {
    return this.registerForm.controls;
=======
  public password: boolean[] = [false];

  constructor(private router: Router) {}

  navigation() {
    this.router.navigate([routes.signIn]);
>>>>>>> Stashed changes
  }

  public togglePassword(index: number) {
    this.password[index] = !this.password[index];
  }
}