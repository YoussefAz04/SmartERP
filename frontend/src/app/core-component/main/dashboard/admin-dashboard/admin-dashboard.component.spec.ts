import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CommonModule } from '@angular/common';
import { of } from 'rxjs';

import { AdminDashboardComponent } from './admin-dashboard.component';
import { DashboardService } from 'src/app/core/service/dashboard/dashboard.service';

describe('AdminDashboardComponent', () => {
  let component: AdminDashboardComponent;
  let fixture: ComponentFixture<AdminDashboardComponent>;

  const dashboardServiceMock = {
    getHomeSummary: () => of({
      totalClients: 0,
      totalFournisseurs: 0,
      totalArticles: 0,
      totalCommandes: 0,
      recentClients: [],
      recentFournisseurs: [],
      recentStocks: [],
      recentCommandes: [],
    }),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AdminDashboardComponent],
      imports: [CommonModule, RouterTestingModule, HttpClientTestingModule],
      providers: [
        { provide: DashboardService, useValue: dashboardServiceMock },
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
