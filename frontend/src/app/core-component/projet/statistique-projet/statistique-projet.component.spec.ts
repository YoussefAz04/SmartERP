import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatistiqueProjetComponent } from './statistique-projet.component';

describe('StatistiqueProjetComponent', () => {
  let component: StatistiqueProjetComponent;
  let fixture: ComponentFixture<StatistiqueProjetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StatistiqueProjetComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(StatistiqueProjetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
