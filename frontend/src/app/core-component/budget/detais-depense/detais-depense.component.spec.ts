import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetaisDepenseComponent } from './detais-depense.component';

describe('DetaisDepenseComponent', () => {
  let component: DetaisDepenseComponent;
  let fixture: ComponentFixture<DetaisDepenseComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetaisDepenseComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DetaisDepenseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
