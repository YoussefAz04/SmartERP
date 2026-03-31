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
      next: (response: any) => {
        console.log('✅ Login success:', response);
        
        // ✅ FIXED: Save user data with correct field names matching backend Response.java
        localStorage.removeItem("myLSkey");
        localStorage.setItem('myLSkey', btoa(JSON.stringify({
          token: response.token,
          name: response.name,              // ✅ FIXED: was response.userName
          username: response.username,      // ✅ ADDED
          userId: response.id,              // ✅ FIXED: was response.idUsers
          permission: response.permission,  // ✅ FIXED: was response.permissionDtos
          entreprise: response.entreprise,
          roles: response.roles,            // ✅ ADDED: array of role name strings
          roleId: response.roleId           // ✅ FIXED: was response.idRoleUser
        })));

        console.log('✅ User saved to localStorage');
        console.log('📋 Permissions saved:', response.permission);

        // ✅ FIXED: Use response.roleId instead of response.idRoleUser
        if (response.roleId) {
          this.moduleService.updateStatusByRole(response.roleId).subscribe(
            () => console.log("✅ Sidebar updated"),
            (err) => console.error("⚠️ Sidebar error:", err)
          );
        }

        console.log('📍 Navigating to:', routes.adminDashboard);
        setTimeout(() => {
          this.router.navigate([routes.adminDashboard]).then((success) => {
            if (success) {
              console.log('✅ Navigation successful');
            } else {
              console.error('❌ Navigation failed');
              this.router.navigate(['/enterprise/listEnterprise']);
            }
            this.loading = false;
          }).catch((err) => {
            console.error('❌ Navigation error:', err);
            this.loading = false;
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