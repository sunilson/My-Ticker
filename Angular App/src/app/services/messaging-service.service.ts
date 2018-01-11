import { Injectable } from '@angular/core';
import { AngularFireDatabase, FirebaseListObservable, FirebaseObjectObservable } from "angularfire2/database";
import { AngularFireModule } from "angularfire2";
import * as firebase from 'firebase';
import { SimpleNotificationsModule, NotificationsService } from 'angular2-notifications';

@Injectable()
export class MessagingService {

    private messaging: firebase.messaging.Messaging;

    constructor(private db: AngularFireDatabase, private service: NotificationsService) {
        this.messaging = firebase.messaging();
    }

    toggleNotifications(userID: string, livetickerID: string, notificationState: boolean) {
        this.messaging.requestPermission().then(() => {
            this.messaging.getToken().then((currentToken) => {
                if (currentToken) {
                    this.db.object('registrationTokens/' + userID + "/" + currentToken).set(true);
                    if (!notificationState) {
                        this.db.object('notifications/' + livetickerID + "/" + userID).set(true).then(result => {
                            this.service.success(
                                'Done!',
                                'You will now recieve notifications!',
                                {
                                    timeOut: 5000,
                                    showProgressBar: true,
                                    pauseOnHover: true,
                                    clickToClose: true,
                                    maxLength: 0
                                }
                            );
                        }).catch((e) => {
                            this.service.error(
                                'Error',
                                'Database error!',
                                {
                                    timeOut: 5000,
                                    showProgressBar: true,
                                    pauseOnHover: true,
                                    clickToClose: true,
                                    maxLength: 0
                                }
                            );
                        });
                    } else {
                        this.db.object('notifications/' + livetickerID + "/" + userID).remove().then(result => {
                            this.service.warn(
                                'Done!',
                                'You will no longer recieve notifications!',
                                {
                                    timeOut: 5000,
                                    showProgressBar: true,
                                    pauseOnHover: true,
                                    clickToClose: true,
                                    maxLength: 0
                                }
                            );
                        }).catch((e) => {
                            this.service.error(
                                'Error',
                                'Database error!',
                                {
                                    timeOut: 5000,
                                    showProgressBar: true,
                                    pauseOnHover: true,
                                    clickToClose: true,
                                    maxLength: 0
                                }
                            );
                        });
                    }
                } else {
                    this.service.error(
                        'Error',
                        'Database error!',
                        {
                            timeOut: 5000,
                            showProgressBar: true,
                            pauseOnHover: true,
                            clickToClose: true,
                            maxLength: 0
                        }
                    );
                }
            }).catch((e) => {
                this.service.error(
                    'Error',
                    'Registration Token Error!',
                    {
                        timeOut: 5000,
                        showProgressBar: true,
                        pauseOnHover: true,
                        clickToClose: true,
                        maxLength: 0
                    }
                );
            });
        }).catch((e) => {
            this.service.error(
                'Error',
                'No permission for notifications!',
                {
                    timeOut: 5000,
                    showProgressBar: true,
                    pauseOnHover: true,
                    clickToClose: true,
                    maxLength: 0
                }
            );
            //alert("No permission for notifications! You won't recieve notifications from this Liveticker until you gave permission.");
        });
    }

    requestNotificationPermission(userID: string) {
        this.messaging.requestPermission().then(() => {
            this.messaging.getToken().then((currentToken) => {
                if (currentToken) {
                    this.db.object('registrationTokens/' + userID + "/" + currentToken).set(true);
                } else {
                    this.service.error(
                        'Error',
                        'Database error!',
                        {
                            timeOut: 5000,
                            showProgressBar: true,
                            pauseOnHover: true,
                            clickToClose: true,
                            maxLength: 0
                        }
                    );
                }
            }).catch((e) => {
                this.service.error(
                    'Error',
                    'Registration Token Error!',
                    {
                        timeOut: 5000,
                        showProgressBar: true,
                        pauseOnHover: true,
                        clickToClose: true,
                        maxLength: 0
                    }
                );
            });
        }).catch((e) => {
            this.service.error(
                'Error',
                'No permission for notifications!',
                {
                    timeOut: 5000,
                    showProgressBar: true,
                    pauseOnHover: true,
                    clickToClose: true,
                    maxLength: 0
                }
            );
            //alert("No permission for notifications! You won't recieve notifications from this Liveticker until you gave permission.");
        });
    }
}
