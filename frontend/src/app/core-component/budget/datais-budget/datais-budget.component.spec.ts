import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DataisBudgetComponent } from './datais-budget.component';

describe('DataisBudgetComponent', () => {
  let component: DataisBudgetComponent;
  let fixture: ComponentFixture<DataisBudgetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DataisBudgetComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DataisBudgetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
