import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { AdjustStockComponent } from './adjust-stock.component';

describe('AdjustStockComponent', () => {
  let component: AdjustStockComponent;
  let fixture: ComponentFixture<AdjustStockComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AdjustStockComponent,
        RouterTestingModule,
        HttpClientTestingModule
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdjustStockComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
