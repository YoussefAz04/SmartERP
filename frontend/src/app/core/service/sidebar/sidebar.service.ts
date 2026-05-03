import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import {BehaviorSubject, Observable} from 'rxjs';
import { routes } from '../../core.index';
import {HttpClient} from "@angular/common/http";
import {ModuleTittle} from "../../../model/moduleTittle.model";
import {SousModule} from "../../../model/sousModule.model";
import {Module} from "../../../model/module.model";

@Injectable({
  providedIn: 'root',
})
export class SidebarService {
  private dataSubject = new BehaviorSubject<unknown>(null);
  data$ = this.dataSubject.asObservable();
  sidebarData1: any[] = [];
  private sidebarDataSubject = new BehaviorSubject<any[]>([]);
  sidebarData$ = this.sidebarDataSubject.asObservable();



  private baseUrl = `${environment.apiUrl}/module/getAllModulesTittle`;
  constructor(private http: HttpClient) {

  }


  private collapseSubject = new BehaviorSubject<boolean>(false);
  collapse$ = this.collapseSubject.asObservable();

  toggleCollapse() {
    this.collapseSubject.next(!this.collapseSubject.value);
  }

  public sideBarPosition: BehaviorSubject<string> = new BehaviorSubject<string>(
    localStorage.getItem('sideBarPosition') || 'true'
  );

  public toggleMobileSideBar: BehaviorSubject<string> =
    new BehaviorSubject<string>(
      localStorage.getItem('isMobileSidebar') || 'false'
    );

  public expandSideBar: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(
    false
  );

  public switchSideMenuPosition(): void {
    if (localStorage.getItem('sideBarPosition')) {
      this.sideBarPosition.next('false');
      this.expandSideBar.next(true);
      localStorage.removeItem('sideBarPosition');
    } else {
      this.sideBarPosition.next('true');
      this.expandSideBar.next(false);
      localStorage.setItem('sideBarPosition', 'true');
    }
  }

  public switchMobileSideBarPosition(): void {
    if (localStorage.getItem('isMobileSidebar')) {
      this.toggleMobileSideBar.next('false');
      localStorage.removeItem('isMobileSidebar');
    } else {
      this.toggleMobileSideBar.next('true');
      localStorage.setItem('isMobileSidebar', 'true');
    }
  }

  getAllModuleTitles(): Observable<ModuleTittle[]> {
    return this.http.get<ModuleTittle[]>(`${this.baseUrl}`);
  }

  /** Parses and returns the session object stored in localStorage, or null on error. */
  private getSession(): any {
    try {
      const raw = localStorage.getItem('myLSkey');
      if (!raw) return null;
      return JSON.parse(decodeURIComponent(escape(atob(raw))));
    } catch {
      return null;
    }
  }

  /** Returns the current user's role: 'SUPER_ADMIN', 'ADMIN', or 'EMPLOYEE'. */
  getUserRole(): string {
    const session = this.getSession();
    if (!session) return 'EMPLOYEE';
    const roles: string[] = Array.isArray(session.roles) ? session.roles : [];
    if (roles.includes('SUPER_ADMIN')) return 'SUPER_ADMIN';
    if (roles.includes('ADMIN')) return 'ADMIN';
    return 'EMPLOYEE';
  }

  /**
   * Returns the set of module IDs that the current EMPLOYEE user has access to,
   * based on the permissions stored in the session. Only modules where the user
   * has been granted `view: true` are returned — modules with only create/update/
   * delete (and no view) are intentionally hidden from the sidebar so the user is
   * never directed to a page they cannot read.
   */
  private getUserPermissionModuleIds(): Set<number> {
    const session = this.getSession();
    if (!session) return new Set();
    const permissions: any[] = Array.isArray(session.permission) ? session.permission : [];
    return new Set(
      permissions
        .filter((p: any) => p && p.view === true && p.moduleId != null)
        .map((p: any) => Number(p.moduleId))
    );
  }


  updateData(data: unknown) {
    this.dataSubject.next(data);
  }


generateSidebarData(modules: ModuleTittle[]) {
    const userRole = this.getUserRole();
    const permittedModuleIds = this.getUserPermissionModuleIds();

    // Strips accents and lowercases a string for accent-insensitive comparisons
    const normalize = (s: string) =>
      s.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();

    // titleMap: maps ModuleTittle.moduleTittle (section-level DB names) to display labels
    const titleMap: { [key: string]: string } = {
      'Gestion Article achat': "Les Articles d'Achats",
      'Gestion Article vente': 'Les Articles de Ventes',
      'Gestion Stock': 'Gestion des Stocks',
      'Gestion Fournisseur': 'Les Fournisseurs',
      'gestion de Client': 'Gestion des Clients',
      'gestion de Fournisseur': 'Gestion des Fournisseurs',
      'gestion Article': 'Gestion des Articles',
      'gestion Achat': 'Gestion des Achats',
      'gestion Vente': 'Gestion des Ventes',
      'gestion Comptabilite': 'Gestion de la Comptabilité',
      'gestion Document': 'Gestion des Documents',
      'Parametrage': 'Paramétrage',
    };

    // menuValueMap: maps Module.title (item-level DB names) to display labels
    const menuValueMap: { [key: string]: string } = {
      'Liste Article': 'Liste des Articles',
      'gestion de Client': 'Clients',
      'gestion de Categorie Client': 'Catégories Clients',
      'gestion de Fournisseur': 'Fournisseurs',
      'gestion de Categorie Fournisseur': 'Catégories Fournisseurs',
      'gestion Article Achat': 'Articles Achat',
      'gestion Article Vente': 'Articles Vente',
      'gestion Stock': 'Stock',
      'Commande Achat': 'Commandes Achat',
      'Commande Vente': 'Commandes Vente',
    };

    // iconMap: maps normalized Module.title to feather icon names for visual variety
    // Keys are pre-normalized (no accents, lowercase) — lookup uses normalize()
    const iconMap: { [key: string]: string } = {
      'liste article': 'package',
      'gestion de client': 'users',
      'gestion de categorie client': 'tag',
      'gestion de fournisseur': 'truck',
      'gestion de categorie fournisseur': 'tag',
      'gestion article achat': 'shopping-bag',
      'gestion article vente': 'shopping-cart',
      'gestion stock': 'box',
      'gestion stocks': 'box',
      'stock': 'box',
      'commande achat': 'file-text',
      'commandes achat': 'file-text',
      'commande vente': 'file-text',
      'commandes vente': 'file-text',
      'module achats': 'shopping-bag',
      'module ventes': 'shopping-cart',
      'facture achat': 'credit-card',
      'factures achats': 'credit-card',
      'facture vente': 'credit-card',
      'factures ventes': 'credit-card',
      'liste document': 'folder',
      'liste depense': 'dollar-sign',
      'users': 'users',
      'utilisateurs': 'users',
      'gestion utilisateur': 'users',
      'gestion utilisateurs': 'users',
      'gestion users': 'users',
      'gestion des utilisateurs': 'users',
      'entreprise': 'briefcase',
      'gestion entreprise': 'briefcase',
      'commande': 'file-text',
      'facture': 'credit-card',
      'depense': 'dollar-sign',
      'document': 'folder',
      'fournisseur': 'truck',
      'client': 'user',
      'article': 'package',
    };

    // Module sections to exclude entirely from the sidebar
    // Comparisons use normalize() (accent-insensitive, case-insensitive)
    const excludedModuleTitles = [
      'gestion module', 'gestion departement', 'departement', 'departemen',
      'bon de reception', 'bon reception', 'bons de reception', 'bons reception',
      'bon de livraison', 'bon livraison', 'bons de livraison', 'bons livraison',
    ];

    // Sous-module links to exclude from the sidebar
    const excludedSousModuleLinks = ['achat/listBonReception', 'vente/listBonLivraison'];

    // Shared list of titles to exclude at both the Module and SousModule levels
    // All values are pre-normalized — comparisons use normalize()
    // 'departemen' is kept intentionally as a DB misspelling variant of 'departement'
    const excludedItemTitles = [
      'bon reception', 'bon de reception', 'bons reception', 'bons de reception',
      'bon livraison', 'bon de livraison', 'bons livraison', 'bons de livraison',
      'categorie depense',
      'select zone',
      'departement', 'departemen',
      'module titre', 'module', 'sous module',
    ];

    const excludedSousModuleTitles = excludedItemTitles;
    const excludedModuleItemTitles = excludedItemTitles;

    // Keywords for partial-match exclusion (used at section, module, and sous-module levels)
    // All values are pre-normalized — comparisons use normalize()
    const excludedKeywords = [
      'departement', 'departemen',
      'bon de reception', 'bon reception', 'bons de reception', 'bons reception',
      'bon de livraison', 'bon livraison', 'bons de livraison', 'bons livraison',
    ];

    const filteredModules = modules.filter((moduleTitle: ModuleTittle) => {
      const title = normalize(moduleTitle.moduleTittle ?? '');
      // Exclude sections by exact match (accent-insensitive)
      if (excludedModuleTitles.some(excluded => title === normalize(excluded))) {
        return false;
      }
      // Also exclude sections containing these keywords (partial match, accent-insensitive)
      if (excludedKeywords.some(kw => title.includes(normalize(kw)))) {
        return false;
      }
      return true;
    });

    const data = filteredModules.map((moduleTitle: ModuleTittle) => {
      let activeModules: Module[];

      // Shared helper: returns true when a module item passes all exclusion filters.
      const isModuleAllowed = (mod: Module): boolean => {
        const t = normalize(mod.title ?? '');
        if (excludedModuleItemTitles.some(excluded => t === normalize(excluded))) return false;
        if (excludedKeywords.some(kw => t.includes(normalize(kw)))) return false;
        return true;
      };

      if (userRole === 'SUPER_ADMIN') {
        // SUPER_ADMIN is platform-level: all sections come from the hardcoded
        // "Administration Plateforme" block below. DB-sourced sections are hidden
        // entirely to avoid duplicate items (e.g. Gestion des Utilisateurs).
        activeModules = [];
      } else if (userRole === 'ADMIN') {
        // ADMIN (Enterprise Owner) sees all business modules
        activeModules = moduleTitle.modules.filter(isModuleAllowed);
      } else {
        // EMPLOYEE: show only the modules explicitly granted via permissions assigned
        // at employee creation. permittedModuleIds contains the Module IDs from the session.
        activeModules = moduleTitle.modules.filter(
          (mod: Module) => permittedModuleIds.has(mod.id) && isModuleAllowed(mod)
        );
      }

      return {
        tittle: titleMap[moduleTitle.moduleTittle] || moduleTitle.moduleTittle,
        showAsTab: false,
        separateRoute: false,
        hasSubRoute: activeModules.some((mod: Module) => mod.sousModules.length > 0),
        showSubRoute: true,
        menu: activeModules.map((mod: Module) => {
          const filteredSousModules = mod.sousModules.filter((sous: SousModule) => {
            const sousTitle = normalize(sous.titreSousModule ?? '');
            if (sous.link && excludedSousModuleLinks.includes(sous.link.trim())) return false;
            if (excludedSousModuleTitles.some(excluded => sousTitle === normalize(excluded))) return false;
            if (excludedKeywords.some(kw => sousTitle.includes(normalize(kw)))) return false;
            return true;
          });
          return {
            menuValue: menuValueMap[mod.title] || mod.title,
            hasSubRoute: filteredSousModules.length > 0,
            showSubRoute: false,
            icon: iconMap[normalize(mod.title ?? '')] || 'grid',
            base1: 'managementEntreprise',
            subMenus: filteredSousModules.map((sous: SousModule) => ({
              menuValue: sous.titreSousModule,
              route: sous.link,
              hasSubRoute: false,
              showSubRoute: false,
              customSubmenuTwo: false,
            }))
          };
        }).filter((mod: any) => mod.subMenus.length > 0 || !mod.hasSubRoute)
      };
    }).filter((section: any) => section.menu && section.menu.length > 0);

    // AI Intelligence section is only visible to ADMIN (enterprise-level), not SUPER_ADMIN or EMPLOYEE
    const result: any[] = [...data];
    if (userRole === 'ADMIN') {
      result.push({
        tittle: 'Intelligence Artificielle',
        showAsTab: false,
        separateRoute: false,
        hasSubRoute: false,
        showSubRoute: true,
        menu: [
          {
            menuValue: 'Intelligence Prédictive',
            hasSubRoute: false,
            showSubRoute: false,
            icon: 'cpu',
            base1: 'ai-intelligence',
            route: 'ai-intelligence',
            subMenus: []
          }
        ]
      });
    }

    // Platform Administration section is only visible to SUPER_ADMIN
    if (userRole === 'SUPER_ADMIN') {
      result.push({
        tittle: 'Administration Plateforme',
        showAsTab: false,
        separateRoute: false,
        hasSubRoute: false,
        showSubRoute: true,
        menu: [
          {
            menuValue: 'Gestion des Entreprises',
            hasSubRoute: false,
            showSubRoute: false,
            icon: 'briefcase',
            route: 'entreprise',
            base1: 'entreprise',
            subMenus: []
          },
          {
            menuValue: 'Gestion des Utilisateurs',
            hasSubRoute: false,
            showSubRoute: false,
            icon: 'users',
            route: 'user-management',
            base1: 'user-management',
            subMenus: []
          },
          {
            menuValue: 'Rôles & Permissions',
            hasSubRoute: false,
            showSubRoute: false,
            icon: 'shield',
            route: 'user-management/roles-permissions',
            base1: 'user-management',
            subMenus: []
          }
        ]
      });
    }
    this.sidebarDataSubject.next(result);
  }

  /** Returns a feather icon name for a sidebar section based on its title. */
  public getSectionFeatherIcon(title: string): string {
    const t = (title || '').toLowerCase();
    if (t.includes('article') || t.includes('stock')) return 'package';
    if (t.includes('client')) return 'users';
    if (t.includes('fournisseur')) return 'truck';
    if (t.includes('commande')) return 'file-text';
    if (t.includes('facture')) return 'credit-card';
    if (t.includes('budget') || t.includes('comptab')) return 'dollar-sign';
    if (t.includes('ia') || t.includes('intellig')) return 'cpu';
    if (t.includes('admin') || t.includes('platform') || t.includes('param')) return 'settings';
    if (t.includes('document')) return 'folder';
    if (t.includes('projet')) return 'briefcase';
    return 'grid';
  }

  // public sidebarData1 = [
  //   {
  //     tittle: 'Main',
  //     showAsTab: false,
  //     separateRoute: false,
  //     hasSubRoute: false,
  //     showSubRoute: true,
  //     menu: [
  //       {
  //         menuValue:  this.data$.subscribe(data => {
  //           console.log('Modules partagés :', data);
  //         }),
  //         hasSubRoute: true,
  //         showSubRoute: true,
  //         icon: 'grid',
  //         base1: 'managementEntreprise',
  //
  //       },
  //       {
  //         menuValue: 'Application',
  //         hasSubRouteTwo: true,
  //         showSubRoute: false,
  //         base: 'application',
  //         icon: 'smartphone',
  //         subMenus: [
  //           {
  //             menuValue: 'Chat',
  //             route: routes.chat,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //             customSubmenuTwo: false,
  //           },
  //           {
  //             menuValue: 'Call',
  //             customSubmenuTwo: true,
  //             hasSubRoute: true,
  //             showSubRoute: false,
  //             route: routes.calendar,
  //             page1: 'video-call',
  //             page2: 'audio-call',
  //             page3: 'call-history',
  //             subMenusTwo: [
  //               {
  //                 menuValue: 'Video Call',
  //                 route: routes.videoCall,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //                 page: 'call',
  //               },
  //               {
  //                 menuValue: 'Audio Call',
  //                 route: routes.audioCall,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //               {
  //                 menuValue: 'Call History',
  //                 route: routes.callHistory,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //             ],
  //           },
  //           {
  //             menuValue: 'Calendar',
  //             route: routes.calendar,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //             customSubmenuTwo: false,
  //           },
  //           {
  //             menuValue: 'Email',
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //             route: routes.email,
  //             customSubmenuTwo: false,
  //             subRoutes: [],
  //           },
  //           {
  //             menuValue: 'To Do',
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //             route: routes.toDo,
  //             customSubmenuTwo: false,
  //             subRoutes: [],
  //           },
  //           {
  //             menuValue: 'Notes',
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //             route: routes.notes,
  //             customSubmenuTwo: false,
  //             subRoutes: [],
  //           },
  //           {
  //             menuValue: 'File Manager',
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //             route: routes.fileManager,
  //             customSubmenuTwo: false,
  //             subRoutes: [],
  //           },
  //         ],
  //       },
  //     ],
  //   },
  //   {
  //     tittle: 'Inventory',
  //     showAsTab: true,
  //     separateRoute: false,
  //     menu: [
  //       {
  //         menuValue: 'Products',
  //         icon: 'box',
  //         route: routes.productList,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Create Product',
  //         icon: 'plus-square',
  //         route: routes.addProduct,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Expired Products',
  //         icon: 'message-square',
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //         route: routes.expiredProduct,
  //         subRoutes: [],
  //       },
  //       {
  //         menuValue: 'Low Stocks',
  //         icon: 'trending-down',
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //         route: routes.lowStock,
  //         subRoutes: [],
  //       },
  //       {
  //         menuValue: 'Category',
  //         icon: 'codepen',
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //         route: routes.categoryList,
  //         subRoutes: [],
  //       },
  //       {
  //         menuValue: 'Sub Category',
  //         icon: 'speaker',
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //         route: routes.subCategories,
  //         subRoutes: [],
  //       },
  //       {
  //         menuValue: 'Brands',
  //         icon: 'tag',
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //         route: routes.brandList,
  //         subRoutes: [],
  //       },
  //       {
  //         menuValue: 'Units',
  //         icon: 'speaker',
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //         route: routes.units,
  //         subRoutes: [],
  //       },
  //       {
  //         menuValue: 'Variant Attributes',
  //         icon: 'layers',
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //         route: routes.varriantAttributes,
  //         subRoutes: [],
  //       },
  //       {
  //         menuValue: 'Warranties',
  //         icon: 'bookmark',
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //         route: routes.warranty,
  //         subRoutes: [],
  //       },
  //       {
  //         menuValue: 'Print Barcode',
  //         icon: 'align-justify',
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //         route: routes.barCode,
  //         subRoutes: [],
  //       },
  //       {
  //         menuValue: 'Print QR Code',
  //         icon: 'maximize',
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //         route: routes.qrCode,
  //         subRoutes: [],
  //       },
  //     ],
  //   },
  //   {
  //     tittle: 'Stock',
  //     showAsTab: true,
  //     separateRoute: false,
  //     menu: [
  //       {
  //         menuValue: 'Manage Stock',
  //         icon: 'package',
  //         route: routes.manageStocks,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Stock Adjustment',
  //         icon: 'clipboard',
  //         route: routes.stockAdjustment,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Stock Transfer',
  //         icon: 'truck',
  //         route: routes.stockTransfer,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //     ],
  //   },
  //
  //   {
  //     tittle: 'Sales',
  //     showAsTab: true,
  //     separateRoute: false,
  //     menu: [
  //       {
  //         menuValue: 'Sales',
  //         icon: 'shopping-cart',
  //         route: routes.salesList,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Sales Return',
  //         icon: 'copy',
  //         route: routes.salesReturn,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Quotation',
  //         icon: 'save',
  //         route: routes.quotationList,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'POS',
  //         icon: 'hard-drive',
  //         route: routes.pos,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //     ],
  //   },
  //
  //   {
  //     tittle: 'Promo',
  //     showAsTab: true,
  //     separateRoute: false,
  //     menu: [
  //       {
  //         menuValue: 'Coupons',
  //         icon: 'shopping-cart',
  //         route: routes.coupons,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //     ],
  //   },
  //   {
  //     tittle: 'Purchases',
  //     showAsTab: true,
  //     separateRoute: false,
  //     menu: [
  //       {
  //         menuValue: 'Purchases',
  //         icon: 'shopping-bag',
  //         route: routes.purchaseList,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Purchase Order',
  //         icon: 'file-minus',
  //         route: routes.purchaseOrderReport,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Purchase Return',
  //         icon: 'refresh-cw',
  //         route: routes.purchaseReturns,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //     ],
  //   },
  //
  //   {
  //     tittle: 'Finance & Accounts',
  //     showAsTab: true,
  //     separateRoute: false,
  //     menu: [
  //       {
  //         menuValue: 'Expenses',
  //         base: 'expense',
  //         icon: 'file-text',
  //         hasSubRoute: true,
  //         showSubRoute: false,
  //         subMenus: [
  //           {
  //             menuValue: 'Expenses',
  //             route: routes.expenseList,
  //           },
  //           {
  //             menuValue: 'Expense Category',
  //             route: routes.expenseCategory,
  //           },
  //         ],
  //       },
  //     ],
  //   },
  //
  //   {
  //     tittle: 'Peoples',
  //     showAsTab: true,
  //     separateRoute: false,
  //     menu: [
  //       {
  //         menuValue: 'Customers',
  //         icon: 'user',
  //         route: routes.customers,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Suppliers',
  //         icon: 'users',
  //         route: routes.suppliers,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Stores',
  //         icon: 'home',
  //         route: routes.storeList,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Warehouses',
  //         icon: 'archive',
  //         route: routes.wareHouse,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //     ],
  //   },
  //   {
  //     tittle: 'HRM',
  //     showAsTab: true,
  //     separateRoute: false,
  //     menu: [
  //       {
  //         menuValue: 'Employees',
  //         icon: 'user',
  //         route: routes.employeesGrid,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Departments',
  //         icon: 'users',
  //         route: routes.departmentGrid,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Designation',
  //         icon: 'git-merge',
  //         route: routes.designation,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Shifts',
  //         icon: 'shuffle',
  //         route: routes.shift,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Attendance',
  //         icon: 'book-open',
  //         page: 'attendance',
  //         hasSubRoute: true,
  //         showSubRoute: false,
  //         subMenus: [
  //           {
  //             menuValue: 'Employee',
  //             route: routes.attendanceEmployee,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Admin',
  //             route: routes.attendanceAdmin,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //         ],
  //       },
  //       {
  //         menuValue: 'Leaves',
  //         icon: 'calendar',
  //         page: 'leaves',
  //         hasSubRoute: true,
  //         showSubRoute: false,
  //         subMenus: [
  //           {
  //             menuValue: 'Admin Leaves',
  //             route: routes.leavesAdmin,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Employee Leaves',
  //             route: routes.leavesEmployee,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Leave Types',
  //             route: routes.leavesType,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //         ],
  //       },
  //       {
  //         menuValue: 'Holidays',
  //         icon: 'credit-card',
  //         route: routes.holidays,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Payroll',
  //         icon: 'dollar-sign',
  //         page: 'payroll',
  //         hasSubRoute: true,
  //         showSubRoute: false,
  //         subMenus: [
  //           {
  //             menuValue: 'Employee Salary',
  //             route: routes.payrollList,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Payslip',
  //             route: routes.paySlip,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //         ],
  //       },
  //     ],
  //   },
  //   {
  //     tittle: 'Reports',
  //     showAsTab: true,
  //     separateRoute: false,
  //     menu: [
  //       {
  //         menuValue: 'Sales Report',
  //         icon: 'bar-chart-2',
  //         route: routes.salesReport,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Purchase Report',
  //         icon: 'pie-chart',
  //         route: routes.purchaseReport,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Inventory Report',
  //         icon: 'inbox',
  //         route: routes.inventoryReport,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Invoice Report',
  //         icon: 'file',
  //         route: routes.invoiceReport,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Supplier Report',
  //         icon: 'user-check',
  //         route: routes.supplierReport,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Customer Report',
  //         icon: 'user',
  //         route: routes.customerReport,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Expense Report',
  //         icon: 'file',
  //         route: routes.expenseReport,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Income Report',
  //         icon: 'bar-chart',
  //         route: routes.incomeReport,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Tax Report',
  //         icon: 'database',
  //         route: routes.taxReport,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Profit & Loss',
  //         icon: 'pie-chart',
  //         route: routes.profitAndLoss,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //     ],
  //   },
  //   {
  //     tittle: 'User Management',
  //     showAsTab: true,
  //     separateRoute: false,
  //     menu: [
  //       {
  //         menuValue: 'Users',
  //         icon: 'user-check',
  //         route: routes.users,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Roles & Permissions',
  //         icon: 'shield',
  //         route: routes.rolesPermission,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Delete Account Request',
  //         icon: 'lock',
  //         route: routes.deleteaccount,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //     ],
  //   },
  //   {
  //     tittle: 'Pages',
  //     showAsTab: true,
  //     separateRoute: false,
  //     menu: [
  //       {
  //         menuValue: 'Profile',
  //         icon: 'user',
  //         route: routes.profile,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Authentication',
  //         icon: 'shield',
  //         hasSubRouteTwo: true,
  //         showSubRoute: false,
  //         subMenus: [
  //           {
  //             menuValue: 'Login',
  //             customSubmenuTwo: true,
  //             hasSubRoute: true,
  //             showSubRoute: false,
  //             subMenusTwo: [
  //               {
  //                 menuValue: 'Cover',
  //                 route: routes.signIn,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //               {
  //                 menuValue: 'Illustration',
  //                 route: routes.signIn2,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //               {
  //                 menuValue: 'Basic',
  //                 route: routes.signIn3,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //             ],
  //           },
  //           {
  //             menuValue: 'Register',
  //             customSubmenuTwo: true,
  //             hasSubRoute: true,
  //             showSubRoute: false,
  //             subMenusTwo: [
  //               {
  //                 menuValue: 'Cover',
  //                 route: routes.register,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //               {
  //                 menuValue: 'Illustration',
  //                 route: routes.register2,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //               {
  //                 menuValue: 'Basic',
  //                 route: routes.register3,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //             ],
  //           },
  //           {
  //             menuValue: 'Forgot Password',
  //             customSubmenuTwo: true,
  //             hasSubRoute: true,
  //             showSubRoute: false,
  //             subMenusTwo: [
  //               {
  //                 menuValue: 'Cover',
  //                 route: routes.forgotPassword,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //               {
  //                 menuValue: 'Illustration',
  //                 route: routes.forgotPassword2,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //               {
  //                 menuValue: 'Basic',
  //                 route: routes.forgotPassword3,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //             ],
  //           },
  //           {
  //             menuValue: 'Reset Password',
  //             customSubmenuTwo: true,
  //             hasSubRoute: true,
  //             showSubRoute: false,
  //             subMenusTwo: [
  //               {
  //                 menuValue: 'Cover',
  //                 route: routes.resetPassword,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //               {
  //                 menuValue: 'Illustration',
  //                 route: routes.resetPassword2,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //               {
  //                 menuValue: 'Basic',
  //                 route: routes.resetPassword3,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //             ],
  //           },
  //           {
  //             menuValue: 'Email Verification',
  //             customSubmenuTwo: true,
  //             hasSubRoute: true,
  //             showSubRoute: false,
  //             subMenusTwo: [
  //               {
  //                 menuValue: 'Cover',
  //                 route: routes.emailVerification,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //               {
  //                 menuValue: 'Illustration',
  //                 route: routes.emailVerification2,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //               {
  //                 menuValue: 'Basic',
  //                 route: routes.emailVerification3,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //             ],
  //           },
  //           {
  //             menuValue: '2 Step Verification',
  //             customSubmenuTwo: true,
  //             hasSubRoute: true,
  //             showSubRoute: false,
  //             subMenusTwo: [
  //               {
  //                 menuValue: 'Cover',
  //                 route: routes.twoStepVerification,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //               {
  //                 menuValue: 'Illustration',
  //                 route: routes.twoStepVerification2,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //               {
  //                 menuValue: 'Basic',
  //                 route: routes.twoStepVerification3,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //             ],
  //           },
  //           {
  //             menuValue: 'Lock Screen',
  //             route: routes.lockScreen,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //             customSubmenuTwo: false,
  //           },
  //         ],
  //       },
  //       {
  //         menuValue: 'Error Pages',
  //         icon: 'file-minus',
  //         hasSubRoute: true,
  //         showSubRoute: false,
  //         subMenus: [
  //           {
  //             menuValue: '404 Error',
  //             route: routes.error404,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: '500 Error',
  //             route: routes.error500,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //         ],
  //       },
  //       {
  //         menuValue: 'Places',
  //         page: 'places',
  //         icon: 'map',
  //         hasSubRoute: true,
  //         showSubRoute: false,
  //         subMenus: [
  //           {
  //             menuValue: 'Countries',
  //             route: routes.countries,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'States',
  //             route: routes.states,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //         ],
  //       },
  //       {
  //         menuValue: 'Blank Page',
  //         icon: 'file',
  //         route: routes.blankPage,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Coming Soon',
  //         icon: 'send',
  //         route: routes.comingSoon,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Under Maintenance',
  //         icon: 'alert-triangle',
  //         route: routes.underMaintanance,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //     ],
  //   },
  //   {
  //     tittle: 'Settings',
  //     showAsTab: true,
  //     separateRoute: false,
  //     menu: [
  //       {
  //         menuValue: 'General Settings',
  //         icon: 'settings',
  //         hasSubRoute: true,
  //         showSubRoute: false,
  //         page: 'general-settings',
  //         subMenus: [
  //           {
  //             menuValue: 'Profile',
  //             route: routes.generalSettings,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Security',
  //             route: routes.securitySettings,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Notifications',
  //             route: routes.settingsNotification,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Connected Apps',
  //             route: routes.connectedApps,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //         ],
  //       },
  //       {
  //         menuValue: 'Website Settings',
  //         page: 'website-settings',
  //         icon: 'globe',
  //         hasSubRoute: true,
  //         showSubRoute: false,
  //         subMenus: [
  //           {
  //             menuValue: 'System Settings',
  //             route: routes.systemSettings,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Company Settings',
  //             route: routes.companySettings,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Localization',
  //             route: routes.localizationSettings,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Prefixes',
  //             route: routes.prefixes,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Preference',
  //             route: routes.preference,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Appearance',
  //             route: routes.appearance,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Social Authentication',
  //             route: routes.socialAuthentication,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Language',
  //             route: routes.languageSettings,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //         ],
  //       },
  //       {
  //         menuValue: 'App Settings',
  //         page: 'app-settings',
  //         icon: 'smartphone',
  //         hasSubRoute: true,
  //         showSubRoute: false,
  //         base: 'app-settings',
  //         subMenus: [
  //           {
  //             menuValue: 'Invoice',
  //             route: routes.invoiceSettings,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Printer',
  //             route: routes.printerSettings,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'POS',
  //             route: routes.posSettings,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Custom Fields',
  //             route: routes.customFields,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //         ],
  //       },
  //       {
  //         menuValue: 'System Settings',
  //         page: 'system-settings',
  //         icon: 'monitor',
  //         hasSubRoute: true,
  //         showSubRoute: false,
  //         subMenus: [
  //           {
  //             menuValue: 'Email',
  //             route: routes.emailSettings,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'SMS Gateways',
  //             route: routes.smsGateway,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'OTP',
  //             route: routes.otpSettings,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'GDPR Cookies',
  //             route: routes.gdprSettings,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //         ],
  //       },
  //       {
  //         menuValue: 'Financial Settings',
  //         page: 'financial-settings',
  //         icon: 'dollar-sign',
  //         hasSubRoute: true,
  //         showSubRoute: false,
  //         subMenus: [
  //           {
  //             menuValue: 'Payment Gateway',
  //             route: routes.paymentGatewaySettings,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Bank Accounts',
  //             route: routes.bankSettingsGrid,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Tax Rates',
  //             route: routes.taxRates,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Currencies',
  //             route: routes.currencySettings,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //         ],
  //       },
  //       {
  //         menuValue: 'Other Settings',
  //         page: 'other-settings',
  //         icon: 'octagon',
  //         hasSubRoute: true,
  //         showSubRoute: false,
  //         subMenus: [
  //           {
  //             menuValue: 'Storage',
  //             route: routes.storageSettings,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Ban IP Address',
  //             route: routes.banIpAddress,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //         ],
  //       },
  //       {
  //         menuValue: 'Logout',
  //         icon: 'log-out',
  //         route: routes.signIn,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //     ],
  //   },
  //   {
  //     tittle: 'UI Interface',
  //     showAsTab: true,
  //     separateRoute: false,
  //     menu: [
  //       {
  //         menuValue: 'Base UI',
  //         base: 'base-ui',
  //         icon: 'layers',
  //         hasSubRoute: true,
  //         showSubRoute: false,
  //         subMenus: [
  //           {
  //             menuValue: 'Alerts',
  //             route: routes.uiAlerts,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Accordion',
  //             route: routes.uiAccordion,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Avatar',
  //             route: routes.uiAvatar,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Badges',
  //             route: routes.uiBadges,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Border',
  //             route: routes.uiBorders,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Buttons',
  //             route: routes.uiButtons,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Button Group',
  //             route: routes.uiButtonsGroup,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Breadcrumb',
  //             route: routes.uiBreadcrumb,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Card',
  //             route: routes.uiCards,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Carousel',
  //             route: routes.uiCarousel,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Colors',
  //             route: routes.uiColors,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Dropdowns',
  //             route: routes.uiDropdowns,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Grid',
  //             route: routes.uiGrid,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Images',
  //             route: routes.uiImages,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Lightbox',
  //             route: routes.uiLightbox,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Media',
  //             route: routes.uiMedia,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Modals',
  //             route: routes.uiModals,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Offcanvas',
  //             route: routes.uiOffcanvas,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Pagination',
  //             route: routes.uiPagination,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Popovers',
  //             route: routes.uiPopovers,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Progress',
  //             route: routes.uiProgress,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Placeholders',
  //             route: routes.uiPlaceholders,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Range Slider',
  //             route: routes.uiRangeSlider,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Spinner',
  //             route: routes.uiSpinner,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Sweet Alerts',
  //             route: routes.uiSweetAlerts,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Tabs',
  //             route: routes.uiNavTabs,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Toasts',
  //             route: routes.uiToasts,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Tooltips',
  //             route: routes.uiTooltips,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Typography',
  //             route: routes.uiTypography,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Video',
  //             route: routes.uiVideo,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //         ],
  //       },
  //       {
  //         menuValue: 'Advanced UI',
  //         base: 'advanced-ui',
  //         icon: 'activity',
  //         hasSubRoute: true,
  //         showSubRoute: false,
  //         subMenus: [
  //           {
  //             menuValue: 'Ribbon',
  //             route: routes.ribbon,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Clipboard',
  //             route: routes.clipboard,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Drag & Drop',
  //             route: routes.dragDrop,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Rating',
  //             route: routes.rating,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Text Editor',
  //             route: routes.textEditor,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Counter',
  //             route: routes.counter,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Scrollbar',
  //             route: routes.scrollbar,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //
  //           {
  //             menuValue: 'Timeline',
  //             route: routes.timeline,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //         ],
  //       },
  //       {
  //         menuValue: 'Charts',
  //         icon: 'bar-chart-2',
  //         hasSubRoute: true,
  //         showSubRoute: false,
  //         base: 'charts',
  //         subMenus: [
  //           {
  //             menuValue: 'Apex Charts',
  //             route: routes.chartApex,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Ng2 Charts',
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //             route: routes.chartNg2,
  //             subRoutes: [],
  //           },
  //           {
  //             menuValue: 'Prime NG Charts',
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //             route: routes.chartPrime,
  //             subRoutes: [],
  //           },
  //         ],
  //       },
  //       {
  //         menuValue: 'Icons',
  //         icon: 'database',
  //         hasSubRoute: true,
  //         showSubRoute: false,
  //         base: 'icons',
  //         subMenus: [
  //           {
  //             menuValue: 'Fontawesome Icons',
  //             route: routes.iconFontAwesome,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Feather Icons',
  //             route: routes.iconFeather,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Ionic Icons',
  //             route: routes.iconIonic,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Material Icons',
  //             route: routes.iconMaterial,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Pe7 Icons',
  //             route: routes.iconPe7,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Simpleline Icons',
  //             route: routes.iconSimpleline,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Themify Icons',
  //             route: routes.iconThemify,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Weather Icons',
  //             route: routes.iconWeather,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Typicon Icons',
  //             route: routes.iconTypicon,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Flag Icons',
  //             route: routes.iconFlag,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //         ],
  //       },
  //       {
  //         menuValue: 'Forms',
  //         icon: 'edit',
  //         base: 'forms',
  //         hasSubRouteTwo: true,
  //         showSubRoute: false,
  //         subMenus: [
  //           {
  //             menuValue: 'Form Elements',
  //             page1: 'form-basic-inputs',
  //             page2: 'form-checkbox-radios',
  //             page3: 'form-input-groups',
  //             page4: 'form-grid-gutters',
  //             page5: 'form-select',
  //             page6: 'form-mask',
  //             page7: 'form-fileupload',
  //             page8: 'form-elements',
  //             customSubmenuTwo: true,
  //             hasSubRoute: true,
  //             showSubRoute: false,
  //             subMenusTwo: [
  //               {
  //                 menuValue: 'Basic Inputs',
  //                 route: routes.formBasicInputs,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //               {
  //                 menuValue: 'Checkbox & Radios',
  //                 route: routes.formCheckboxRadios,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //               {
  //                 menuValue: 'Input Groups',
  //                 route: routes.formInputsGroups,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //               {
  //                 menuValue: 'Grid & Gutters',
  //                 route: routes.formGridGutters,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //               {
  //                 menuValue: 'Form Select',
  //                 route: routes.formSelect,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //               {
  //                 menuValue: 'Input Masks',
  //                 route: routes.formMask,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //               {
  //                 menuValue: 'File Uploads',
  //                 route: routes.formFileUpload,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //             ],
  //           },
  //           {
  //             menuValue: 'Layouts',
  //             customSubmenuTwo: true,
  //             page1: 'form-horizontal',
  //             page2: 'form-vertical',
  //             page3: 'form-floating-labels',
  //             hasSubRoute: true,
  //             showSubRoute: false,
  //             subMenusTwo: [
  //               {
  //                 menuValue: 'Horizontal Form',
  //                 route: routes.formHorizontal,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //               {
  //                 menuValue: 'Vertical Form',
  //                 route: routes.formVertical,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //               {
  //                 menuValue: 'Floating Labels',
  //                 route: routes.formFloatingLabels,
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //               },
  //             ],
  //           },
  //           {
  //             menuValue: 'Form Validation',
  //             route: routes.formValidation,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //             customSubmenuTwo: false,
  //           },
  //           {
  //             menuValue: 'Select2',
  //             route: routes.formSelect2,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //             customSubmenuTwo: false,
  //           },
  //           {
  //             menuValue: 'Form Wizard',
  //             route: routes.formWizard,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //             customSubmenuTwo: false,
  //           },
  //         ],
  //       },
  //       {
  //         menuValue: 'Tables',
  //         icon: 'layout',
  //         base: 'table',
  //         hasSubRoute: true,
  //         showSubRoute: false,
  //         subMenus: [
  //           {
  //             menuValue: 'Basic Tables',
  //             route: routes.basicTable,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //           {
  //             menuValue: 'Data Table',
  //             route: routes.dataTable,
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //           },
  //         ],
  //       },
  //     ],
  //   },
  //   {
  //     tittle: 'Help',
  //     showAsTab: true,
  //     separateRoute: false,
  //     menu: [
  //       {
  //         menuValue: 'Documentation',
  //         icon: 'file-text',
  //         // route: routes.chat,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Changelog v2.0.3',
  //         icon: 'lock',
  //         // route: routes.chat,
  //         hasSubRoute: false,
  //         showSubRoute: false,
  //       },
  //       {
  //         menuValue: 'Multi Level',
  //         hasSubRouteThree: true,
  //         icon: 'file-minus',
  //         showSubRoute: false,
  //         subMenus: [
  //           {
  //             menuValue: 'Level 1.1',
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //             customSubmenuThree: false,
  //           },
  //           {
  //             menuValue: 'Level 1.2',
  //             hasSubRoute: false,
  //             showSubRoute: false,
  //             customSubmenuThree: true,
  //             subMenusTwo: [
  //               {
  //                 menuValue: 'Level 2.1',
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //                 customSubmenuFour: false,
  //               },
  //               {
  //                 menuValue: 'Level 2.2',
  //                 hasSubRoute: false,
  //                 showSubRoute: false,
  //                 customSubmenuFour: true,
  //                 subMenusThree: [
  //                   {
  //                     menuValue: 'Level 3.1',
  //                     hasSubRoute: false,
  //                     showSubRoute: false,
  //                     customSubmenuFour: true,
  //                   },
  //                   {
  //                     menuValue: 'Level 3.2',
  //                     hasSubRoute: false,
  //                     showSubRoute: false,
  //                     customSubmenuFour: true,
  //                   },
  //                 ],
  //               },
  //             ],
  //           },
  //         ],
  //       },
  //     ],
  //   },
  // ];

  public sidebarData2 = [
    {
      tittle: 'Main Menu',
      hasSubRoute: true,
      icon: 'assets/img/icons/menu-icon.svg',
      showSubRoute: false,
      subRoutes: [
        {
          tittle: 'Dashboard',
          hasSubRoute: true,
          icon: 'assets/img/icons/dashboard.svg',
          showSubRoute: true,
          route: routes.dashboard,
          subRoutes: [
            {
              tittle: 'Admin Dashboard',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.adminDashboard,
              subRoutes: [],
            },
            {
              tittle: 'Sales Dashboard',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.salesDashboard,
              subRoutes: [],
            },
          ],
        },
        {
          tittle: 'Application',
          hasSubRoute: true,
          showSubRoute: true,
          base: 'application',
          subRoutes: [
            {
              tittle: 'Chat',
              route: routes.chat,
              hasSubRoute: false,
              showSubRoute: false,
              customSubmenuTwo: false,
            },
            {
              tittle: 'Call',
              hasSubRoute: true,
              showSubRoute: true,
              base: 'application',
              subRoutes: [
                {
                  tittle: 'Video Call',
                  route: routes.videoCall,
                  hasSubRoute: false,
                  showSubRoute: false,
                  subRoutes: [],
                },
                {
                  tittle: 'Audio Call',
                  route: routes.audioCall,
                  hasSubRoute: false,
                  showSubRoute: false,
                },
                {
                  tittle: 'Call History',
                  route: routes.callHistory,
                  hasSubRoute: false,
                  showSubRoute: false,
                },
              ],
            },
            {
              tittle: 'Calendar',
              route: routes.calendar,
              hasSubRoute: false,
              showSubRoute: false,
              customSubmenuTwo: false,
            },
            {
              tittle: 'Email',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.email,
              customSubmenuTwo: false,
              subRoutes: [],
            },
            {
              tittle: 'To Do',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.toDo,
              customSubmenuTwo: false,
              subRoutes: [],
            },
            {
              tittle: 'Notes',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.notes,
              customSubmenuTwo: false,
              subRoutes: [],
            },
            {
              tittle: 'File Manager',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.fileManager,
              customSubmenuTwo: false,
              subRoutes: [],
            },
          ],
        },
      ],
    },
    {
      tittle: 'Inventory',
      hasSubRoute: true,
      icon: 'assets/img/icons/product.svg',
      showSubRoute: false,
      activeRoute: 'product',
      subRoutes: [
        {
          tittle: 'Products',
          hasSubRoute: false,
          showSubRoute: true,
          route: routes.productList,
          subRoutes: [],
        },
        {
          tittle: 'Create Product',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.addProduct,
          subRoutes: [],
        },
        {
          tittle: 'Expired Products',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.expiredProduct,
          subRoutes: [],
        },
        {
          tittle: 'Low Stocks',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.lowStock,
          subRoutes: [],
        },
        {
          tittle: 'Category',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.categoryList,
          subRoutes: [],
        },
        {
          tittle: 'Sub Category',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.subCategories,
          subRoutes: [],
        },
        {
          tittle: 'Brands',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.brandList,
          subRoutes: [],
        },
        {
          tittle: 'Units',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.units,
          subRoutes: [],
        },
        {
          tittle: 'Variant Attributes',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.varriantAttributes,
          subRoutes: [],
        },
        {
          tittle: 'Warranties',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.warranty,
          subRoutes: [],
        },
        {
          tittle: 'Print Barcode',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.barCode,
          subRoutes: [],
        },
        {
          tittle: 'Print QR Code',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.qrCode,
          subRoutes: [],
        },
      ],
    },
    {
      tittle: 'Sales',
      hasSubRoute: true,
      showSubRoute: false,
      activeRoute: 'users',
      subRoutes: [
        {
          tittle: 'Sales',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.salesList,
          subRoutes: [],
        },
        {
          tittle: 'Invoice',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.invoiceSettings,
          subRoutes: [],
        },
        {
          tittle: 'Sales Return',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.salesReturn,
          subRoutes: [],
        },
        {
          tittle: 'Quotation',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.quotationList,
          subRoutes: [],
        },
        {
          tittle: 'POS',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.pos,
          subRoutes: [],
        },
      ],
    },
    {
      tittle: 'Purchases',
      hasSubRoute: true,
      icon: 'assets/img/icons/purchase1.svg',
      showSubRoute: false,
      activeRoute: 'users',
      subRoutes: [
        {
          tittle: 'Purchases',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.purchaseList,
          subRoutes: [],
        },
        {
          tittle: 'Purchase Order',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.purchaseOrderReport,
          subRoutes: [],
        },
        {
          tittle: 'Purchase Return',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.purchaseReturns,
          subRoutes: [],
        },
        {
          tittle: 'ManageStock',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.manageStocks,
          subRoutes: [],
        },
        {
          tittle: 'Stock Adjustment',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.stockAdjustment,
          subRoutes: [],
        },
        {
          tittle: 'Sub Category',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.subCategories,
          subRoutes: [],
        },
        {
          tittle: 'Stock Transfer',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.stockTransfer,
          subRoutes: [],
        },
        {
          tittle: 'Expenses',
          hasSubRoute: true,
          showSubRoute: true,
          route: routes.stockTransfer,
          subRoutes: [
            {
              tittle: 'Expenses',
              route: routes.expenseList,
            },
            {
              tittle: 'Expense Category',
              route: routes.expenseCategory,
            },
          ],
        },
      ],
    },
    {
      tittle: 'Customers',
      hasSubRoute: true,
      icon: 'assets/img/icons/users1.svg',
      showSubRoute: false,
      activeRoute: 'users',
      subRoutes: [
        {
          tittle: 'Customers',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.customers,
          subRoutes: [],
        },
        {
          tittle: 'Suppliers',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.suppliers,
          subRoutes: [],
        },
        {
          tittle: 'Stores',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.storeList,
          subRoutes: [],
        },
        {
          tittle: 'Warehouses',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.wareHouse,
          subRoutes: [],
        },
      ],
    },
    {
      tittle: 'Employees',
      hasSubRoute: true,
      icon: 'assets/img/icons/product.svg',
      showSubRoute: false,
      activeRoute: 'users',
      subRoutes: [
        {
          tittle: 'Employees',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.employeesGrid,
          subRoutes: [],
        },
        {
          tittle: 'Departments',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.departmentGrid,
          subRoutes: [],
        },
        {
          tittle: 'Designation',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.designation,
          subRoutes: [],
        },
        {
          tittle: 'Shifts',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.shift,
          subRoutes: [],
        },
        {
          tittle: 'Attendence',
          hasSubRoute: true,
          showSubRoute: true,
          route: routes.shift,
          subRoutes: [
            {
              tittle: 'Employees',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.attendanceEmployee,
              subRoutes: [],
            },
            {
              tittle: 'Admin',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.attendanceAdmin,
              subRoutes: [],
            },
          ],
        },
        {
          tittle: 'Leaves',
          hasSubRoute: true,
          showSubRoute: true,
          route: routes.shift,
          subRoutes: [
            {
              tittle: 'Admin Leaves',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.leavesAdmin,
              subRoutes: [],
            },
            {
              tittle: 'Employee Leaves',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.leavesEmployee,
              subRoutes: [],
            },
            {
              tittle: 'Leave Types',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.leavesType,
              subRoutes: [],
            },
          ],
        },
        {
          tittle: 'Holidays',
          hasSubRoute: false,
          showSubRoute: true,
          route: routes.holidays,
          subRoutes: [],
        },
        {
          tittle: 'Payroll',
          hasSubRoute: true,
          showSubRoute: true,
          route: routes.holidays,
          subRoutes: [
            {
              tittle: 'Employee Salary',
              route: routes.payrollList,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Payslip',
              route: routes.paySlip,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },
      ],
    },
    {
      tittle: 'Sales Report',
      hasSubRoute: true,
      icon: 'assets/img/icons/users1.svg',
      showSubRoute: false,
      activeRoute: 'users',
      subRoutes: [
        {
          tittle: 'Sales Report',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.salesReport,
          subRoutes: [],
        },
        {
          tittle: 'Purchase Report',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.purchaseReport,
          subRoutes: [],
        },
        {
          tittle: 'Inventory Report',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.inventoryReport,
          subRoutes: [],
        },
        {
          tittle: 'Invoice Report',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.invoiceReport,
          subRoutes: [],
        },
        {
          tittle: 'Supplier Report',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.supplierReport,
          subRoutes: [],
        },
        {
          tittle: 'Supplier Report',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.supplierReport,
          subRoutes: [],
        },
        {
          tittle: 'Customer Report',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.customerReport,
          subRoutes: [],
        },
        {
          tittle: 'Expense Report',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.expenseReport,
          subRoutes: [],
        },
        {
          tittle: 'Income Report',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.incomeReport,
          subRoutes: [],
        },
        {
          tittle: 'Tax Report',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.taxReport,
          subRoutes: [],
        },
      ],
    },
    {
      tittle: 'Pages',
      hasSubRoute: true,
      icon: 'assets/img/icons/purchase1.svg',
      showSubRoute: false,
      activeRoute: 'users',
      subRoutes: [
        {
          tittle: 'Profile',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.profile,
          subRoutes: [],
        },
        {
          tittle: 'Authentication',
          hasSubRoute: true,
          showSubRoute: false,
          route: routes.purchaseReport,
          subRoutes: [
            {
              tittle: 'Login',
              hasSubRoute: true,
              showSubRoute: false,
              subRoutes: [
                {
                  tittle: 'Cover',
                  route: routes.signIn,
                  hasSubRoute: false,
                  showSubRoute: false,
                },
                {
                  tittle: 'Illustration',
                  route: routes.signIn2,
                  hasSubRoute: false,
                  showSubRoute: false,
                },
                {
                  tittle: 'Basic',
                  route: routes.signIn3,
                  hasSubRoute: false,
                  showSubRoute: false,
                },
              ],
            },
          ],
        },
        {
          tittle: 'Error Pages',
          hasSubRoute: true,
          showSubRoute: false,
          route: routes.inventoryReport,
          subRoutes: [
            {
              tittle: '404 Error',
              route: routes.error404,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: '500 Error',
              route: routes.error500,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },
        {
          tittle: 'Places',
          hasSubRoute: true,
          showSubRoute: false,
          route: routes.invoiceReport,
          subRoutes: [
            {
              tittle: 'Countries',
              route: routes.countries,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'States',
              route: routes.states,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },
        {
          tittle: 'Blank Page',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.blankPage,
          subRoutes: [],
        },
        {
          tittle: 'Coming Soon',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.comingSoon,
          subRoutes: [],
        },
        {
          tittle: 'Under Maintenance',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.underMaintanance,
          subRoutes: [],
        },
      ],
    },
    {
      tittle: 'User Management',
      hasSubRoute: true,
      icon: 'assets/img/icons/printer.svg',
      showSubRoute: false,
      activeRoute: 'users',
      subRoutes: [
        {
          tittle: 'Users',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.users,
          subRoutes: [],
        },
        {
          tittle: 'Roles & Permissions',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.rolesPermission,
          subRoutes: [],
        },
        {
          tittle: 'Delete Account Request',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.deleteaccount,
          subRoutes: [],
        },
        {
          tittle: 'Base UI',
          hasSubRoute: true,
          icon: 'assets/img/icons/printer.svg',
          showSubRoute: false,
          activeRoute: 'users',
          subRoutes: [
            {
              tittle: 'Alerts',
              route: routes.uiAlerts,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Accordion',
              route: routes.uiAccordion,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Avatar',
              route: routes.uiAvatar,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Badges',
              route: routes.uiBadges,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Border',
              route: routes.uiBorders,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Buttons',
              route: routes.uiButtons,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Button Group',
              route: routes.uiButtonsGroup,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Breadcrumb',
              route: routes.uiBreadcrumb,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Card',
              route: routes.uiCards,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Carousel',
              route: routes.uiCarousel,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Colors',
              route: routes.uiColors,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Dropdowns',
              route: routes.uiDropdowns,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Grid',
              route: routes.uiGrid,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Images',
              route: routes.uiImages,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Lightbox',
              route: routes.uiLightbox,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Media',
              route: routes.uiMedia,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Modals',
              route: routes.uiModals,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Offcanvas',
              route: routes.uiOffcanvas,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Pagination',
              route: routes.uiPagination,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Popovers',
              route: routes.uiPopovers,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Progress',
              route: routes.uiProgress,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Placeholders',
              route: routes.uiPlaceholders,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Range Slider',
              route: routes.uiRangeSlider,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Spinner',
              route: routes.uiSpinner,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Sweet Alerts',
              route: routes.uiSweetAlerts,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Tabs',
              route: routes.uiNavTabs,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Toasts',
              route: routes.uiToasts,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Tooltips',
              route: routes.uiTooltips,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Typography',
              route: routes.uiTypography,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Video',
              route: routes.uiVideo,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },
        {
          tittle: 'Advanced UI',
          hasSubRoute: true,
          showSubRoute: false,
          activeRoute: 'users',
          subRoutes: [
            {
              tittle: 'Ribbon',
              route: routes.ribbon,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Clipboard',
              route: routes.clipboard,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Drag & Drop',
              route: routes.dragDrop,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Rating',
              route: routes.rating,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Text Editor',
              route: routes.textEditor,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Counter',
              route: routes.counter,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Scrollbar',
              route: routes.scrollbar,
              hasSubRoute: false,
              showSubRoute: false,
            },

            {
              tittle: 'Timeline',
              route: routes.timeline,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },
        {
          tittle: 'Charts',
          hasSubRoute: true,
          showSubRoute: false,
          activeRoute: 'users',
          subRoutes: [
            {
              tittle: 'Apex Charts',
              route: routes.chartApex,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Ng2 Charts',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.chartNg2,
              subRoutes: [],
            },
            {
              tittle: 'Prime NG Charts',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.chartPrime,
              subRoutes: [],
            },
          ],
        },
        {
          tittle: 'Icons',
          hasSubRoute: true,
          showSubRoute: false,
          activeRoute: 'users',
          subRoutes: [
            {
              tittle: 'Fontawesome Icons',
              route: routes.iconFontAwesome,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Feather Icons',
              route: routes.iconFeather,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Ionic Icons',
              route: routes.iconIonic,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Material Icons',
              route: routes.iconMaterial,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Pe7 Icons',
              route: routes.iconPe7,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Simpleline Icons',
              route: routes.iconSimpleline,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Themify Icons',
              route: routes.iconThemify,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Weather Icons',
              route: routes.iconWeather,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Typicon Icons',
              route: routes.iconTypicon,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Flag Icons',
              route: routes.iconFlag,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },
      ],
    },
    {
      tittle: 'Settings',
      hasSubRoute: true,
      icon: 'assets/img/icons/product.svg',
      showSubRoute: false,
      activeRoute: 'users',
      subRoutes: [
        {
          tittle: 'General Settings',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.users,
          subRoutes: [
            {
              tittle: 'Profile',
              route: routes.generalSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Security',
              route: routes.securitySettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Notifications',
              route: routes.settingsNotification,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Connected Apps',
              route: routes.connectedApps,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },
        {
          tittle: 'Website Settings',
          hasSubRoute: false,
          showSubRoute: false,
          subRoutes: [
            {
              tittle: 'System Settings',
              route: routes.systemSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Company Settings',
              route: routes.companySettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Localization',
              route: routes.localizationSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Prefixes',
              route: routes.prefixes,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Preference',
              route: routes.preference,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Appearance',
              route: routes.appearance,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Social Authentication',
              route: routes.socialAuthentication,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Language',
              route: routes.languageSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },

        {
          tittle: 'App Settings',
          hasSubRoute: true,
          icon: 'assets/img/icons/printer.svg',
          showSubRoute: false,
          activeRoute: 'users',
          subRoutes: [
            {
              tittle: 'Invoice',
              route: routes.invoiceSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Printer',
              route: routes.printerSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'POS',
              route: routes.posSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Custom Fields',
              route: routes.customFields,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },
        {
          tittle: 'System Settings',
          hasSubRoute: true,
          showSubRoute: false,
          activeRoute: 'users',
          subRoutes: [
            {
              tittle: 'Email',
              route: routes.emailSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'SMS Gateways',
              route: routes.smsGateway,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'OTP',
              route: routes.otpSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'GDPR Cookies',
              route: routes.gdprSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },
        {
          tittle: 'Financial Settings',
          hasSubRoute: true,
          showSubRoute: false,
          activeRoute: 'users',
          subRoutes: [
            {
              tittle: 'Payment Gateway',
              route: routes.paymentGatewaySettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Bank Accounts',
              route: routes.bankSettingsGrid,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Tax Rates',
              route: routes.taxRates,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Currencies',
              route: routes.currencySettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },
        {
          tittle: 'Other Settings',
          hasSubRoute: true,
          showSubRoute: false,
          activeRoute: 'users',
          subRoutes: [
            {
              tittle: 'Storage',
              route: routes.storageSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Ban IP Address',
              route: routes.banIpAddress,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },
        {
          tittle: 'Documentation',
          hasSubRoute: false,
          showSubRoute: false,
          activeRoute: 'users',
        },
        {
          tittle: 'Changelog v2.0.3',
          hasSubRoute: false,
          showSubRoute: false,
          activeRoute: 'users',
        },
      ],
    },
  ];

  public sidebarData3 = [
    {
      tittle: 'Main Menu',
      hasSubRoute: true,
      icon: 'assets/img/icons/menu-icon.svg',
      showSubRoute: false,
      subRoutes: [
        {
          tittle: 'Dashboard',
          hasSubRoute: true,
          icon: 'assets/img/icons/dashboard.svg',
          showSubRoute: true,
          route: routes.dashboard,
          subRoutes: [
            {
              tittle: 'Admin Dashboard',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.adminDashboard,
              subRoutes: [],
            },
            {
              tittle: 'Sales Dashboard',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.salesDashboard,
              subRoutes: [],
            },
          ],
        },
        {
          tittle: 'Application',
          hasSubRoute: true,
          showSubRoute: true,
          base: 'application',
          subRoutes: [
            {
              tittle: 'Chat',
              route: routes.chat,
              hasSubRoute: false,
              showSubRoute: false,
              customSubmenuTwo: false,
            },
            {
              tittle: 'Call',
              hasSubRoute: true,
              showSubRoute: true,
              base: 'application',
              subRoutes: [
                {
                  tittle: 'Video Call',
                  route: routes.videoCall,
                  hasSubRoute: false,
                  showSubRoute: false,
                  subRoutes: [],
                },
                {
                  tittle: 'Audio Call',
                  route: routes.audioCall,
                  hasSubRoute: false,
                  showSubRoute: false,
                },
                {
                  tittle: 'Call History',
                  route: routes.callHistory,
                  hasSubRoute: false,
                  showSubRoute: false,
                },
              ],
            },
            {
              tittle: 'Calendar',
              route: routes.calendar,
              hasSubRoute: false,
              showSubRoute: false,
              customSubmenuTwo: false,
            },
            {
              tittle: 'Email',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.email,
              customSubmenuTwo: false,
              subRoutes: [],
            },
            {
              tittle: 'To Do',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.toDo,
              customSubmenuTwo: false,
              subRoutes: [],
            },
            {
              tittle: 'Notes',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.notes,
              customSubmenuTwo: false,
              subRoutes: [],
            },
            {
              tittle: 'File Manager',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.fileManager,
              customSubmenuTwo: false,
              subRoutes: [],
            },
          ],
        },
      ],
    },
    {
      tittle: 'Inventory',
      hasSubRoute: true,
      icon: 'assets/img/icons/product.svg',
      showSubRoute: false,
      activeRoute: 'product',
      subRoutes: [
        {
          tittle: 'Products',
          hasSubRoute: false,
          showSubRoute: true,
          route: routes.productList,
          subRoutes: [],
        },
        {
          tittle: 'Create Product',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.addProduct,
          subRoutes: [],
        },
        {
          tittle: 'Expired Products',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.expiredProduct,
          subRoutes: [],
        },
        {
          tittle: 'Low Stocks',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.lowStock,
          subRoutes: [],
        },
        {
          tittle: 'Category',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.categoryList,
          subRoutes: [],
        },
        {
          tittle: 'Sub Category',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.subCategories,
          subRoutes: [],
        },
        {
          tittle: 'Brands',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.brandList,
          subRoutes: [],
        },
        {
          tittle: 'Units',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.units,
          subRoutes: [],
        },
        {
          tittle: 'Variant Attributes',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.varriantAttributes,
          subRoutes: [],
        },
        {
          tittle: 'Warranties',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.warranty,
          subRoutes: [],
        },
        {
          tittle: 'Print Barcode',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.barCode,
          subRoutes: [],
        },
        {
          tittle: 'Print QR Code',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.qrCode,
          subRoutes: [],
        },
      ],
    },
    {
      tittle: 'Sales & Purchase',
      hasSubRoute: true,
      icon: 'assets/img/icons/purchase1.svg',
      showSubRoute: false,
      activeRoute: 'users',
      subRoutes: [
        {
          tittle: 'Sales',
          hasSubRoute: true,
          showSubRoute: false,
          route: routes.salesList,
          subRoutes: [
            {
              tittle: 'Sales',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.salesList,
              subRoutes: [],
            },
            {
              tittle: 'Invoice',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.invoiceSettings,
              subRoutes: [],
            },
            {
              tittle: 'Sales Return',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.salesReturn,
              subRoutes: [],
            },
            {
              tittle: 'Quotation',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.quotationList,
              subRoutes: [],
            },
            {
              tittle: 'POS',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.pos,
              subRoutes: [],
            },
          ],
        },
        {
          tittle: 'Purchases',
          hasSubRoute: true,
          icon: 'assets/img/icons/purchase1.svg',
          showSubRoute: false,
          activeRoute: 'users',
          subRoutes: [
            {
              tittle: 'Purchases',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.purchaseList,
              subRoutes: [],
            },
            {
              tittle: 'Purchase Order',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.purchaseOrderReport,
              subRoutes: [],
            },
            {
              tittle: 'Purchase Return',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.purchaseReturns,
              subRoutes: [],
            },
            {
              tittle: 'ManageStock',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.manageStocks,
              subRoutes: [],
            },
            {
              tittle: 'Stock Adjustment',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.stockAdjustment,
              subRoutes: [],
            },
            {
              tittle: 'Sub Category',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.subCategories,
              subRoutes: [],
            },
            {
              tittle: 'Stock Transfer',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.stockTransfer,
              subRoutes: [],
            },
          ],
        },
        {
          tittle: 'Expenses',
          hasSubRoute: true,
          showSubRoute: true,
          route: routes.stockTransfer,
          subRoutes: [
            {
              tittle: 'Expenses',
              route: routes.expenseList,
            },
            {
              tittle: 'Expense Category',
              route: routes.expenseCategory,
            },
          ],
        },
      ],
    },
    {
      tittle: 'User Management',
      hasSubRoute: true,
      icon: 'assets/img/icons/users1.svg',
      showSubRoute: false,
      activeRoute: 'users',
      subRoutes: [
        {
          tittle: 'Roles & Permissions',
          hasSubRoute: true,
          showSubRoute: false,
          route: routes.users,
          subRoutes: [
            {
              tittle: 'Roles & Permissions',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.rolesPermission,
              subRoutes: [],
            },
            {
              tittle: 'Delete Account Request',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.deleteaccount,
              subRoutes: [],
            },
          ],
        },
        {
          tittle: 'Base UI',
          hasSubRoute: true,
          icon: 'assets/img/icons/printer.svg',
          showSubRoute: false,
          activeRoute: 'users',
          subRoutes: [
            {
              tittle: 'Alerts',
              route: routes.uiAlerts,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Accordion',
              route: routes.uiAccordion,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Avatar',
              route: routes.uiAvatar,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Badges',
              route: routes.uiBadges,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Border',
              route: routes.uiBorders,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Buttons',
              route: routes.uiButtons,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Button Group',
              route: routes.uiButtonsGroup,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Breadcrumb',
              route: routes.uiBreadcrumb,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Card',
              route: routes.uiCards,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Carousel',
              route: routes.uiCarousel,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Colors',
              route: routes.uiColors,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Dropdowns',
              route: routes.uiDropdowns,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Grid',
              route: routes.uiGrid,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Images',
              route: routes.uiImages,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Lightbox',
              route: routes.uiLightbox,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Media',
              route: routes.uiMedia,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Modals',
              route: routes.uiModals,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Offcanvas',
              route: routes.uiOffcanvas,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Pagination',
              route: routes.uiPagination,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Popovers',
              route: routes.uiPopovers,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Progress',
              route: routes.uiProgress,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Placeholders',
              route: routes.uiPlaceholders,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Range Slider',
              route: routes.uiRangeSlider,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Spinner',
              route: routes.uiSpinner,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Sweet Alerts',
              route: routes.uiSweetAlerts,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Tabs',
              route: routes.uiNavTabs,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Toasts',
              route: routes.uiToasts,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Tooltips',
              route: routes.uiTooltips,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Typography',
              route: routes.uiTypography,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Video',
              route: routes.uiVideo,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },
        {
          tittle: 'Advanced UI',
          hasSubRoute: true,
          showSubRoute: false,
          activeRoute: 'users',
          subRoutes: [
            {
              tittle: 'Ribbon',
              route: routes.ribbon,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Clipboard',
              route: routes.clipboard,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Drag & Drop',
              route: routes.dragDrop,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Rating',
              route: routes.rating,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Text Editor',
              route: routes.textEditor,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Counter',
              route: routes.counter,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Scrollbar',
              route: routes.scrollbar,
              hasSubRoute: false,
              showSubRoute: false,
            },

            {
              tittle: 'Timeline',
              route: routes.timeline,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },
        {
          tittle: 'Charts',
          hasSubRoute: true,
          showSubRoute: false,
          activeRoute: 'users',
          subRoutes: [
            {
              tittle: 'Apex Charts',
              route: routes.chartApex,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Ng2 Charts',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.chartNg2,
              subRoutes: [],
            },
            {
              tittle: 'Prime NG Charts',
              hasSubRoute: false,
              showSubRoute: false,
              route: routes.chartPrime,
              subRoutes: [],
            },
          ],
        },
        {
          tittle: 'Primary-icons',
          hasSubRoute: true,
          showSubRoute: false,
          activeRoute: 'users',
          subRoutes: [
            {
              tittle: 'Fontawesome Icons',
              route: routes.iconFontAwesome,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Feather Icons',
              route: routes.iconFeather,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Ionic Icons',
              route: routes.iconIonic,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Material Icons',
              route: routes.iconMaterial,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Pe7 Icons',
              route: routes.iconPe7,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },
        {
          tittle: 'Secondary-icons',
          hasSubRoute: true,
          showSubRoute: false,
          activeRoute: 'users',
          subRoutes: [
            {
              tittle: 'Simpleline Icons',
              route: routes.iconSimpleline,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Themify Icons',
              route: routes.iconThemify,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Weather Icons',
              route: routes.iconWeather,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Typicon Icons',
              route: routes.iconTypicon,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Flag Icons',
              route: routes.iconFlag,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },
        {
          tittle: 'Forms',
          hasSubRoute: true,
          showSubRoute: false,
          activeRoute: 'users',
          subRoutes: [
            {
              tittle: 'Basic Inputs',
              route: routes.formBasicInputs,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Checkbox & Radios',
              route: routes.formCheckboxRadios,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Input Groups',
              route: routes.formInputsGroups,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Form Select',
              route: routes.formSelect,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Input Masks',
              route: routes.formMask,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'File Uploads',
              route: routes.formFileUpload,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },
      ],
    },
    {
      tittle: 'Profile',
      hasSubRoute: true,
      icon: 'assets/img/icons/users1.svg',
      showSubRoute: false,
      subRoutes: [
        {
          tittle: 'Profile',
          route: routes.generalSettings,
          hasSubRoute: false,
          showSubRoute: false,
        },
        {
          tittle: 'Authentication',
          hasSubRoute: true,
          showSubRoute: false,
          route: routes.purchaseReport,
          subRoutes: [
            {
              tittle: 'Login',
              hasSubRoute: true,
              showSubRoute: false,
              subRoutes: [
                {
                  tittle: 'Cover',
                  route: routes.signIn,
                  hasSubRoute: false,
                  showSubRoute: false,
                },
                {
                  tittle: 'Illustration',
                  route: routes.signIn2,
                  hasSubRoute: false,
                  showSubRoute: false,
                },
                {
                  tittle: 'Basic',
                  route: routes.signIn3,
                  hasSubRoute: false,
                  showSubRoute: false,
                },
              ],
            },
          ],
        },
        {
          tittle: 'Pages',
          hasSubRoute: true,
          showSubRoute: false,
          route: routes.purchaseReport,
          subRoutes: [
            {
              tittle: '404 Error',
              hasSubRoute: true,
              showSubRoute: false,
              subRoutes: [],
            },
            {
              tittle: '500 Error',
              hasSubRoute: true,
              showSubRoute: false,
              subRoutes: [],
            },
            {
              tittle: 'Blank page',
              hasSubRoute: true,
              showSubRoute: false,
              subRoutes: [],
            },
            {
              tittle: 'Comming Soon',
              hasSubRoute: true,
              showSubRoute: false,
              subRoutes: [],
            },
          ],
        },
      ],
    },
    {
      tittle: 'Reports',
      hasSubRoute: true,
      icon: 'assets/img/icons/printer.svg',
      showSubRoute: false,
      activeRoute: 'users',
      subRoutes: [
        {
          tittle: 'Sales Report',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.salesReport,
          subRoutes: [],
        },
        {
          tittle: 'Purchase Report',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.purchaseReport,
          subRoutes: [],
        },
        {
          tittle: 'Inventory Report',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.inventoryReport,
          subRoutes: [],
        },
        {
          tittle: 'Invoice Report',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.invoiceReport,
          subRoutes: [],
        },
        {
          tittle: 'Supplier Report',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.supplierReport,
          subRoutes: [],
        },
        {
          tittle: 'Supplier Report',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.supplierReport,
          subRoutes: [],
        },
        {
          tittle: 'Customer Report',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.customerReport,
          subRoutes: [],
        },
        {
          tittle: 'Expense Report',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.expenseReport,
          subRoutes: [],
        },
        {
          tittle: 'Income Report',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.incomeReport,
          subRoutes: [],
        },
        {
          tittle: 'Tax Report',
          hasSubRoute: false,
          showSubRoute: false,
          route: routes.taxReport,
          subRoutes: [],
        },
      ],
    },
    {
      tittle: 'Settings',
      hasSubRoute: true,
      icon: 'assets/img/icons/purchase1.svg',
      showSubRoute: false,
      activeRoute: 'users',
      subRoutes: [
        {
          tittle: 'General Settings',
          hasSubRoute: true,
          showSubRoute: false,
          route: routes.users,
          subRoutes: [
            {
              tittle: 'Profile',
              route: routes.generalSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Security',
              route: routes.securitySettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Notifications',
              route: routes.settingsNotification,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Connected Apps',
              route: routes.connectedApps,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },
        {
          tittle: 'Website Settings',
          hasSubRoute: true,
          showSubRoute: false,
          subRoutes: [
            {
              tittle: 'System Settings',
              route: routes.systemSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Company Settings',
              route: routes.companySettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Localization',
              route: routes.localizationSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Prefixes',
              route: routes.prefixes,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Preference',
              route: routes.preference,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Appearance',
              route: routes.appearance,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Social Authentication',
              route: routes.socialAuthentication,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Language',
              route: routes.languageSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },

        {
          tittle: 'App Settings',
          hasSubRoute: true,
          icon: 'assets/img/icons/printer.svg',
          showSubRoute: false,
          activeRoute: 'users',
          subRoutes: [
            {
              tittle: 'Invoice',
              route: routes.invoiceSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Printer',
              route: routes.printerSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'POS',
              route: routes.posSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Custom Fields',
              route: routes.customFields,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },
        {
          tittle: 'System Settings',
          hasSubRoute: true,
          showSubRoute: false,
          activeRoute: 'users',
          subRoutes: [
            {
              tittle: 'Email',
              route: routes.emailSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'SMS Gateways',
              route: routes.smsGateway,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'OTP',
              route: routes.otpSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'GDPR Cookies',
              route: routes.gdprSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },
        {
          tittle: 'Financial Settings',
          hasSubRoute: true,
          showSubRoute: false,
          activeRoute: 'users',
          subRoutes: [
            {
              tittle: 'Payment Gateway',
              route: routes.paymentGatewaySettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Bank Accounts',
              route: routes.bankSettingsGrid,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Tax Rates',
              route: routes.taxRates,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Currencies',
              route: routes.currencySettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },
        {
          tittle: 'Other Settings',
          hasSubRoute: true,
          showSubRoute: false,
          activeRoute: 'users',
          subRoutes: [
            {
              tittle: 'Storage',
              route: routes.storageSettings,
              hasSubRoute: false,
              showSubRoute: false,
            },
            {
              tittle: 'Ban IP Address',
              route: routes.banIpAddress,
              hasSubRoute: false,
              showSubRoute: false,
            },
          ],
        },
        {
          tittle: 'Documentation',
          hasSubRoute: false,
          showSubRoute: false,
          activeRoute: 'users',
        },
        {
          tittle: 'Changelog v2.0.3',
          hasSubRoute: false,
          showSubRoute: false,
          activeRoute: 'users',
        },
      ],
    },
  ];

  public settings_sidebar = [
    {
      icon: 'feather icon-settings',
      title: 'General Settings',
      page: 'general-settings',
      subMenu: [
        { title: 'Profile', routes: routes.generalSettings },
        { title: 'Security', routes: routes.securitySettings },
        { title: 'Notifications', routes: routes.settingsNotification },
        { title: 'Connected Apps', routes: routes.connectedApps },
      ],
    },
    {
      icon: 'feather icon-airplay',
      title: 'Website Settings',
      page: 'website-settings',
      subMenu: [
        { title: 'System Settings', routes: routes.systemSettings },
        { title: 'Company Settings', routes: routes.companySettings },
        { title: 'Localization', routes: routes.localizationSettings },
        { title: 'Prefixes', routes: routes.prefixes },
        { title: 'Preference', routes: routes.preference },
        { title: 'Appearance', routes: routes.appearance },
        {
          title: 'Social Authentication',
          routes: routes.socialAuthentication,
        },
        { title: 'Language', routes: routes.languageSettings },
      ],
    },
    {
      icon: 'feather icon-archive',
      title: 'App Settings',
      page: 'app-settings',
      subMenu: [
        { title: 'Invoice', routes: routes.invoiceSettings },
        { title: 'Printer', routes: routes.printerSettings },
        { title: 'POS', routes: routes.posSettings },
        { title: 'Custom Fields', routes: routes.customFields },
      ],
    },
    {
      icon: 'feather icon-server',
      title: 'System Settings',
      page: 'system-settings',
      subMenu: [
        { title: 'Email', routes: routes.emailSettings },
        { title: 'SMS Gateways', routes: routes.smsGateway },
        { title: 'OTP', routes: routes.otpSettings },
        { title: 'GDPR Cookies', routes: routes.gdprSettings },
      ],
    },
    {
      icon: 'feather icon-credit-card',
      title: 'Financial Settings',
      page: 'financial-settings',
      subMenu: [
        { title: 'Payment Gateway', routes: routes.paymentGatewaySettings },
        { title: 'Bank Accounts', routes: routes.bankSettingsGrid },
        { title: 'Tax Rates', routes: routes.taxRates },
        { title: 'Currencies', routes: routes.currencySettings },
      ],
    },
    {
      icon: 'feather icon-layout',
      title: 'Other Settings',
      page: 'other-settings',
      subMenu: [
        { title: 'Storage', routes: routes.storageSettings },
        { title: 'Ban IP Address', routes: routes.banIpAddress },
      ],
    },
  ];
  public videocall = [
    {
      img: 'assets/img/users/user-01.jpg',
      name: 'Barbara',
    },
    {
      img: 'assets/img/users/user-02.jpg',
      name: 'Linnea',
    },
    {
      img: 'assets/img/users/user-05.jpg',
      name: 'Richard',
    },
    {
      img: 'assets/img/users/user-03.jpg',
      name: 'Freda',
    },
  ];
}
