import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { routes } from 'src/app/core/helpers/routes';
import { AuthService } from "../../service/auth.service";
import { FormBuilder, FormGroup } from "@angular/forms";
import { Validators } from "@angular/forms";
import { PermissionDto } from "../../../model/permissionDto.model";
import { ModuleService } from "../../../core/service/Module/module.service";
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-signin',
  templateUrl: './signin.component.html',
  styleUrl: './signin.component.scss'
})
export class SigninComponent implements OnInit {
  public routes = routes;

  loginForm: FormGroup;
  loading = false;
  errorMessage = '';
  rememberMe = false;

  constructor(
    private router: Router, 
    private fb: FormBuilder,
    private authService: AuthService,
    private moduleService: ModuleService,
    private translate: TranslateService
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });
  }

  private static readonly REMEMBER_KEY = 'rememberedUsername';

  ngOnInit(): void {
    // Restore the previously remembered username, if any.
    try {
      const remembered = localStorage.getItem(SigninComponent.REMEMBER_KEY);
      if (remembered) {
        this.loginForm.patchValue({ username: remembered });
        this.rememberMe = true;
      }
    } catch {
      // localStorage may be unavailable (private mode); ignore.
    }
  }

  responseLogin!: PermissionDto;
  
  navigation() {
    // Mark all controls as touched so validation messages appear
    this.loginForm.markAllAsTouched();

    if (!this.loginForm.valid) {
      this.errorMessage = this.translate.instant('auth.errors.requiredCredentials');
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const credentials = {
      username: this.loginForm.get('username')?.value,
      password: this.loginForm.get('password')?.value
    };

    this.authService.login(credentials).subscribe({
      next: (response: any) => {
        try {
          // Persist / clear remembered username based on the checkbox state
          try {
            if (this.rememberMe) {
              localStorage.setItem(SigninComponent.REMEMBER_KEY, credentials.username);
            } else {
              localStorage.removeItem(SigninComponent.REMEMBER_KEY);
            }
          } catch {
            // ignore storage errors
          }

          // Store only non-sensitive session metadata. Authentication tokens
          // remain in HttpOnly cookies and are never exposed to JavaScript.
          localStorage.removeItem("myLSkey");
          localStorage.setItem('myLSkey', btoa(unescape(encodeURIComponent(JSON.stringify({
            name: response.name,
            username: response.username,
            userId: response.id,
            permission: response.permission,
            roles: response.roles,
            roleId: response.roleId,
            entrepriseId: response.entrepriseId,
            entrepriseName: response.entrepriseName,
            // rovide entreprise object so components can access currentUser.entreprise.nom
            entreprise: {
              id: response.entrepriseId,
              nom: response.entrepriseName
            }
          })))));

          // ALWAYS SAVE ENTERPRISE ID SEPARATELY (both key variants for compatibility)
          const entrepriseId = response.entrepriseId;
          if (entrepriseId !== null && entrepriseId !== undefined) {
            localStorage.setItem('entrepriseId', entrepriseId.toString());
            localStorage.setItem('enterpriseId', entrepriseId.toString());
          }

          setTimeout(() => {
            const isSuperAdmin = (response.roles ?? []).includes('SUPER_ADMIN');
            if (isSuperAdmin) {
              // SUPER_ADMIN is platform-level: land on enterprise management, not the business dashboard
              this.router.navigate(['/entreprise'])
                .catch(() => { window.location.href = '/entreprise'; })
                .finally(() => { this.loading = false; });
            } else {
              const fallback = () => this.router.navigate([routes.dashboard]);
              this.router.navigate([routes.adminDashboard])
                .then((success) => { if (!success) fallback(); })
                .catch(fallback)
                .finally(() => { this.loading = false; });
            }
          }, 300);
        } catch (err) {
          console.error('❌ Error saving session:', err);
          this.loading = false;
          this.errorMessage = this.translate.instant('auth.errors.generic');
        }
      },
      error: (error: any) => {
        this.loading = false;
        const code = error?.error?.error;
        const serverMessage = typeof error?.error === 'string' ? error.error : error?.error?.message;
        switch (code) {
          case 'INVALID_USERNAME':
            this.errorMessage = this.translate.instant('auth.errors.invalidUsername');
            break;
          case 'INVALID_PASSWORD':
            this.errorMessage = this.translate.instant('auth.errors.invalidPassword');
            break;
          case 'ACCOUNT_DISABLED':
            this.errorMessage = this.translate.instant('auth.errors.accountDisabled');
            break;
          case 'TOO_MANY_LOGIN_ATTEMPTS':
            this.errorMessage = this.translate.instant('auth.errors.tooManyAttempts');
            break;
          default:
            this.errorMessage = serverMessage || this.translate.instant('auth.errors.invalidCredentials');
        }
      }
    });
  }
  
  public password: boolean[] = [false];

  public togglePassword(index: number) {
    this.password[index] = !this.password[index];
  }
  
  get f() {
    return this.loginForm.controls;
  }
}
