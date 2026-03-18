import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GeneratePdfFactureDeVenteComponent } from './generate-pdf-facture-de-vente.component';

describe('GeneratePdfFactureDeVenteComponent', () => {
  let component: GeneratePdfFactureDeVenteComponent;
  let fixture: ComponentFixture<GeneratePdfFactureDeVenteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GeneratePdfFactureDeVenteComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(GeneratePdfFactureDeVenteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
