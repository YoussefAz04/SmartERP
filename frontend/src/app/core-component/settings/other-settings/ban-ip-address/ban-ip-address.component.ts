import { Component } from '@angular/core';
import { SidebarService } from 'src/app/core/service/sidebar/sidebar.service';
import Swal from 'sweetalert2';
interface data {
  value: string;
}

@Component({
  selector: 'app-ban-ip-address',
  templateUrl: './ban-ip-address.component.html',
  styleUrl: './ban-ip-address.component.scss'
})
export class BanIpAddressComponent {

 public selectedValue1 = '';
 public selectedValue2 = '';
 selectedList1: data[] = [
  { value: 'Sort by Datee' },
  { value: 'Newest' },
  { value: 'Oldest' },
];
selectedList2: data[] = [
  { value: 'Choose IP' },
  { value: '211.11.0.25' },
  { value: '211.03.0.11' },
];
constructor( private sidebar: SidebarService){
    
}
  isCollapsed: boolean = false;
  toggleCollapse() {
    this.sidebar.toggleCollapse();
    this.isCollapsed = !this.isCollapsed;
  }
public filter = false;
openFilter() {
  this.filter = !this.filter;
}
confirmColor() {
  const swalWithBootstrapButtons = Swal.mixin({
    customClass: {
      confirmButton: ' btn btn-success',
      cancelButton: 'me-2 btn btn-danger',
    },
    buttonsStyling: false,
  });

  swalWithBootstrapButtons
    .fire({
      title: 'Are you sure?',
      text: "You won't be able to revert this!",
      confirmButtonText: 'Yes, delete it!',
      showCancelButton: true,
      cancelButtonText: 'Cancel',
      reverseButtons: true,
    })
    .then((result) => {
      if (result.isConfirmed) {
        swalWithBootstrapButtons.fire(
          'Deleted!',
          'Your file has been deleted.',
          'success'
        );
      } else if (result.dismiss === Swal.DismissReason.cancel) {
        swalWithBootstrapButtons.fire(
          'Cancelled',
          'Your imaginary file is safe :)',
          'error'
        );
      }
    });
}
}
