import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddEquipeComponent } from './add-equipe.component';

describe('AddEquipeComponent', () => {
  let component: AddEquipeComponent;
  let fixture: ComponentFixture<AddEquipeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddEquipeComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AddEquipeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
