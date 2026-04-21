import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CommonModule } from '@angular/common';

import { AdminDashboardComponent } from './admin-dashboard.component';
import { ClientService } from 'src/app/core/service/client/client.service';
import { FournisseurService } from 'src/app/core/service/fournisseur/fournisseur.service';
import { ArticleAchatService } from 'src/app/core/service/article-achat/article-achat.service';
import { StockService } from 'src/app/core/service/stock/stock.service';
import { CommandeVenteService } from 'src/app/core/service/commande-vente/commande-vente.service';
import { of } from 'rxjs';

describe('AdminDashboardComponent', () => {
  let component: AdminDashboardComponent;
  let fixture: ComponentFixture<AdminDashboardComponent>;

  const clientServiceMock = {
    getAllClients: () => of([]),
  };
  const fournisseurServiceMock = {
    getAllFournisseurs: () => of([]),
  };
  const articleServiceMock = {
    getAllArticlesAchat: () => of([]),
  };
  const stockServiceMock = {
    getAllStocks: () => of([]),
  };
  const commandeServiceMock = {
    getAllCommandes: () => of([]),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AdminDashboardComponent],
      imports: [CommonModule, RouterTestingModule, HttpClientTestingModule],
      providers: [
        { provide: ClientService, useValue: clientServiceMock },
        { provide: FournisseurService, useValue: fournisseurServiceMock },
        { provide: ArticleAchatService, useValue: articleServiceMock },
        { provide: StockService, useValue: stockServiceMock },
        { provide: CommandeVenteService, useValue: commandeServiceMock },
      ],
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.userName).toBeDefined();
    expect(component.stats.totalClients).toBe(0);
    expect(component.stats.totalFournisseurs).toBe(0);
    expect(component.stats.totalArticles).toBe(0);
  });

  it('should load data and update stats', () => {
    expect(component.statsLoaded).toBeTrue();
    expect(component.loadingClients).toBeFalse();
    expect(component.loadingFournisseurs).toBeFalse();
    expect(component.loadingArticles).toBeFalse();
    expect(component.loadingCommandes).toBeFalse();
  });
});
