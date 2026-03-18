import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetaisTacheComponent } from './detais-tache.component';

describe('DetaisTacheComponent', () => {
  let component: DetaisTacheComponent;
  let fixture: ComponentFixture<DetaisTacheComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetaisTacheComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DetaisTacheComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
