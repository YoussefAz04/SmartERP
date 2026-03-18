import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddProjetComponent } from './add-projet.component';

describe('AddProjetComponent', () => {
  let component: AddProjetComponent;
  let fixture: ComponentFixture<AddProjetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddProjetComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AddProjetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
