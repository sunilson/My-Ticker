// Give the service worker access to Firebase Messaging.
// Note that you can only use Firebase Messaging here, other Firebase libraries
// are not available in the service worker.
importScripts('https://www.gstatic.com/firebasejs/3.9.0/firebase-app.js');
importScripts('https://www.gstatic.com/firebasejs/3.9.0/firebase-messaging.js');

// Initialize the Firebase app in the service worker by passing in the
// messagingSenderId.
firebase.initializeApp({
    'messagingSenderId': '1059975207216'
});

// Retrieve an instance of Firebase Messaging so that it can handle background
// messages.
const messaging = firebase.messaging();

messaging.setBackgroundMessageHandler(function (payload) {

    var livetickerTitle = payload.data.title;
    var content = payload.data.content;
    var contentType = payload.data.contentType;
    var notificationTitle = "";
    var notificationOptions = {};
    var livetickerID = payload.data.livetickerID.substring(1);

    if (contentType === "text") {
        notificationTitle = 'New Liveticker event!';
        notificationOptions = {
            body: content,
            icon: 'assets/img/icon.jpg'
        };
    } else if (contentType === "image") {
        notificationTitle = 'New Liveticker event!';
        notificationOptions = {
            body: "Image was added",
            icon: 'assets/img/icon.jpg'
        };
    } else if (contenType === "state") {
        notificationTitle = 'New Liveticker event!';
        notificationOptions = {
            body: "State changed",
            icon: 'assets/img/icon.jpg'
        };
    }

    self.addEventListener('notificationclick', (event) => {
        event.notification.close();
        event.waitUntil(clients.openWindow("https://my-ticker.live/liveticker/" + livetickerID));
    });

    return self.registration.showNotification(notificationTitle,
        notificationOptions);
});