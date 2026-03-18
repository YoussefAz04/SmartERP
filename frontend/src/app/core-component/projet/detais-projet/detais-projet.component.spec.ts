import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetaisProjetComponent } from './detais-projet.component';

describe('DetaisProjetComponent', () => {
  let component: DetaisProjetComponent;
  let fixture: ComponentFixture<DetaisProjetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetaisProjetComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DetaisProjetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
