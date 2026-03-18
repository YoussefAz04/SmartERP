import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListcategorieTacheComponent } from './listcategorie-tache.component';

describe('ListcategorieTacheComponent', () => {
  let component: ListcategorieTacheComponent;
  let fixture: ComponentFixture<ListcategorieTacheComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListcategorieTacheComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ListcategorieTacheComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
