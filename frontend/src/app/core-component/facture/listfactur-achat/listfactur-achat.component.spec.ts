import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListfacturAchatComponent } from './listfactur-achat.component';

describe('ListfacturAchatComponent', () => {
  let component: ListfacturAchatComponent;
  let fixture: ComponentFixture<ListfacturAchatComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListfacturAchatComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ListfacturAchatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
