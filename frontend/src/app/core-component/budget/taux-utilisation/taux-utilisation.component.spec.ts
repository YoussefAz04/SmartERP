import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TauxUtilisationComponent } from './taux-utilisation.component';

describe('TauxUtilisationComponent', () => {
  let component: TauxUtilisationComponent;
  let fixture: ComponentFixture<TauxUtilisationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TauxUtilisationComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(TauxUtilisationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
