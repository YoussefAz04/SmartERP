import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddFactureDeVenteComponent } from './add-facture-de-vente.component';

describe('AddFactureDeVenteComponent', () => {
  let component: AddFactureDeVenteComponent;
  let fixture: ComponentFixture<AddFactureDeVenteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddFactureDeVenteComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AddFactureDeVenteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
