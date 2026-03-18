import { Module } from "./module.model";
import {Role} from "./role.model";

export interface Permission {
  id?: number;

  create: boolean;
  update: boolean;
  view: boolean;
  delete:  boolean;

  module?: Module;
  role?:   Role;
}
