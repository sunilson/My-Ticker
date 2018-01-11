import { Injectable } from '@angular/core';
import {AngularFireDatabase, FirebaseListObservable} from "angularfire2/database";
import {AngularFireModule} from "angularfire2";

@Injectable()
export class LivetickerListDataService {

  livetickers: FirebaseListObservable<any[]>;

  constructor(db: AngularFireDatabase) {
    this.livetickers = db.list('/liveticker');
  }

  getLivetickerList() {
    return this.livetickers
  }
}
