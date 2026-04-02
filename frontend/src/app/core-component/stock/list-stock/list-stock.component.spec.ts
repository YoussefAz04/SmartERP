import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { ListStockComponent } from './list-stock.component';

describe('ListStockComponent', () => {
  let component: ListStockComponent;
  let fixture: ComponentFixture<ListStockComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ListStockComponent,
        RouterTestingModule,
        HttpClientTestingModule
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListStockComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
