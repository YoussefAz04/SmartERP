import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface MonthlySales {
  month: string;
  amount: number;
}

export interface SalesAnalysis {
  monthlySales: MonthlySales[];
  totalRevenue: number;
  averageMonthlyRevenue: number;
  growthRate: number;
  bestMonth: string;
  bestMonthRevenue: number;
  trendLine: number[];
}

export interface ForecastPoint {
  period: string;
  actual: number | null;
  forecast: number;
  upperBound: number;
  lowerBound: number;
}

export interface Seasonality {
  month: string;
  index: number;
  trend: string;
}

export interface Forecast {
  forecastPoints: ForecastPoint[];
  predictedNextMonth: number;
  predictedNext3Months: number;
  predictedNext6Months: number;
  confidenceScore: number;
  methodology: string;
  seasonality: Seasonality[];
}

export interface ProductPerformance {
  productName: string;
  quantitySold: number;
  revenue: number;
  revenueShare: number;
  rank: number;
  performanceLabel: string;
}

export interface CategoryPerformance {
  categoryName: string;
  revenue: number;
  quantity: number;
  revenueShare: number;
}

export interface ProductIntelligence {
  topProducts: ProductPerformance[];
  slowMovingProducts: ProductPerformance[];
  categoryPerformance: CategoryPerformance[];
  totalProducts: number;
  activeProducts: number;
}

export interface Recommendation {
  type: string;
  title: string;
  description: string;
  priority: string;
  icon: string;
  actionLabel: string;
}

export interface KpiSummary {
  totalRevenue: number;
  revenueGrowth: number;
  totalOrders: number;
  orderGrowth: number;
  totalProducts: number;
  lowStockAlerts: number;
  profitMargin: number;
  avgOrderValue: number;
}

export interface AiDashboard {
  salesAnalysis: SalesAnalysis;
  forecast: Forecast;
  productIntelligence: ProductIntelligence;
  recommendations: Recommendation[];
  kpiSummary: KpiSummary;
}

@Injectable({
  providedIn: 'root'
})
export class AiIntelligenceService {
  private baseUrl = `${environment.apiUrl}/api/analytics`;

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = this.getToken();
    let headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return headers;
  }

  private getToken(): string | null {
    const item = localStorage.getItem('myLSkey');
    if (item) {
      try {
        const user = JSON.parse(decodeURIComponent(escape(atob(item))));
        return user.token || null;
      } catch {
        return null;
      }
    }
    return null;
  }

  getAiDashboard(): Observable<AiDashboard> {
    return this.http.get<AiDashboard>(`${this.baseUrl}/dashboard`, { headers: this.getHeaders() });
  }

  getSalesAnalysis(): Observable<SalesAnalysis> {
    return this.http.get<SalesAnalysis>(`${this.baseUrl}/sales`, { headers: this.getHeaders() });
  }

  getForecast(): Observable<Forecast> {
    return this.http.get<Forecast>(`${this.baseUrl}/forecast`, { headers: this.getHeaders() });
  }

  getProductIntelligence(): Observable<ProductIntelligence> {
    return this.http.get<ProductIntelligence>(`${this.baseUrl}/products`, { headers: this.getHeaders() });
  }

  getRecommendations(): Observable<Recommendation[]> {
    return this.http.get<Recommendation[]>(`${this.baseUrl}/recommendations`, { headers: this.getHeaders() });
  }
}
