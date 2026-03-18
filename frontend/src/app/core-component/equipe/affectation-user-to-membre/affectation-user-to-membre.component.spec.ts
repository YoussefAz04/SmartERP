import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AffectationUserToMembreComponent } from './affectation-user-to-membre.component';

describe('AffectationUserToMembreComponent', () => {
  let component: AffectationUserToMembreComponent;
  let fixture: ComponentFixture<AffectationUserToMembreComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AffectationUserToMembreComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AffectationUserToMembreComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
