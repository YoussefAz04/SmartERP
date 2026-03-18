import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatistiqueTacheComponent } from './statistique-tache.component';

describe('StatistiqueTacheComponent', () => {
  let component: StatistiqueTacheComponent;
  let fixture: ComponentFixture<StatistiqueTacheComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StatistiqueTacheComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(StatistiqueTacheComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
