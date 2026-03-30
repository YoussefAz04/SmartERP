import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../../../auth/service/user/user.service';
import { UserDTO } from '../../../model/userDTO.model';

@Component({
  selector: 'app-detais-users',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './detais-users.component.html',
  styleUrl: './detais-users.component.scss'
})
export class DetaisUsersComponent implements OnInit {
  user: UserDTO | null = null;
  isLoading = true;
  userId!: number;

  constructor(
    private userService: UserService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.userId = this.route.snapshot.params['id'];
    this.loadUserDetails();
  }

  loadUserDetails(): void {
    this.isLoading = true;
    this.userService.getUserById(this.userId).subscribe({
      next: (data) => {
        this.user = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading user:', err);
        alert('Error loading user details');
        this.isLoading = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/user-management/users']);
  }
}