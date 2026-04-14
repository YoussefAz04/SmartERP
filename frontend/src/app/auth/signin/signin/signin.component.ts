import { Component, OnInit, AfterViewInit } from '@angular/core';
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
export class SigninComponent implements OnInit, AfterViewInit {
  public routes = routes;

  loginForm: FormGroup;
  loading = false;
  errorMessage = '';
  
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

  ngAfterViewInit(): void {
    this.animateCounters();
  }

  private animateCounters(): void {
    const elements = document.querySelectorAll<HTMLElement>('.stat-value[data-target]');
    elements.forEach(el => {
      const target = parseInt(el.getAttribute('data-target') || '0', 10);
      const duration = 1800;
      const step = target / (duration / 16);
      let current = 0;
      const timer = setInterval(() => {
        current += step;
        if (current >= target) {
          current = target;
          clearInterval(timer);
        }
        el.textContent = Math.floor(current).toLocaleString();
      }, 16);
    });
  }

  responseLogin!: PermissionDto;
  
  navigation() {
    console.log('✅ Sign In clicked');

    const usernameCtrl = this.loginForm.get('username');
    const passwordCtrl = this.loginForm.get('password');

    console.log('Form validity:', this.loginForm.valid);
    console.log('Username filled:', !!usernameCtrl?.value, '(valid:', usernameCtrl?.valid, ')');
    console.log('Password filled:', !!passwordCtrl?.value, '(valid:', passwordCtrl?.valid, ')');
    console.log('Form errors:', JSON.stringify(this.loginForm.errors));
    console.log('Username errors:', JSON.stringify(usernameCtrl?.errors));
    console.log('Password errors:', JSON.stringify(passwordCtrl?.errors));

    // Mark all controls as touched so validation messages appear
    this.loginForm.markAllAsTouched();

    if (!this.loginForm.valid) {
      this.errorMessage = 'Please fill in username and password';
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
            const fallback = () => this.router.navigate([routes.dashboard]);
            this.router.navigate([routes.adminDashboard])
              .then((success) => { if (!success) fallback(); })
              .catch(fallback)
              .finally(() => { this.loading = false; });
          }, 300);
        } catch (err) {
          console.error('❌ Error saving session:', err);
          this.loading = false;
          this.errorMessage = 'Une erreur est survenue. Veuillez réessayer.';
        }
      },
      error: (error: any) => {
        this.loading = false;
        this.errorMessage = 'Identifiants invalides. Veuillez réessayer.';
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