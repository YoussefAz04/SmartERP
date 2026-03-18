import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KanbanTacheComponent } from './kanban-tache.component';

describe('KanbanTacheComponent', () => {
  let component: KanbanTacheComponent;
  let fixture: ComponentFixture<KanbanTacheComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KanbanTacheComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(KanbanTacheComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
