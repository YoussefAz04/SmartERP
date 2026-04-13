import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ListfacturAchatComponent } from './listfactur-achat.component';
import { FactureService } from '../../../core/service/facture/facture.service';
import { Router } from '@angular/router';
import { of } from 'rxjs';

describe('ListfacturAchatComponent', () => {
  let component: ListfacturAchatComponent;
  let fixture: ComponentFixture<ListfacturAchatComponent>;
  let factureService: jasmine.SpyObj<FactureService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    // Create spy objects for services
    const factureServiceSpy = jasmine.createSpyObj('FactureService', [
      'getAllFactureAchat',
      'deleteFactureAchat'
    ]);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [ListfacturAchatComponent],
      providers: [
        { provide: FactureService, useValue: factureServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    })
    .compileComponents();
    
    factureService = TestBed.inject(FactureService) as jasmine.SpyObj<FactureService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    // Mock the service response
    factureService.getAllFactureAchat.and.returnValue(of([]));

    fixture = TestBed.createComponent(ListfacturAchatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with empty filtered list', () => {
    expect(component.filteredList).toEqual([]);
  });

  it('should load factures on init', () => {
    expect(factureService.getAllFactureAchat).toHaveBeenCalled();
  });

  it('should filter factures by search term', () => {
    component.factureAchatList = [
      { id: 1, referance: 'FAC001', dateAchat: '2024-01-01', montantTTC: 100, modePaiement: 'Virement', statut: 'Payée' } as any,
      { id: 2, referance: 'FAC002', dateAchat: '2024-01-02', montantTTC: 200, modePaiement: 'Chèque', statut: 'En attente' } as any
    ];
    
    component.onSearch('FAC001');
    expect(component.filteredList.length).toBe(1);
    expect(component.filteredList[0].referance).toBe('FAC001');
  });

  it('should sort factures by date descending', () => {
    component.factureAchatList = [
      { id: 1, referance: 'FAC001', dateAchat: '2024-01-01', montantTTC: 100, modePaiement: 'Virement', statut: 'Payée' } as any,
      { id: 2, referance: 'FAC002', dateAchat: '2024-01-02', montantTTC: 200, modePaiement: 'Chèque', statut: 'En attente' } as any
    ];
    
    component.applyFiltersAndSort();
    expect(component.filteredList[0].dateAchat).toBe('2024-01-02');
  });

  it('should select single facture', () => {
    component.toggleSelectFacture(1);
    expect(component.isFactureSelected(1)).toBe(true);
  });

  it('should select all factures', () => {
    component.filteredList = [
      { id: 1, referance: 'FAC001' } as any,
      { id: 2, referance: 'FAC002' } as any
    ];
    
    component.toggleSelectAll(true);
    expect(component.selectedFactures.size).toBe(2);
    expect(component.selectAll).toBe(true);
  });

  it('should deselect all factures', () => {
    component.selectedFactures.add(1);
    component.selectedFactures.add(2);
    
    component.toggleSelectAll(false);
    expect(component.selectedFactures.size).toBe(0);
    expect(component.selectAll).toBe(false);
  });

  it('should navigate to facture details', () => {
    component.viewFacture(1);
    expect(router.navigate).toHaveBeenCalledWith(['facture/detaisFactureAchat', 1]);
  });

  it('should delete facture when confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    factureService.deleteFactureAchat.and.returnValue(of({}));
    
    component.factureAchatList = [
      { id: 1, referance: 'FAC001' } as any,
      { id: 2, referance: 'FAC002' } as any
    ];
    
    component.deleteFacture(1);
    
    expect(factureService.deleteFactureAchat).toHaveBeenCalledWith(1);
  });
});