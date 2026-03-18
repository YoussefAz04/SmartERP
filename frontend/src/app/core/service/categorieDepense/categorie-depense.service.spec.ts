import { TestBed } from '@angular/core/testing';

import { CategorieDepenseService } from './categorie-depense.service';

describe('CategorieDepenseService', () => {
  let service: CategorieDepenseService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CategorieDepenseService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
