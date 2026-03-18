import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddDepenseComponent } from './add-depense.component';

describe('AddDepenseComponent', () => {
  let component: AddDepenseComponent;
  let fixture: ComponentFixture<AddDepenseComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddDepenseComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AddDepenseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
