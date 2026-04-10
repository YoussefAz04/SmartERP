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
  }

  responseLogin!: PermissionDto;
  
 navigation() {
    
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
        
        // ✅ SAVE TO LOCALSTORAGE
        localStorage.removeItem("myLSkey");
        localStorage.setItem('myLSkey', btoa(JSON.stringify({
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
        })));

        // ✅ ALWAYS SAVE ENTERPRISE ID SEPARATELY (both key variants for compatibility)
        const entrepriseId = response.entrepriseId;
        if (entrepriseId !== null && entrepriseId !== undefined) {
          localStorage.setItem('entrepriseId', entrepriseId.toString());
          localStorage.setItem('enterpriseId', entrepriseId.toString());
        }

        if (response.roleId) {
          this.moduleService.updateStatusByRole(response.roleId).subscribe(
            () => console.log("✅ Sidebar updated"),
            (err: any) => console.error("⚠️ Sidebar error:", err)
          );
        }

        setTimeout(() => {
          this.router.navigate([routes.adminDashboard]).then((success) => {
            if (success) {
            } else {
              this.router.navigate(['/enterprise/listEnterprise']);
            }
            this.loading = false;
          });
        }, 500);
      },
      error: (error: any) => {
        this.loading = false;
        this.errorMessage = 'Invalid credentials';
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