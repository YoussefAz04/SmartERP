import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetaisSousTacheComponent } from './detais-sous-tache.component';

describe('DetaisSousTacheComponent', () => {
  let component: DetaisSousTacheComponent;
  let fixture: ComponentFixture<DetaisSousTacheComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetaisSousTacheComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DetaisSousTacheComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
