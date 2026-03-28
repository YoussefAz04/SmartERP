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
    console.log('✅ SigninComponent initialized');
  }

  responseLogin!: PermissionDto;
  
  navigation() {
    console.log('🔐 Sign In clicked');
    console.log('Form valid:', this.loginForm.valid);
    console.log('Form data:', this.loginForm.value);
    
    if (!this.loginForm.valid) {
      console.warn('❌ Form invalid');
      this.errorMessage = 'Please fill in username and password';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const credentials = {
      username: this.loginForm.get('username')?.value,
      password: this.loginForm.get('password')?.value
    };

    console.log('📤 Sending:', credentials);

    this.authService.login(credentials).subscribe({
      next: (response) => {
        console.log('✅ Login success:', response);
        
        // Save user data
        localStorage.removeItem("myLSkey");
        localStorage.setItem('myLSkey', btoa(JSON.stringify({
          token: response.token,
          name: response.userName,
          userId: response.idUsers,
          permission: response.permissionDtos,
          entreprise: response.entreprise,
          idRole: response.idRoleUser
        })));

        console.log('✅ User saved to localStorage');

        // Update sidebar
        this.moduleService.updateStatusByRole(response.idRoleUser).subscribe(
          () => console.log("✅ Sidebar updated"),
          (err) => console.error("⚠️ Sidebar error:", err)
        );

        // ✅ FIXED: Use the correct route from routes helper
        console.log('📍 Navigating to:', routes.adminDashboard);
        setTimeout(() => {
          this.router.navigate([routes.adminDashboard]).then((success) => {
            if (success) {
              console.log('✅ Navigation successful');
            } else {
              console.error('❌ Navigation failed');
              // Fallback to enterprise list
              this.router.navigate(['/enterprise/listEnterprise']);
            }
            this.loading = false;
          }).catch((err) => {
            console.error('❌ Navigation error:', err);
            this.loading = false;
            // Fallback to enterprise list
            this.router.navigate(['/enterprise/listEnterprise']);
          });
        }, 500);
      },
      error: (error) => {
        console.error('❌ Login failed:', error);
        this.loading = false;
        this.errorMessage = error.error?.message || `Login failed: ${error.status}`;
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