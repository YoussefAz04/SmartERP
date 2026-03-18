import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PieChartTacheComponent } from './pie-chart-tache.component';

describe('PieChartTacheComponent', () => {
  let component: PieChartTacheComponent;
  let fixture: ComponentFixture<PieChartTacheComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PieChartTacheComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PieChartTacheComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
