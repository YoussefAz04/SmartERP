import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BarresVerticalesProjetComponent } from './barres-verticales-projet.component';

describe('BarresVerticalesProjetComponent', () => {
  let component: BarresVerticalesProjetComponent;
  let fixture: ComponentFixture<BarresVerticalesProjetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BarresVerticalesProjetComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(BarresVerticalesProjetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
