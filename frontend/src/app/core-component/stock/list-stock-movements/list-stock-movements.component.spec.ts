import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { ListStockMovementsComponent } from './list-stock-movements.component';

describe('ListStockMovementsComponent', () => {
  let component: ListStockMovementsComponent;
  let fixture: ComponentFixture<ListStockMovementsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ListStockMovementsComponent,
        RouterTestingModule,
        HttpClientTestingModule
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListStockMovementsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
