import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegisterEntrepriseSetpperComponent } from './register-entreprise-setpper.component';

describe('RegisterEntrepriseSetpperComponent', () => {
  let component: RegisterEntrepriseSetpperComponent;
  let fixture: ComponentFixture<RegisterEntrepriseSetpperComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterEntrepriseSetpperComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(RegisterEntrepriseSetpperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
