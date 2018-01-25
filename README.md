# My Ticker - A free liveticker app

<italic>My Ticker is a liveticker app for Android phones and browsers.</italic>

The app is written in Java. Due to time restraints, it has not been completely finished and released, but the prototype is mostly working.

Table of Contents
=================

  * [Authentication](#authentication)
  * [Livetickers](#livetickers)
  * [Installation](#installation)
     * [Sharing Livetickers](#sharing-livetickers)
     * [Creating a Liveticker](#creating-a-liveticker)
     * [Liveticker events](#liveticker-events)
        * [Text](#text)
        * [Image](#images)
  * [Channels](#channels)
  * [Cloud functions](#cloud-functions)
     * [Notifications](#notifications)
     * [Search](#search)
     * [Queue System](#queue-system)
        * [Database Security](#database-security)

## Authentication

The authentication is being handled with Firebase. Every new user is logged in anonymously and can watch, like, share, search and comment Livetickers. 

To create a channel, subscribe to other channels and create Livetickers the user needs to register with his email or a Google account.

<strong>Login screen:</strong>

<img src="http://i.imgur.com/zXM6zNm.png" width="200">

## Livetickers

Livetickers are the core of the app. They are organised like a chat application (with reverse order). The author can post text messages or images with a caption. The viewers get the new events in realtime, due to the realtime database of Firebase.

<strong>Liveticker Interface:</strong>

<img src="http://i.imgur.com/dDOqEoH.png" width="200">

<strong>Liveticker Interface for Author:</strong>

<img src="http://i.imgur.com/R7zKY8Z.png" width="200">

### Sharing Livetickers

A liveticker can easily be shared and embedded. Users without an Android phone can open any liveticker in the Web App via the sharing link!

<strong>Sharing on Android app:</strong>

<img src="http://i.imgur.com/PTTWJab.png" width="200">

<strong>Sharing on Web App:</strong>

<img src="http://i.imgur.com/JJ7sXCM.png" width="200">


### Creating a Liveticker

Livetickers can be created after a short registration process. They have a title, a status, a state and a description. They can be started immediately or after a certain amount of time (5 days max). At the end, the Liveticker can be set to a finished state. The different states are displayed via a color (red - not started, green - live, grey - finished).

<img src="https://media.giphy.com/media/3oKIPjDq1zp65Iw1oc/giphy.gif" width="200">

### Liveticker Events

There are 2 types of Liveticker events:

#### Text

Simple text messages.

#### Images

Images from either a gallery or the cammera. Can be captioned and the file size can max. be 5 MB (enforced with Firebase storage security rules)

The interface should be as consistent as possible, so I didn't use the default camera app of the user, which looks and behaves differently on every smartphone. I used the Android Library CameraKit, which works great in most use-cases. More here: <a href="https://github.com/gogopop/CameraKit-Android">CameraKit</a>

## Channels

Every user has a channel with a profile and a title picture. There viewers can see all livetickers of the user and more info about the user.

<img src="https://media.giphy.com/media/3oKIPgld68kxeJH3wY/giphy.gif" width="200">

## Cloud functions

Every user input is handled via the Firebase cloud functions, which are functions that run in a Node.js enviroment. They operate independent of each other and can be triggered via HTTPS or a database event. I use a Queue system with the realtime database to trigger the functions and for example create a new liveticker (user posts new liveticker data to queue in database, function sanitizes/validates the input, creates the liveticker if everything is in order and posts a success or error result in the database).

### Notifications

With Firebase it was also possible to create so called Push Notifications. When a channel creates a new Liveticker, all subscribed users will get a notifications. Also if you activate notifications for a Liveticker, you will get a notification when a new event has been added. This works in the native app and in the Angular web app.

<img src="https://media.giphy.com/media/l4FGCKcczXNp8TpG8/giphy.gif" width="200">

### Search

The search function has been developed with Algolia search. The index is updated in real time when a liveticker or a user has been created. The search itself is done via a cloud function.

<img src="http://i.imgur.com/HHhFSl9.gif" width="200">

### Queue System

To create a queue 2 different paths in the database were used: <strong>Request</strong> and <strong>Result</strong>. The user writes their data in the Request path and the cloud functions write their result in the result path. The paths look like this:

```
request
   |-UserID
      |-AddLiveticker
         |-PushID
            |-values
      |-AddEvent
      |-AddComment
      |-etc
```

```
result
   |-UserID
      |-AddLiveticker
         |-PushID (Same as Request)
            |-resultValues
      |-AddEvent
      |-AddComment
      |-etc
```

Queue functions are triggered everytime something in the request branch is changed.

#### Database security

Users should not be allowed to write in the result queue or in the request queue of other users. Also, only authenticated users should be able to add requests. In other paths, most of the time users should only be allowed to read (with some exceptions, like the recently visited path).

The cloud functions with the uid "functions-admin" should be allowed to write and read in most paths (some exceptions).

Here are a few examples of Security rules used to secure the My Ticker database:

<strong>Request</strong>

```
"request": {
        ".write": "auth.uid === 'functions-admin'",
        ".read": "auth.uid === 'functions-admin'",
        "$uid": {
        "addLiveticker": {
            "$pushID": {
              ".write": "($uid === auth.uid && auth.token.email_verified === true && auth.provider !== 'anonymous') || auth.uid === 'functions-admin'",
          		".read": "($uid === auth.uid && auth.token.email_verified === true && auth.provider !== 'anonymous') || auth.uid === 'functions-admin'"
            }
          },
    			 "editLiveticker": {
            "$pushID": {
              ".write": "($uid === auth.uid && auth.token.email_verified === true && auth.provider !== 'anonymous') || auth.uid === 'functions-admin'",
          		".read": "($uid === auth.uid && auth.token.email_verified === true && auth.provider !== 'anonymous') || auth.uid === 'functions-admin'"
            }
          },
       "deleteLiveticker": {
            "$pushID": {
              ".write": "($uid === auth.uid && auth.token.email_verified === true && auth.provider !== 'anonymous') || auth.uid === 'functions-admin'",
          		".read": "($uid === auth.uid && auth.token.email_verified === true && auth.provider !== 'anonymous') || auth.uid === 'functions-admin'"
            }
          },
         "addLivetickerEvent": {
            "$pushID": {
              ".write": "($uid === auth.uid && auth.token.email_verified === true && auth.provider !== 'anonymous') || auth.uid === 'functions-admin'",
          		".read": "($uid === auth.uid && auth.token.email_verified === true && auth.provider !== 'anonymous') || auth.uid === 'functions-admin'"
            }
          },
          etc....
          },
          "$other": {
              ".validate": false
            }
        }
      }
```

<strong>Result</strong>

```
"result": {
        ".write": "auth.uid === 'functions-admin'",
          ".read": "auth.uid === 'functions-admin'",
        "$uid": {
          "addLiveticker": {
            "$pushID": {
              ".write": "auth.uid === 'functions-admin'",
          		".read": "($uid === auth.uid && auth.token.email_verified === true && auth.provider !== 'anonymous') || auth.uid === 'functions-admin'"
            }
          },
          "deleteLiveticker": {
            "$pushID": {
              ".write": "auth.uid === 'functions-admin'",
          		".read": "($uid === auth.uid && auth.token.email_verified === true && auth.provider !== 'anonymous') || auth.uid === 'functions-admin'"
            }
          },
          "addLivetickerEvent": {
            "$pushID": {
              ".write": "auth.uid === 'functions-admin'",
          		".read": "($uid === auth.uid && auth.token.email_verified === true && auth.provider !== 'anonymous') || auth.uid === 'functions-admin'"
            }
          },
          etc....
          "$other": {
              ".validate": "auth.uid === 'functions-admin'"
            }
        }
      }
```

<strong>Others</strong>

```
....
"users": {
        ".write": "auth.uid === 'functions-admin'",
        ".read": "auth != null",
        ".indexOn": "userName",
        "$uid": {
          "userName": {
          	".validate": "
        		!root.child('usernames').child(newData.val()).exists() ||
        		root.child('usernames').child(newData.val()).val() == $uid"
        	}
        }
      },
      "usernames": {
        ".read": "auth.uid === 'functions-admin'",
        "$username": {
          ".write": "(auth.uid === 'functions-admin' && !data.exists()) || (auth.uid === 'functions-admin' && data.val() === newData.val())",
        }
      },
      "lastComment": {
          ".write": "auth.uid === 'functions-admin'",
          ".read": "auth.uid === 'functions-admin'"
      },
      "comments": {
        ".write": "auth.uid === 'functions-admin'",
        ".read": "auth != null",
        "$livetickerID": {
          ".indexOn": "timestamp"
        }
      },
      "notifications": {
        "$livetickerID": {
          ".write": "auth.uid === 'functions-admin'",
            ".read": "auth.uid === 'functions-admin'",
          "$uid": {
            ".write": "$uid === auth.uid",
          	".read": "$uid === auth.uid",
          	".validate": "newData.isBoolean()"
          }
        }
      },
      "contents": {
        ".write": "auth.uid === 'functions-admin'",
        ".read": "auth != null"
      },
      "firstLogin": {
        "$uid": {
          ".write": "($uid === auth.uid && auth.token.email_verified === true && auth.provider !== 'anonymous')  || auth.uid === 'functions-admin'",
          ".read": "($uid === auth.uid && auth.token.email_verified === true && auth.provider !== 'anonymous')  || auth.uid === 'functions-admin'",
          ".validate": "newData.isBoolean()"
        }
      },
      "liked": {
        "$livetickerID": {
          "$uid": {
            ".write": "$uid === auth.uid || auth.uid === 'functions-admin'",
          	".read": "$uid === auth.uid || auth.uid === 'functions-admin'",
          	".validate": "newData.isBoolean()"
          }
        }
      },
      .....
```
