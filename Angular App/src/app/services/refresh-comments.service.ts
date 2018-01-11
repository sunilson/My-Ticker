import { Observable } from 'rxjs/Observable';
import { Liveticker } from './../liveticker';
import { Injectable } from '@angular/core';
import { Subject } from 'rxjs/Subject';
import * as firebase from 'firebase';


@Injectable()
export class RefreshCommentsService {

    private refreshCommentsSource = new Subject<boolean>();

    refreshComments = this.refreshCommentsSource.asObservable();

    constructor() { }

    refresh() {
        this.refreshCommentsSource.next(true);
    }
}
