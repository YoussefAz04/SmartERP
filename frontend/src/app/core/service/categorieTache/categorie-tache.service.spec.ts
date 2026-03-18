import { TestBed } from '@angular/core/testing';

import { CategorieTacheService } from './categorie-tache.service';

describe('CategorieTacheService', () => {
  let service: CategorieTacheService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CategorieTacheService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
