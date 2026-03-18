import { Component, OnDestroy, OnInit, Renderer2 } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { Editor, Toolbar, Validators } from 'ngx-editor';
import { CommonService } from 'src/app/core/service/common/common.service';
import { SidebarService } from 'src/app/core/service/sidebar/sidebar.service';
interface data {
  value: string;
}

@Component({
  selector: 'app-todo',
  templateUrl: './todo.component.html',
  styleUrl: './todo.component.scss',
})
export class TodoComponent implements OnInit, OnDestroy {
  public selectedValue1 = '';
  public selectedValue2 = '';
  public selectedValue3 = '';
  public selectedValue4 = '';
  public selectedValue5 = '';
  public selectedValue6 = '';
  public selectedValue8 = '';
  public selectedValue7 = '';
  public selectedValue9 = '';
  public selectedValue10 = '';
  public selectedValue11 = '';
  public selectedValue12 = '';
  public selectedValue13 = '';
  selectedList1: data[] = [
    { value: 'Bulk Actions' },
    { value: 'Delete Marked' },
    { value: 'Unmark All' },
    { value: 'Mark All' },
  ];
  selectedList2: data[] = [
    { value: 'Recent' },
    { value: 'Last Modified' },
    { value: 'Unmark All' },
    { value: 'Last Modified by me' },
  ];
  selectedList5: data[] = [
    { value: 'Sort by Date' },
    { value: 'Ascending' },
    { value: 'Descending' },
    { value: 'Recently Viewed' },
    { value: 'Recently Added' },
    { value: 'Creation Date ' },
  ];
  selectedList6: data[] = [
    { value: 'Choose' },
    { value: 'Recent1' },
    { value: 'Recent2' },
  ];
  selectedList7: data[] = [
    { value: 'Onhold' },
    { value: 'Onhold' },
    { value: 'Onhold' },
  ];
  selectedList8: data[] = [
    { value: 'High' },
    { value: 'Medium' },
    { value: 'Low' },
  ];
  selectedList9: data[] = [
    { value: 'Select' },
    { value: 'Recent1' },
    { value: 'Recent2' },
  ];
  selectedList10: data[] = [
    { value: 'Choose' },
    { value: 'Recent1' },
    { value: 'Recent2' },
  ];
  selectedList11: data[] = [
    { value: 'Select' },
    { value: 'Recent1' },
    { value: 'Recent2' },
  ];
  selectedList12: data[] = [
    { value: 'Select' },
    { value: 'Recent1' },
    { value: 'Recent2' },
  ];
  selectedList13: data[] = [
    { value: 'Select' },
    { value: 'Recent1' },
    { value: 'Recent2' },
  ];
  isCollapsed: boolean = false;
  toggleCollapse() {
    this.sidebar.toggleCollapse();
    this.isCollapsed = !this.isCollapsed;
  }
  bsValue = new Date();
  bsRangeValue: Date[];
  maxDate = new Date();
  constructor(
    private sidebar: SidebarService,
    private common: CommonService,
    private renderer: Renderer2
  ) {
    this.maxDate.setDate(this.maxDate.getDate() + 7);
    this.bsRangeValue = [this.bsValue, this.maxDate];
  }

  editor!: Editor;
  toolbar: Toolbar = [
    ['bold', 'italic'],
    ['underline', 'strike'],
    ['code', 'blockquote'],
    ['ordered_list', 'bullet_list'],
    [{ heading: ['h1', 'h2', 'h3', 'h4', 'h5', 'h6'] }],
    ['link', 'image'],
    ['text_color', 'background_color'],
    ['align_left', 'align_center', 'align_right', 'align_justify'],
  ];

  form = new FormGroup({
    editorContent: new FormControl('', Validators.required()),
  });

  ngOnInit(): void {
    this.editor = new Editor();
  }

  ngOnDestroy(): void {
    this.editor.destroy();
  }
  public appSidebar = true;

  toggleChange() {
    this.appSidebar = !this.appSidebar;
  }
}
