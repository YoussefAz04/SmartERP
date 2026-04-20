import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { provideRouter } from '@angular/router';

import { ClientComponent } from './client.component';
import { ClientService } from '../../core/service/client/client.service';
import { NotificationService } from '../../shared/notification/notification.service';

describe('ClientComponent', () => {
  let component: ClientComponent;
  let fixture: ComponentFixture<ClientComponent>;
  let clientServiceSpy: jasmine.SpyObj<ClientService>;
  let notificationSpy: jasmine.SpyObj<NotificationService>;

  beforeEach(async () => {
    clientServiceSpy = jasmine.createSpyObj<ClientService>('ClientService', ['getAllClients', 'deleteClient']);
    notificationSpy = jasmine.createSpyObj<NotificationService>('NotificationService', ['error', 'success', 'warning', 'confirm']);
    clientServiceSpy.getAllClients.and.returnValue(of([]));
    notificationSpy.confirm.and.returnValue(Promise.resolve(false));

    await TestBed.configureTestingModule({
      imports: [ClientComponent],
      providers: [
        provideRouter([]),
        { provide: ClientService, useValue: clientServiceSpy },
        { provide: NotificationService, useValue: notificationSpy }
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ClientComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
