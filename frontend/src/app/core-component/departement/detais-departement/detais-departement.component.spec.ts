import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetaisDepartementComponent } from './detais-departement.component';

describe('DetaisDepartementComponent', () => {
  let component: DetaisDepartementComponent;
  let fixture: ComponentFixture<DetaisDepartementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetaisDepartementComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DetaisDepartementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
