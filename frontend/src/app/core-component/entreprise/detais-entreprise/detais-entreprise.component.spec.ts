import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetaisEntrepriseComponent } from './detais-entreprise.component';

describe('DetaisEntrepriseComponent', () => {
  let component: DetaisEntrepriseComponent;
  let fixture: ComponentFixture<DetaisEntrepriseComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetaisEntrepriseComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DetaisEntrepriseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
