import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { routes } from 'src/app/core/helpers/routes';
import { AuthService } from "../../service/auth.service";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
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
  public password: boolean[] = [false];

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
    // Mark all fields as touched to show validation errors
    this.loginForm.markAllAsTouched();
  }

  responseLogin!: PermissionDto;

  navigation() {
    console.log('Form Valid:', this.loginForm.valid);
    console.log('Form Value:', this.loginForm.value);

    if (!this.loginForm.valid) {
      this.errorMessage = 'Please fill in all fields correctly';
      console.warn('Form is invalid:', this.loginForm.errors);
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const credentials = this.loginForm.value;
    console.log('Logging in with:', credentials);

    this.authService.login(credentials).subscribe({
      next: (response) => {
        console.log("Login response:", response);

        if (response && response.token) {
          localStorage.removeItem("myLSkey");
          localStorage.setItem('myLSkey', btoa(JSON.stringify({
            token: response.token,
            name: response.userName,
            userId: response.idUsers,
            permission: response.permissionDtos,
            entreprise: response.entreprise,
            idRole: response.idRoleUser
          })));

          this.moduleService.updateStatusByRole(response.idRoleUser).subscribe(() => {
            console.log("Sidebar updated successfully");
            this.router.navigate([routes.adminDashboard]);
          }, (error) => {
            console.error('Module update error:', error);
            this.router.navigate([routes.adminDashboard]);
          });
        } else {
          this.errorMessage = 'Login failed: Invalid response from server';
          this.loading = false;
        }
      },
      error: (error) => {
        console.error('Login error:', error);
        this.errorMessage = error?.error?.message || 'Login failed. Please check your credentials.';
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
    return this.loginForm.controls;
  }
}