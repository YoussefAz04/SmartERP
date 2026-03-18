import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpadateTacheComponent } from './upadate-tache.component';

describe('UpadateTacheComponent', () => {
  let component: UpadateTacheComponent;
  let fixture: ComponentFixture<UpadateTacheComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpadateTacheComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(UpadateTacheComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
