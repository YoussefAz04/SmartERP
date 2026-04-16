import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
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
export class SigninComponent implements OnInit, AfterViewInit, OnDestroy {
  public routes = routes;

  loginForm: FormGroup;
  loading = false;
  errorMessage = '';
  rememberMe = false;

  private counterTimers: number[] = [];
  
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
      const duration = 2200;
      const startTime = performance.now();

      const easeOutQuart = (t: number): number => 1 - Math.pow(1 - t, 4);

      let frameId = 0;
      const animate = (currentTime: number) => {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);
        const easedProgress = easeOutQuart(progress);
        const current = Math.floor(easedProgress * target);

        el.textContent = current.toLocaleString('fr-FR');

        if (progress < 1) {
          frameId = requestAnimationFrame(animate);
        }
      };

      frameId = requestAnimationFrame(animate);
      this.counterTimers.push(frameId);
    });
  }

  ngOnDestroy(): void {
    this.counterTimers.forEach(id => cancelAnimationFrame(id));
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