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

export interface MetricItem {
  label: string;
  value: string;
}

export interface Recommendation {
  type: string;
  title: string;
  description: string;
  priority: string;
  icon: string;
  actionLabel: string;
  metrics?: MetricItem[];
  insight?: string;
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

export interface PeriodComparison {
  current: number;
  previous: number;
  change: number;
  /** `null` means the previous period was 0 (so %-change is undefined → the UI should show a "NEW" badge). */
  changePercent: number | null;
  trend: 'up' | 'down' | 'stable';
}

export interface ComparisonMetrics {
  mtdVsLtd: PeriodComparison;
  ytdVsLastYear: PeriodComparison;
  purchaseYtd?: PeriodComparison;
  grossMarginYtd?: number;
  salesOrderCountYtd?: number;
  purchaseOrderCountYtd?: number;
  // Enhanced MTD multi-dimensional
  purchaseMtdVsLtd?: PeriodComparison;
  marginMtdVsLtd?: PeriodComparison;
  marginYtdVsLastYear?: PeriodComparison;
  orderCountMtdVsLtd?: PeriodComparison;
  orderCountYtdVsLastYear?: PeriodComparison;
}

export interface SupplierStatusCount {
  status: string;
  count: number;
}

export interface SupplierPerformance {
  supplierId: number;
  supplierName: string;
  orderCount: number;
  totalSpend: number;
  deliveryRate: number;
  rating: number;
  statusBreakdown: SupplierStatusCount[];
}

export interface NameValue {
  name: string;
  value: number;
}

export interface StatusCount {
  status: string;
  count: number;
  amount?: number;
}

export interface RevenueDetails {
  topProducts: NameValue[];
  topCustomers: NameValue[];
  paymentMethods: StatusCount[];
}

export interface OrdersDetails {
  byStatus: StatusCount[];
  totalOrders: number;
  avgOrderValue: number;
  totalRevenue: number;
}

export interface ProductsDetails {
  totalSKUs: number;
  lowStockCount: number;
  outOfStockCount: number;
  totalStockValue: number;
  topProducts: NameValue[];
  slowMovers: NameValue[];
}

export interface ForecastDetails {
  accuracy: number;
  predictedNextMonth: number;
  predictedNext3Months: number;
  predictedNext6Months: number;
  methodology: string;
  selectedModel?: string;
  crossValidatedMape?: number | null;
  crossValidatedSmape?: number | null;
  crossValidatedRmse?: number | null;
}

export interface KpiDetails {
  revenue: RevenueDetails;
  orders: OrdersDetails;
  products: ProductsDetails;
  forecast: ForecastDetails;
}

export interface AiDashboard {
  salesAnalysis: SalesAnalysis;
  forecast: Forecast;
  productIntelligence: ProductIntelligence;
  recommendations: Recommendation[];
  kpiSummary: KpiSummary;
  comparison?: ComparisonMetrics;
  kpiDetails?: KpiDetails;
  supplierPerformance?: SupplierPerformance[];
}

export interface PredictionLog {
  id: number;
  predictionDate: string;
  predictionMonth: string;
  targetMonth: string;
  targetMonthLabel: string;
  horizonMonths: number;
  predictedValue: number;
  lowerBound: number | null;
  upperBound: number | null;
  confidenceScore: number | null;
  methodology: string | null;
  modelName: string | null;
  actualValue: number | null;
  absoluteError: number | null;
  percentError: number | null;
  withinInterval: boolean | null;
  status: 'PENDING' | 'SETTLED';
  settledAt: string | null;
  createdAt: string;
}

export interface PredictionAccuracy {
  totalPredictions: number;
  settledPredictions: number;
  pendingPredictions: number;
  mae: number | null;
  mape: number | null;
  smape: number | null;
  bias: number | null;
  intervalCoverage: number | null;
  history: PredictionLog[];
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

  getPredictionHistory(): Observable<PredictionAccuracy> {
    return this.http.get<PredictionAccuracy>(`${this.baseUrl}/predictions`, { headers: this.getHeaders() });
  }

  reconcilePredictions(): Observable<{ settled: number }> {
    return this.http.post<{ settled: number }>(`${this.baseUrl}/predictions/reconcile`, {}, { headers: this.getHeaders() });
  }
}
