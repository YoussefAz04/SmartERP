import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListFactureDeVenteComponent } from './list-facture-de-vente.component';

describe('ListFactureDeVenteComponent', () => {
  let component: ListFactureDeVenteComponent;
  let fixture: ComponentFixture<ListFactureDeVenteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListFactureDeVenteComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ListFactureDeVenteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
