import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddCategorieProjetComponent } from './add-categorie-projet.component';

describe('AddCategorieProjetComponent', () => {
  let component: AddCategorieProjetComponent;
  let fixture: ComponentFixture<AddCategorieProjetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddCategorieProjetComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AddCategorieProjetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
