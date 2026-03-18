import { Component } from '@angular/core';
import { SidebarService, routes } from 'src/app/core/core.index';
interface data {
  value: string;
}
@Component({
  selector: 'app-add-employee',
  templateUrl: './add-employee.component.html',
  styleUrl: './add-employee.component.scss'
})
export class AddEmployeeComponent {
  constructor( private sidebar: SidebarService){

  }
  public routes = routes;
  public selectedValue1 = '';
  public selectedValue2 = '';
  public selectedValue3 = '';
  public selectedValue4 = '';
  public selectedValue5 = '';
  public selectedValue6 = '';
  public selectedValue7 = '';
  public password : boolean[] = [false];
  selectedList1: data[] = [{ value: 'Male' }, { value: 'Female' }];
  selectedList2: data[] = [{ value: 'United Kingdom' }, { value: 'India' }];
  selectedList3: data[] = [{ value: 'Choose' },{value: 'Regular' }];
  selectedList4: data[] = [
    { value: 'UI/UX' },
    { value: 'Support' },
    { value: 'HR' },
    { value: 'Engineering' },
  ];
  selectedList5: data[] = [
    { value: 'Designer' },
    { value: 'Developer' },
    { value: 'Tester' },
  ];
  selectedList6: data[] = [
    { value: 'O positive' },
    { value: 'A positive' },
    { value: 'B positive' },
  ];
  selectedList7: data[] = [{ value: 'United Kingdom' }, { value: 'USA' }];

  public togglePassword(index: number){
    this.password[index] = !this.password[index]
  }
  isCollapsed: boolean = false;
  toggleCollapse() {
    this.sidebar.toggleCollapse();
    this.isCollapsed = !this.isCollapsed;
  }
}
