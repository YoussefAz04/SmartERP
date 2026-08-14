import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { routes } from 'src/app/core/helpers/routes';
import { AuthService } from 'src/app/auth/service/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent implements OnInit {
  
  registerForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  protected readonly routes = routes;
  password: boolean[] = [false, false, false];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initializeForm();
  }

  initializeForm(): void {
    this.registerForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
      phone: ['', []]
    }, { 
      validators: this.passwordMatchValidator 
    });
  }

  get f() {
    return this.registerForm.controls;
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.registerForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getErrorMessage(fieldName: string): string {
    const field = this.registerForm.get(fieldName);
    if (!field) return '';
    if (field.hasError('required')) return 'Ce champ est obligatoire';
    if (field.hasError('email')) return 'Email invalide';
    if (field.hasError('minlength')) return `Minimum ${field.errors?.['minlength']?.requiredLength} characters`;
    if (field.hasError('maxlength')) return `Maximum ${field.errors?.['maxlength']?.requiredLength} characters`;
    if (field.hasError('pattern')) return 'Format invalide';
    return '';
  }

  passwordMatchValidator(group: FormGroup): { [key: string]: any } | null {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  togglePassword(index: number): void {
    this.password[index] = !this.password[index];
  }

  navigation(): void {
    this.onSignUp();
  }

  onSignUp(): void {
    if (!this.registerForm.valid) {
      console.warn('❌ Form is invalid');
      this.markFormGroupTouched(this.registerForm);
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const registerData = {
      username: this.registerForm.get('name')?.value,
      email: this.registerForm.get('email')?.value,
      password: this.registerForm.get('password')?.value,
      phone: this.registerForm.get('phone')?.value || ''
    };

    console.log('📝 Registering user:', registerData.username);

    this.authService.register(registerData).subscribe({
      next: (response) => {
        console.log('✅ Registration successful:', response);
        this.isLoading = false;
        this.successMessage = 'Registration successful! Redirecting to signin...';
        
        setTimeout(() => {
          // ✅ Navigate to /auth/signin - ABSOLUTE PATH
          this.router.navigate(['/auth/signin']).then((success) => {
            if (success) {
              console.log('✅ Redirected to signin');
            } else {
              console.error('❌ Navigation failed');
              window.location.href = '/auth/signin';
            }
          }).catch((err) => {
            console.error('❌ Navigation error:', err);
            window.location.href = '/auth/signin';
          });
        }, 2000);
      },
      error: (error) => {
        console.error('❌ Registration error:', error);
        this.isLoading = false;
        
        if (error.error?.message) {
          this.errorMessage = error.error.message;
        } else if (error.error?.status === 'error') {
          this.errorMessage = error.error.message;
        } else {
          this.errorMessage = 'Registration failed. Please try again.';
        }
      }
    });
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();

      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }
}