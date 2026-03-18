import {SousModule} from "./sousModule.model";
import {Permission} from "./permission.model";

export interface Module {
  id: number;
  title: string;
  sousModules: SousModule[];
  permission?: Permission[];
  status?:boolean;
}

