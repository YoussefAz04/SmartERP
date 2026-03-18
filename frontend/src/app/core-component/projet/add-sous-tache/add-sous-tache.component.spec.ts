import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddSousTacheComponent } from './add-sous-tache.component';

describe('AddSousTacheComponent', () => {
  let component: AddSousTacheComponent;
  let fixture: ComponentFixture<AddSousTacheComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddSousTacheComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AddSousTacheComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
