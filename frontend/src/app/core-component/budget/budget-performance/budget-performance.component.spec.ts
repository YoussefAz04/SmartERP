import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BudgetPerformanceComponent } from './budget-performance.component';

describe('BudgetPerformanceComponent', () => {
  let component: BudgetPerformanceComponent;
  let fixture: ComponentFixture<BudgetPerformanceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BudgetPerformanceComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(BudgetPerformanceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
