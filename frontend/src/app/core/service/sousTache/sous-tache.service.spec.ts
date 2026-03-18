import { TestBed } from '@angular/core/testing';

import { SousTacheService } from './sous-tache.service';

describe('SousTacheService', () => {
  let service: SousTacheService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SousTacheService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
