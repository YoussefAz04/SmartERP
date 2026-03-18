import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListTachesProjetComponent } from './list-taches-projet.component';

describe('ListTachesProjetComponent', () => {
  let component: ListTachesProjetComponent;
  let fixture: ComponentFixture<ListTachesProjetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListTachesProjetComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ListTachesProjetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
