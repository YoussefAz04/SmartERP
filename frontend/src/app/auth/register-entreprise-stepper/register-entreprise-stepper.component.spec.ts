import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegisterEntrepriseStepperComponent } from './register-entreprise-stepper.component';

describe('RegisterEntrepriseStepperComponent', () => {
  let component: RegisterEntrepriseStepperComponent;
  let fixture: ComponentFixture<RegisterEntrepriseStepperComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterEntrepriseStepperComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(RegisterEntrepriseStepperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
