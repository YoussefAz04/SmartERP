import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListModuleAffecterComponent } from './list-module-affecter.component';

describe('ListModuleAffecterComponent', () => {
  let component: ListModuleAffecterComponent;
  let fixture: ComponentFixture<ListModuleAffecterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListModuleAffecterComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ListModuleAffecterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
