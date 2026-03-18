import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddModuleTittleComponent } from './add-module-tittle.component';

describe('AddModuleTittleComponent', () => {
  let component: AddModuleTittleComponent;
  let fixture: ComponentFixture<AddModuleTittleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddModuleTittleComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AddModuleTittleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
