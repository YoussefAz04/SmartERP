import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListCategorieProjetComponent } from './list-categorie-projet.component';

describe('ListCategorieProjetComponent', () => {
  let component: ListCategorieProjetComponent;
  let fixture: ComponentFixture<ListCategorieProjetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListCategorieProjetComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ListCategorieProjetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
