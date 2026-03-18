import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListSousTacheComponent } from './list-sous-tache.component';

describe('ListSousTacheComponent', () => {
  let component: ListSousTacheComponent;
  let fixture: ComponentFixture<ListSousTacheComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListSousTacheComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ListSousTacheComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
