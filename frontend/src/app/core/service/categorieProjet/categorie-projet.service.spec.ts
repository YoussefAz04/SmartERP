import { TestBed } from '@angular/core/testing';

import { CategorieProjetService } from './categorie-projet.service';

describe('CategorieProjetService', () => {
  let service: CategorieProjetService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CategorieProjetService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
