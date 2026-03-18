export interface countries {
  CountryName?: number;
  Region?: string;
  isSelected?: boolean;
  isStatus?: boolean;
}
export interface expenseCategoryList {
  sNo?: number;
  Date?: number;
  Categoryname?: string;
  Reference?: string;
  Status?: string;
  Amount?: string;
  isSelected?: boolean;
  Description?: string;
}
export interface userList {
  sNo?: number;
  Username?: string;
  Phone?: string;
  email?: string;
  isSelected?: boolean;
  Role?: string;
  CreatedOn?: string;
  Status?: string;
}

export interface peopleUserList {
  sNo?: number;
  img?: string;
  Firstname?: string;
  Lastname?: string;
  Username?: string;
  Phone?: string;
  isSelected?: boolean;
  isStatus?: boolean;
  email?: string;
}

export interface transferList {
  sNo?: number;
  Date?: string;
  Reference?: string;
  isSelected?: boolean;
  From?: string;
  Paid?: string;
  items?: string;
  Grandtotal?: string;
  Status?: string;
}

export interface taxRates {
  sNo?: number;
  Taxname?: string;
  Tax?: string;
  isSelected?: boolean;
  isStatus?: boolean;
}

export interface paymentSettings {
  sNo?: number;
  PaymentTypeName?: string;
  isSelected?: boolean;
  isStatus?: boolean;
}

export interface groupPermission {
  sNo?: number;
  Role?: string;
  description?: string;
  Status?: string;
  isSelected?: boolean;
}

export interface currencySettings {
  sNo?: number;
  currencyName?: string;
  currencyCode?: string;
  currencySymbol?: string;
  exchangerate?: string;
  createdOn?: string;
}

export interface salesreturn {
  sNo: number;
  img: string;
  productName: string;
  date: string;
  customer: string;
  status: string;
  grandTotal: string;
  paid: string;
  due: string;
  paymentStatus: string;
  isSelected?: boolean;
}

export interface purchaseOrderReport {
  sNo: number;
  img: string;
  productName: string;
  purchasedAmount: string;
  purchasedQty: string;
  instockQty: string;
  isSelected?: boolean;
}

export interface invoiceReport {
  sNo?: number;
  Invoicenumber?: string;
  Customername?: string;
  isSelected?: boolean;
  Duedate?: string;
  Amount?: string;
  Paid?: string;
  Amountdue?: string;
  Status?: string;
}

export interface inventoryReport {
  sNo?: number;
  ProductName?: string;
  SKU?: string;
  Category?: string;
  isSelected?: boolean;
  Brand?: string;
  price?: string;
  Unit?: string;
  Instockqty?: string;
  img?: string;
}

export interface brandList {
  sNo?: number;
  brand: string;
  createdOn: string;
  status: string;
  img: string;
  isSelected?: boolean;
}

export interface stateList {
  sNo?: number;
  StateName?: string;
  CountryName?: string;
  isSelected?: boolean;
  isStatus?: boolean;
}

export interface customerReport {
  sNo?: number;
  Customercode?: string;
  Customername?: string;
  isSelected?: boolean;
  Amount?: string;
  Paid?: string;
  Amountdue?: string;
  Status?: string;
  PaymentStatus?: string;
}

export interface subcategoryList {
  sNo?: number;
  Image?: string;
  Category?: string;
  Parentcategory?: string;
  CategoryCode?: string;
  isSelected?: boolean;
  Description?: string;
  CreatedBy?: string;
}

export interface productList {
  sNo: number;
  product: string;
  SKU: string;
  category: string;
  brand: string;
  price: string;
  unit: string;
  qty: string;
  createdBy: string;
  img1: string;
  img2: string;
  isSelected?: boolean;
}

export interface purchaseList {
  sNo: number;
  supplierName: string;
  reference: string;
  date: string;
  status: string;
  grandTotal: string;
  paid: string;
  due: string;
  createdBy: string;
  isSelected?: boolean;
}

export interface purchaseReturns {
  sNo: number;
  img: string;
  date: string;
  supplier: string;
  reference: string;
  status: string;
  grandTotal: string;
  paid: string;
  due: string;
  paymentStatus: string;
  isSelected?: boolean;
}
export interface supplierList {
  sNo?: number;
  img?: string;
  SupplierName?: string;
  code?: string;
  isSelected?: boolean;
  Customer?: string;
  email?: string;
  Country?: string;
}
export interface storeList {
  sNo?: number;
  Storename?: string;
  Username?: string;
  Phone?: string;
  isSelected?: boolean;
  email?: string;
  isStatus?: boolean;
}

export interface customerList {
  sNo?: number;
  img?: string;
  CustomerName?: string;
  code?: string;
  Customer?: string;
  Phone?: string;
  email?: string;
  isSelected?: boolean;
  Country?: string;
}
export interface datatables {
  isSelected: boolean;
  sNo?: number;
  name?: string;
  position?: string;
  office?: string;
  age?: string;
  salary?: string;
  startDate?: string;
  id?: string;
}
export interface expenseList {
  sNo?: number;
  categoryName?: string;
  reference?: string;
  date?: string;
  
  status?: string;
  amount?: string;
  description?: string;
  isSelected?: boolean;
}

export interface Star {
  show?: boolean;
  half?: boolean;
}

export interface pos {
  Date?: string;
  Reference?: string;
  Customer?: string;
  Amount?: string;
}

export interface subRoutes {
  tittle?: string;
  hasSubRoute?: boolean;
  icon?: string;
  showSubRoute?: boolean;
  route?: string;
  activeRoute?: string;
}
export interface ScheduleEvent {
  id?: string;
  title?: string;
  start?: string;
}

export interface Permission {
  View: boolean;
  Create: boolean;
  Edit: boolean;
  Delete: boolean;
  ViewAllRecords: boolean;
  Barcode: boolean;
  SelectAll: boolean;
  Name: string;
  details: string;
  ImportProducts: string;
  editpermission: string;
}
export interface DeleteAccount {
  sNo?: number;
  userImg?: string;
  CustomerName?: string;
  code?: string;
  Customer?: string;
  Phone?: string;
  email?: string;
  isSelected?: boolean;
  Country?: string;
  userName?: string;
  requisition?: string;
  delete?: string;
}
export interface Coupons {
  code?: string;
  Customer?: string;
  used?: string;
  valid?: string;
  status?: boolean;
  Country?: string;
  userName?: string;
  requisition?: string;
  delete?: string;
  type?: string;
  discount?: string;
  limit?: string;
  sNo?: number;
  isSelected?: boolean;
}
export interface designation {
  designation?: string;
  createdOn?: string;
  members?: string;
  sNo?: number;
  isSelected?: boolean;
}
export interface departmentList {
  department?: string;
  hod?: string;
  members?: string;
  createdon?: string;
  sNo?: number;
  img?: string;
  status?: string;
}

export interface barcode {
  sNo: number;
  product: string;
  sku: string;
  code: string;
  img: string;
}
export interface expenseCategory {
  sNo: number;
  categoryName: string;
  description: string;
  img: string;
  isSelected?: boolean;
}
export interface expiredProducts {
  sNo: number;
  product: string;
  sku: string;
  code: string;
  img: string;
  manufacturedDate: string;
  expiredDate: string;
  isSelected: boolean,
}
export interface employeeList {
  sNo: number;
  img: string;
  employee: string;
  role: string;
  employeeID: string;
  email: string;
  phoneNumber: string;
  shift: string;
  status: string;
  isSelected?: boolean;
}
export interface bankSettingsList {
  sNo: number;
  name: string;
  bank: string;
  branch: string;
  accountNo: string;
  ifsc: string;
  createdOn: string;
  isSelected?: boolean;
}
export interface customFiled {
  sNo: number;
  module: string;
  label: string;
  type: string;
  defaultvalue: string;
  required: string;
  isSelected?: boolean;
}
export interface attendenceAdmin {
  sNo: number;
  name: string;
  img: string;
  posting: string;
  empId: string;
  Date: string;
  shift: string;
  clockIn: string;
  Production: string;
  overTime: string;
  totalhours: string;
  isSelected?: boolean;
}
export interface attendenceEmployee {
  sNo: number;
  date: string;
  clockIn: string;
  clockOut: string;
  production: string;
  overTime: string;
  break: string;
  status: string;
  totalHours: string;
  isSelected?: boolean;
}
export interface file {
  sNo: number;
  name: string;
  lastModified: string;
  size: string;
  ownedMember: string;
  modified: string;
  memberimg: string;
  folderimg: string;
  isSelected?: boolean;
}
export interface holiday {
  sNo: number;
  name: string;
  date: string;
  duration: string;
  createdOn: string;
  status: string;
  isSelected?: boolean;
}
export interface languagesetting {
  sNo: number;
  language: string;
  code: string;
  rtl: string;
  total: string;
  done: string;
  progress: string;
  status: string;
  img:string;
  isSelected?: boolean;
}
export interface callHistory {
  sNo: number;
  username: string;
  phoneNumber: string;
  callType: string;
  duration: string;
  dateTime: string;
  img: string;
  video: boolean;
  audio: boolean;
  isSelected?: boolean;
}
export interface categoryList {
  sNo: number;
  category: string;
  categorySlug: string;
  createdOn: string;
  status: string;
  isSelected?: boolean;
}
export interface expiredproduct {
  sNo: number;
  product: string;
  sku: string;
  manufacturedDate: string;
  expiredDate: string;
  img: string;
  isSelected?: boolean;
}
export interface unit {
  sNo: number;
  unit: string;
  shortName: string;
  noOfProducts: string;
  createdOn: string;
  status: string;
  isSelected?: boolean;
}
export interface countries {
  sNo: number;
  countryName: string;
  status: string;
}
export interface state {
  sNo: number;
  stateName: string;
  country: string;
  status: string;
  isSelected?: boolean;
}
export interface customer {
  sNo: number;
  customerName: string;
  code: string;
  customer: string;
  img: string;
  email: string;
  phone: string;
  country: string;
  isSelected?: boolean;
}
export interface printer {
  sNo: number;
  printerName: string;
  connectionType: string;
  ipAddress: string;
  port: string;
}
export interface fileShared {
  sNo: number;
  name: string;
  lastModified: string;
  size: string;
  ownedMember: string;
  img1: string;
  img2: string;
  isSelected?: boolean;
}
export interface rolesPermissions {
  sNo: number;
  roleName: string;
  createdOn: string;
  isSelected?: boolean;
}
export interface salesList {
  sNo: number;
  customerName: string;
  reference: string;
  date: string;
  status: string;
  grandTotal: string;
  paid: string;
  due: string;
  paymentStatus: string;
  biller: string;
  isSelected?: boolean;
}
export interface shift {
  sNo: number;
  shiftName: string;
  time: string;
  weekOff: string;
  createdOn: string;
  status: string;
  isSelected?: boolean;
}

export interface lowStocks {
  sNo: number;
  warehouse: string;
  store: string;
  product: string;
  category: string;
  sku: string;
  qty: string;
  qtyAlert: string;
  img: string;
  isSelected?: boolean;
}
export interface manageStocks {
  sNo: number;
  warehouse: string;
  shop: string;
  product: string;
  date: string;
  person: string;
  quantity: string;
  img: string;
  img2: string;
  isSelected?: boolean;
}
export interface warranty {
  sNo: number;
  name: string;
  description: string;
  duration: string;
  status: string;
  isSelected?: boolean;
}
export interface payrollList {
  employee: string;
  employeeID: string;
  email: string;
  salary: string;
  status: string;
  img: string;
  sNo: number;
  isSelected?: boolean;
  

}
export interface salesReport {
  sNo: number;
  productName: string;
  img: string;
  SKU: string;
  category: string;
  brand: string;
  soldQty: string;
  soldAmount: string;
  instockQty: string;
  isSelected?: boolean;
}
export interface variantAttributes {
  sNo: number;
  variant: string;
  values: string;
  createdOn: string;
  status: string;
  isSelected?: boolean;
}
export interface stock {
  sNo: number;
  fromWarehouse: string;
  toWarehouse: string;
  noOfProducts: string;
  quantityTransferred: string;
  refNumber: string;
  date: string;
  isSelected?: boolean;
}
export interface stockadjustment {
  sNo: number;
  warehouse: string;
  shop: string;
  product: string;
  date: string;
  person: string;
  quantity: string;
  img: string;
  img2: string;
  isSelected?: boolean;
}
export interface users {
  sNo: number;
  img: string;
  userName: string;
  phone: string;
  email: string;
  role: string;
  createdOn: string;
  status: string;
  isSelected?: boolean;
}
export interface quotationList {
  sNo: number;
  img: string;
  productName: string;
  reference: string;
  customerName: string;
  status: string;
  grandTotal: string;
  isSelected?: boolean;
  

}
export interface qrCode {
  sNo: number;
  product: string;
  sku: string;
  code: string;
  referenceNo: string;
  qty: string;
  img: string;
}
export interface leavesadmin {
  sNo: number;
  empName: string;
  empId: string;
  type: string;
  fromDate: string;
  toDate: string;
  daysHours: string;
  shift: string;
  appliedOn: string;
  status: string;
  img: string;
  value:string;
  isSelected?: boolean;
}
export interface leavestype {
  sNo?: number;
  name: string;
  leaveQuota: string;
  createdOn: string;
  status: string;
  isSelected?: boolean;
}
export interface leavesemployee {
  sNo?: number;
  empId: string;
  type: string;
  date: string;
  duration: string;
  appliedOn: string;
  reason: string;
  approval: string;
  isSelected?: boolean;
}
export interface storeList {
  sNo?: number;
  storeName: string;
  userName: string;
  phone: string;
  email?: string;
  status?: string;
}
export interface subcategories {
  sNo?: number;
  category: string;
  parentCategory: string;
  categoryCode: string;
  description?: string;
  createdBy?: string;
  image?: string;
  isSelected?: boolean;
}
export interface supplier {
  sNo?: number;
  supplierName: string;
  code: string;
  email: string;
  phone?: string;
  country?: string;
  img: string;
  isSelected?: boolean;
}
export interface warehouse {
  sNo?: number;
  warehouse: string;
  contactPerson: string;
  phone: string;
  stock: string;
  qty: string;
  createdOn: string;
  status: string;
  totalProducts: string;
  img: string;
  isSelected?: boolean;
}

export interface purchaseReport {
  sNo: number;
  productName: string;
  purchaseAmount: string;
  purchaseQty: string;
  instockQty: string;
  img: string;
  isSelected?: boolean;
}
export interface taxrate {
  sNo?: number;
  name: string;
  taxRates: string;
  createdOn: string;
  isSelected?: boolean;
}
export interface inventoryreport {
  sNo?: number;
  productName: string;
  SKU: string;
  category: string;
  brand: string;
  unit: string;
  instockQty: string;
  img: string;
  isSelected?: boolean;
}
export interface invoicereport {
  sNo?: number;
  invoiceNo: string;
  customer: string;
  dueDate: string;
  amount: string;
  paid: string;
  amountDue: string;
  status: string;
  img: string;
  isSelected?: boolean;
}
export interface expensereport {
  sNo?: number;
  date: string;
  expenseCategory: string;
  user: string;
  amount: string;
  img: string;
  isSelected?: boolean;
}
export interface customerreport {
  sNo?: number;
  customerId: string;
  customerName: string;
  paid: string;
  amount: string;
  img: string;
  dueAmount: string;
  status: string;
  paymentStatus: string;
  isSelected?: boolean;
}
export interface incomereport {
  sNo?: number;
  date: string;
  incomeCategory: string;
  user: string;
  paymentMethod: string;
  amount: string;
  userImg: string;
  isSelected?: boolean;
}
export interface supplierreport {
  sNo?: number;
  purchaseDate: string;
  product: string;
  purchaseAmount: string;
  purchaseQty: string;
  paid: string;
  balance: string;
  status: string;
  img: string;
  date: string;
  purchase: string;
  reference: string;
  supplierName: string;
  amount: string;
  isSelected?: boolean;
  paidBy: string;
  value: string;
  dueAmount: string;
  paymentStatus: string;
}

// export interface returnreport {
//   sNo?: number;
//   reference: string;
//   supplierName: string;
//   amount: string;
//   paid: string;
//   dueAmount: string;
//   status: string;
//   paymentStatus: string;
// }

export interface permission {
  sNo?: number;
  modules: string;
  create: string;
  edit: string;
  delete: string;
  view: string;
  allowAll: string;
  isSelected?: boolean;
}
export interface taxReport {
  sNo: number;
  img1: string;
  img2: string;
  supplier: string;
  date: string;
  refNo: string;
  totalAmount: string;
  paymentMethod: string;
  discount: string;
  taxAmount: string;
  customer: string;
  invoiceNumber: string;
  isSelected?: boolean;
}
export interface pospurchase {
  sNo: number;
  date: string;
  reference: string;
  customer: string;
  amount: string;
 
}
export interface videocall {
  sNo: number;
  img: string;
  name: string;
}
