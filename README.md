# My Ticker - A free liveticker app

<italic>My Ticker is a free liveticker app for Android phones and browsers.</italic>

The app is written in Java. Due to time and monetary restraints, it has not been released, but the prototype is fully working.

For the web app written in Angular see here: <a href="https://github.com/sunilson/My-Ticker-Angular-App">https://github.com/sunilson/My-Ticker-Angular-App</a>

For the server side cloud functions see here: <a href="https://github.com/sunilson/My-Ticker-Cloud-Functions">https://github.com/sunilson/My-Ticker-Cloud-Functions</a>

## Authentication

The authentication is being handled with Firebase. Every new user is logged in anonymously and can already watch, like, share, search and comment Livetickers. 

To create a channel, subscribe to other channels and creae Livetickers the user needs to register with his email or a Google account.

## Livetickers

Livetickers are the core of the app. They are organised like a chat application (with reverse order). The author can post text messages or images with a caption. The viewers get the new events in realtime, due to the realtime database of Firebase.

### Sharing Livetickers

A liveticker can easily be shared and embedded. Users without an Android phone can open any liveticker in the Web App via the sharing link!

### Creating a Liveticker

Livetickers can be created after a short registration process. They have a title, a status, a state and a description. They can be started immediately or after a certain amount of time (5 days max). At the end, the Liveticker can be set to a finished state. The different states are displayed via a color (red - not started, green - live, grey - finished).

### Camera functionality

TODO

## Cloud functions

Every user input is handled via the Firebase cloud functions, which are functions that run in a Node.js enviroment. They operate independent of each other and can be triggered via HTTPS or a database event. I use a Queue system with the realtime database to trigger the functions and for example create a new liveticker (user posts new liveticker data to queue in database, function sanitizes/validates the input, creates the liveticker if everything is in order and posts a success or error result in the database).

### Notifications

With Firebase it was also possible to create so called Push Notifications. When a channel creates a new Liveticker, all subscribed users will get a notifications. Also if you activate notifications for a Liveticker, you will get a notification when a new event has been added. This works in the native app and in the Angular web app.

### Search

The search function has been developed with Algolia search. The index is updated in real time when a liveticker or a user has been created. The search itself is done via a cloud function.

### Database security

TODO
