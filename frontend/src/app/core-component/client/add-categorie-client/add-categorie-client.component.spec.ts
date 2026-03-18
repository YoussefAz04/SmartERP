import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddCategorieClientComponent } from './add-categorie-client.component';

describe('AddCategorieClientComponent', () => {
  let component: AddCategorieClientComponent;
  let fixture: ComponentFixture<AddCategorieClientComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddCategorieClientComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AddCategorieClientComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
