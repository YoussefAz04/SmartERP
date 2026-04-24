import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ClientService } from '../../../core/service/client/client.service';
import { Client } from '../../../model/client.model';

@Component({
  selector: 'app-view-client',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './view-client.component.html',
  styleUrl: './view-client.component.scss'
})
export class ViewClientComponent implements OnInit {
  client: Client | null = null;
  isLoading = false;
  errorMessage = '';
  canEditClient = false;

  constructor(
    private clientService: ClientService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadRolePermissions();
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.loadClient(id);
    } else {
      this.errorMessage = 'ID client invalide';
    }
  }

  loadRolePermissions(): void {
    try {
      const raw = localStorage.getItem('myLSkey');
      if (!raw) return;
      const session = JSON.parse(decodeURIComponent(escape(atob(raw))));
      const roles: string[] = Array.isArray(session?.roles) ? session.roles : [];
      this.canEditClient = roles.includes('SUPER_ADMIN') || roles.includes('ADMIN') || roles.includes('EMPLOYEE');
    } catch {
      this.canEditClient = false;
    }
  }

  loadClient(id: number): void {
    this.isLoading = true;
    this.clientService.getClientById(id).subscribe({
      next: (data) => {
        this.client = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement du client:', err);
        this.errorMessage = 'Erreur lors du chargement du client';
        this.isLoading = false;
      }
    });
  }

  editClient(): void {
    if (this.client?.id) {
      this.router.navigate(['/client/editClient', this.client.id]);
    }
  }

  goBack(): void {
    this.router.navigate(['/client/listClient']);
  }
}
