import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpadateProjetComponent } from './upadate-projet.component';

describe('UpadateProjetComponent', () => {
  let component: UpadateProjetComponent;
  let fixture: ComponentFixture<UpadateProjetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpadateProjetComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(UpadateProjetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
