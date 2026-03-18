import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AffecterMembreToEquipeComponent } from './affecter-membre-to-equipe.component';

describe('AffecterMembreToEquipeComponent', () => {
  let component: AffecterMembreToEquipeComponent;
  let fixture: ComponentFixture<AffecterMembreToEquipeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AffecterMembreToEquipeComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AffecterMembreToEquipeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
