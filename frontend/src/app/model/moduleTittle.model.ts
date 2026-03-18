import {Module} from "./module.model";

export interface ModuleTittle {
  id: number;
  moduleTittle: string;
  modules: Module[];
  isSelected?: boolean;

}
