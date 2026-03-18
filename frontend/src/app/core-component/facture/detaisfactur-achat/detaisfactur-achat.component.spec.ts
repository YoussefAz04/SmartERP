import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetaisfacturAchatComponent } from './detaisfactur-achat.component';

describe('DetaisfacturAchatComponent', () => {
  let component: DetaisfacturAchatComponent;
  let fixture: ComponentFixture<DetaisfacturAchatComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetaisfacturAchatComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DetaisfacturAchatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
