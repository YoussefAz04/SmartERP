import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetaisDocumentComponent } from './detais-document.component';

describe('DetaisDocumentComponent', () => {
  let component: DetaisDocumentComponent;
  let fixture: ComponentFixture<DetaisDocumentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetaisDocumentComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DetaisDocumentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
