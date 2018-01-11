import { Injectable } from '@angular/core';
import { AngularFireDatabase, FirebaseListObservable, FirebaseObjectObservable } from "angularfire2/database";
import { AngularFireModule } from "angularfire2";
import * as firebase from 'firebase';

@Injectable()
export class LivetickerPostService {

    liveticker: FirebaseListObservable<any>;
    db: AngularFireDatabase;

    constructor(db: AngularFireDatabase) {
        this.db = db;
    }

    registerAsViewer(livetickerID: string, userID: string) {
        this.db.object('viewer/' + livetickerID + '/' + userID).set(firebase.database.ServerValue.TIMESTAMP);

        setInterval(() => {
            this.db.object('viewer/' + livetickerID + '/' + userID).set(firebase.database.ServerValue.TIMESTAMP);
        }, 5 * 60 * 1000);
    }

    unregisterAsViewer(livetickerID: string, userID: string) {
        this.db.object('viewer/' + livetickerID + '/' + userID).remove();
    }

    addLike(livetickerID: string, userID: string) {
        this.db.object('liked/' + livetickerID + '/' + userID).set(true);
    }

    removeLike(livetickerID: string, userID: string) {
        this.db.object('liked/' + livetickerID + '/' + userID).remove();
    }

    addComment(livetickerID: string, userID: string, content: string) {
        return this.db.list('request/' + userID + "/addComment/").push({
            "authorID": userID,
            "content": content,
            "livetickerID": livetickerID
        }).then(item => {
            return this.db.object('result/' + userID + "/addComment/" + item.key, { preserveSnapshot: true });
        });
    }
}
