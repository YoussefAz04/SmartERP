import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetaisFactureDeVenteComponent } from './detais-facture-de-vente.component';

describe('DetaisFactureDeVenteComponent', () => {
  let component: DetaisFactureDeVenteComponent;
  let fixture: ComponentFixture<DetaisFactureDeVenteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetaisFactureDeVenteComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DetaisFactureDeVenteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
