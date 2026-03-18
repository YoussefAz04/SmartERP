import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListCategorieClientComponent } from './list-categorie-client.component';

describe('ListCategorieClientComponent', () => {
  let component: ListCategorieClientComponent;
  let fixture: ComponentFixture<ListCategorieClientComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListCategorieClientComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ListCategorieClientComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
