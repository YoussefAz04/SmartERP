import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListSousModuleComponent } from './list-sous-module.component';

describe('ListSousModuleComponent', () => {
  let component: ListSousModuleComponent;
  let fixture: ComponentFixture<ListSousModuleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListSousModuleComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ListSousModuleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
