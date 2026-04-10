import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface MonthlySalesDto {
  month: string;
  amount: number;
}

export interface TopCustomerDto {
  name: string;
  totalSpent: number;
  ordersCount: number;
}

export interface TopProductDto {
  productName: string;
  quantitySold: number;
  revenue: number;
}

export interface DashboardStatsDto {
  // Sales
  totalSalesRevenue: number;
  salesThisMonth: number;
  salesThisYear: number;
  totalOrders: number;
  averageOrderValue: number;
  // Purchases
  totalPurchases: number;
  purchasesThisMonth: number;
  totalPurchaseOrders: number;
  // Inventory
  totalStockValue: number;
  lowStockItemsCount: number;
  outOfStockItemsCount: number;
  totalSKUs: number;
  // Financial
  totalRevenue: number;
  totalExpenses: number;
  profitLoss: number;
  accountsReceivable: number;
  accountsPayable: number;
  // Customers
  totalCustomers: number;
  newCustomersThisMonth: number;
  topCustomers: TopCustomerDto[];
  // Orders
  orderStatusBreakdown: { [key: string]: number };
  purchaseOrderStatusBreakdown: { [key: string]: number };
  // Charts
  monthlySalesChart: MonthlySalesDto[];
  monthlyPurchasesChart: MonthlySalesDto[];
  topProducts: TopProductDto[];
}

@Injectable({
  providedIn: 'root'
})
export class DashboardApiService {
  private baseUrl = `${environment.apiUrl}/api/dashboard`;

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
      } catch (error) {
        return null;
      }
    }
    return null;
  }

  getDashboardStats(): Observable<DashboardStatsDto> {
    return this.http.get<DashboardStatsDto>(`${this.baseUrl}/stats`, {
      headers: this.getHeaders()
    });
  }
}
