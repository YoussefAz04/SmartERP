import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { routes } from 'src/app/core/helpers/routes';
import { AuthService } from "../../service/auth.service";
import { FormBuilder, FormGroup } from "@angular/forms";
import { Validators } from "@angular/forms";
import { PermissionDto } from "../../../model/permissionDto.model";
import { ModuleService } from "../../../core/service/Module/module.service";

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
    private moduleService: ModuleService
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
  }

  responseLogin!: PermissionDto;
  
  navigation() {
    // Mark all controls as touched so validation messages appear
    this.loginForm.markAllAsTouched();

    if (!this.loginForm.valid) {
      this.errorMessage = 'Veuillez renseigner votre nom d\'utilisateur et mot de passe';
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
          // ✅ SAVE TOKEN TO AUTH SERVICE FIRST (so interceptor can use it)
          const token = response.token;
          if (token) {
            this.authService.setToken(token);
          }

          // ✅ SAVE TO LOCALSTORAGE
          localStorage.removeItem("myLSkey");
          localStorage.setItem('myLSkey', btoa(unescape(encodeURIComponent(JSON.stringify({
            token: response.token,
            name: response.name,
            username: response.username,
            userId: response.id,
            permission: response.permission,
            roles: response.roles,
            roleId: response.roleId,
            entrepriseId: response.entrepriseId,
            entrepriseName: response.entrepriseName,
            // ✅ Provide entreprise object so components can access currentUser.entreprise.nom
            entreprise: {
              id: response.entrepriseId,
              nom: response.entrepriseName
            }
          })))));

          // ✅ ALWAYS SAVE ENTERPRISE ID SEPARATELY (both key variants for compatibility)
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
          this.errorMessage = 'Une erreur est survenue. Veuillez réessayer.';
        }
      },
      error: (error: any) => {
        this.loading = false;
        const code = error?.error?.error;
        const serverMessage = typeof error?.error === 'string' ? error.error : error?.error?.message;
        switch (code) {
          case 'INVALID_USERNAME':
            this.errorMessage = 'Nom d\'utilisateur invalide';
            break;
          case 'INVALID_PASSWORD':
            this.errorMessage = 'Mot de passe invalide';
            break;
          case 'ACCOUNT_DISABLED':
            this.errorMessage = 'Compte inactif. Veuillez contacter votre administrateur.';
            break;
          default:
            this.errorMessage = serverMessage || 'Identifiants invalides. Veuillez réessayer.';
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
