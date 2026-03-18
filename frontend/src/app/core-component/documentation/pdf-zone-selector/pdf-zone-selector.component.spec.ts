import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PdfZoneSelectorComponent } from './pdf-zone-selector.component';

describe('PdfZoneSelectorComponent', () => {
  let component: PdfZoneSelectorComponent;
  let fixture: ComponentFixture<PdfZoneSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PdfZoneSelectorComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PdfZoneSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
