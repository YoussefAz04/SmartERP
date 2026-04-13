import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-stat-card',
  template: `
    <div class="stat-card" [ngClass]="'card-' + color">
      <div class="stat-card-content">
        <div class="stat-icon" [ngClass]="'icon-' + color">
          <i [class]="icon"></i>
        </div>
        <div class="stat-info">
          <h3 class="stat-value">{{ value }}</h3>
          <p class="stat-label">{{ label }}</p>
          <div class="stat-trend" [ngClass]="trend === 'up' ? 'trend-up' : 'trend-down'">
            <i [class]="trend === 'up' ? 'feather icon-trending-up' : 'feather icon-trending-down'"></i>
            <span>{{ trendValue }}</span>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .stat-card {
      background: white;
      border-radius: 10px;
      padding: 24px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
      transition: transform 0.28s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.28s cubic-bezier(0.4, 0, 0.2, 1);
      cursor: pointer;
      border: 1px solid rgba(0, 0, 0, 0.06);
      height: 100%;
    }

    .stat-card:hover {
      box-shadow: 0 8px 24px rgba(44, 62, 80, 0.12);
      transform: translateY(-2px);
    }

    .stat-card-content {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      flex-shrink: 0;
    }

    .icon-blue {
      background: linear-gradient(135deg, #0084D6 0%, #00A8E8 100%);
      color: white;
    }

    .icon-green {
      background: linear-gradient(135deg, #2C3E50 0%, #34495E 100%);
      color: white;
    }

    .icon-orange {
      background: linear-gradient(135deg, #27AE60 0%, #2ECC71 100%);
      color: white;
    }

    .icon-cyan {
      background: linear-gradient(135deg, #34495E 0%, #4A5568 100%);
      color: white;
    }

    .stat-info {
      flex: 1;
      min-width: 0;
    }

    .stat-value {
      margin: 0;
      font-size: 22px;
      font-weight: 700;
      color: #1a202c;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .stat-label {
      margin: 4px 0 8px 0;
      font-size: 13px;
      color: #718096;
      font-weight: 500;
    }

    .stat-trend {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      font-weight: 600;
    }

    .trend-up {
      color: #48bb78;
    }

    .trend-down {
      color: #f56565;
    }
  `]
})
export class StatCardComponent {
  @Input() label: string = '';
  @Input() value: string = '0';
  @Input() icon: string = 'feather icon-bar-chart-2';
  @Input() color: string = 'blue';
  @Input() trend: string = 'up';
  @Input() trendValue: string = '0%';
}
