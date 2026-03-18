import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddSousModuleComponent } from './add-sous-module.component';

describe('AddSousModuleComponent', () => {
  let component: AddSousModuleComponent;
  let fixture: ComponentFixture<AddSousModuleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddSousModuleComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AddSousModuleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
