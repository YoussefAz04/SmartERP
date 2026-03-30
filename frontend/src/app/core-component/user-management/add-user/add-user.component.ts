import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { UserService } from '../../../auth/service/user/user.service';

@Component({
  selector: 'app-add-user',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './add-user.component.html',
  styleUrl: './add-user.component.scss'
})
export class AddUserComponent implements OnInit {
  addUserForm!: FormGroup;
  isSubmitting = false;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initializeForm();
  }

  initializeForm(): void {
    this.addUserForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      phone: ['']
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.addUserForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  onSubmitAddUser(): void {
    if (!this.addUserForm.valid) {
      alert('Please fill all required fields');
      return;
    }

    this.isSubmitting = true;
    const userData = this.addUserForm.value;

    this.userService.registerUser(userData, 1, 1).subscribe({
      next: (response: any) => {
        console.log('User added successfully:', response);
        alert('✓ User added successfully');
        this.router.navigate(['/user-management/users']);
        this.isSubmitting = false;
      },
      error: (err: any) => {
        console.error('Error adding user:', err);
        alert('✗ Error: ' + (err.error?.message || err.message));
        this.isSubmitting = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/user-management/users']);
  }
}