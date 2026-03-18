import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MembreListComponent } from './membre-list.component';

describe('MembreListComponent', () => {
  let component: MembreListComponent;
  let fixture: ComponentFixture<MembreListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MembreListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(MembreListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
