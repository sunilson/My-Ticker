import { Liveticker } from './../liveticker';
import { Injectable } from '@angular/core';
import { Subject } from 'rxjs/Subject';
import * as firebase from 'firebase';


@Injectable()
export class LivetickerControlService {

  private livetickerChangedSource = new Subject<Liveticker>();
  private userChangedSource = new Subject<firebase.User>();
  private livetickerIDChangedSource = new Subject<string[]>();

  livetickerChanged = this.livetickerChangedSource.asObservable();
  userChanged = this.userChangedSource.asObservable();
  livetickerIDChanged = this.livetickerIDChangedSource.asObservable();

  constructor() { }

  livetickerChange(liveticker: Liveticker) {
    this.livetickerChangedSource.next(liveticker);
  }

  userChange(user: firebase.User) {
    this.userChangedSource.next(user);
  }

  livetickerIDChange(id: string, name: string) {
    this.livetickerIDChangedSource.next([id, name]);
  }
}
