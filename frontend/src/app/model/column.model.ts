import {Tache} from "./tache.model";

export class Column {
  constructor(public name: string, public tasks: Tache[]) {}
}
