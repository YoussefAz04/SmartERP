import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddcategorieTacheComponent } from './addcategorie-tache.component';

describe('AddcategorieTacheComponent', () => {
  let component: AddcategorieTacheComponent;
  let fixture: ComponentFixture<AddcategorieTacheComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddcategorieTacheComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AddcategorieTacheComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
