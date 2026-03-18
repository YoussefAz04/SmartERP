import { TestBed } from '@angular/core/testing';

import { FactureDeVenteServiceService } from './facture-de-vente-service.service';

describe('FactureDeVenteServiceService', () => {
  let service: FactureDeVenteServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FactureDeVenteServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
