import { Injectable } from '@angular/core';
import { AngularFireDatabase, FirebaseListObservable, FirebaseObjectObservable } from "angularfire2/database";
import { AngularFireModule } from "angularfire2";

@Injectable()
export class LivetickerDataService {

  liveticker: FirebaseListObservable<any>;
  db: AngularFireDatabase;

  constructor(db: AngularFireDatabase) {
    this.db = db;
  }

  getLiveticker(id: string) {
    return this.db.object('liveticker/' + id, { preserveSnapshot: true });
  }

  getLikedLiveticker(livetickerID: string, userID: string) {
    return this.db.object('liked/' + livetickerID + '/' + userID, { preserveSnapshot: true });
  }

  getLivetickerContents(id: string) {
    return this.db.list('contents/' + id);
  }

  getLivetickerNotification(userID: string, livetickerID: string) {
    return this.db.object('notifications/' + livetickerID + "/" + userID, { preserveSnapshot: true });
  }

  getLivetickerAuthor(id: string) {
    return this.db.object('users/' + id, { preserveSnapshot: true });
  }

  getLivetickerComments(livetickerID: string, userID: string) {
    return this.db.list('request/' + userID + "/comments/").push({ "livetickerID": livetickerID }).then(item => {
      return this.db.object('result/' + userID + "/comments/" + item.key, { preserveSnapshot: true });
    });
  }

  getLivetickerCommentUpdates(livetickerID: string) {
    return this.db.list('comments/' + livetickerID);
  }

  getLivetickerViewerCount(id: string) {
    return this.db.object('viewerCount/' + id, { preserveSnapshot: true });
  }

  formatDate(timestamp: number) {
    var date = new Date(timestamp);
    return (date.getDate() < 10 ? '0' : '') + date.getDate() + "." + ((date.getMonth() + 1 < 10 ? '0' : '')) + (date.getMonth() + 1) + "." + date.getFullYear().toString().substr(-2);
  }

  formatTime(timestamp: number) {
    var date = new Date(timestamp);
    return (date.getHours() < 10 ? '0' : '') + date.getHours() + ":" + (date.getMinutes() < 10 ? '0' : '') + date.getMinutes();
  }
}
