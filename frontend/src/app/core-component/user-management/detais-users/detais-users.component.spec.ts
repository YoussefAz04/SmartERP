import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetaisUsersComponent } from './detais-users.component';

describe('DetaisUsersComponent', () => {
  let component: DetaisUsersComponent;
  let fixture: ComponentFixture<DetaisUsersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetaisUsersComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DetaisUsersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
