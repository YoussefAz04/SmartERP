import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PieChartProjetComponent } from './pie-chart-projet.component';

describe('PieChartProjetComponent', () => {
  let component: PieChartProjetComponent;
  let fixture: ComponentFixture<PieChartProjetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PieChartProjetComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PieChartProjetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
