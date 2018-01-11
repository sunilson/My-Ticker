const functions = require('firebase-functions');
const request = require('request');
const algoliasearch = require('algoliasearch');
const admin = require('firebase-admin');
const generateUsername = require('project-name-generator');
const schedule = require(__dirname + '/livetickerSchedule.js');
const liveticker = require(__dirname + '/liveticker.js');
const comment = require(__dirname + '/comment.js');
const userClass = require(__dirname + '/user.js');
const livetickerEvent = require(__dirname + '/livetickerEvent.js');
var utilities = require(__dirname + '/utilities.js');

//configure google cloud storage
var gcloud = require('@google-cloud/storage')({
  projectID: 'pro4-84e1f',
  keyFilename: 'PRO4-c353759e6e2b.json'
});

var bucket = gcloud.bucket('pro4-84e1f.appspot.com');

// configure algolia
var algolia = algoliasearch("E8K8UVW4LL", "4c873f8da1ac0956e272cf72ffbe8f7f");
var index = algolia.initIndex('liveticker');
var channelIndex = algolia.initIndex('channels');

var firebaseConfig = functions.config().firebase;
firebaseConfig.databaseAuthVariableOverride = {
  uid: 'functions-admin'
};

admin.initializeApp(firebaseConfig);

/************************************************/
/*************** Liveticker Push ****************/
/***********************************************/

exports.liveticker = functions.database.ref('/liveticker/{pushID}').onWrite(event => {

  //Liveticker was deleted
  if (!event.data.exists() && event.data.previous.exists()) {

    console.log("Deleting: " + event.params.pushID);

    return index.deleteObject(event.params.pushID).then(() => {
      return admin.database().ref("/contents/" + event.params.pushID).remove().then(() => {
        return admin.database().ref("/liked/" + event.params.pushID).remove().then(() => {
          return admin.database().ref("/comments/" + event.params.pushID).remove().then(() => {
            return bucket.deleteFiles({
              prefix: 'livetickerImages/' + event.data.previous.val().authorID + "/" + event.params
                .pushID,
              force: true
            });
          });
        });
      });
    });
  }

  //Liveticker is changed or added
  if (event.data.exists()) {
    if (event.data.val().authorID) {
      if (!event.data.previous.exists() || event.data.val().likeCount === event.data.previous.val().likeCount) {
        //Liveticker added or changed. Index it
        var firebaseObject = event.data.val();
        firebaseObject.objectID = event.params.pushID;
        index.saveObject(firebaseObject);
      }
    }

    if (!event.data.previous.exists()) {
      return admin.database().ref("subscriptions/" + event.data.val().authorID).once('value').then((snapshot) => {
        let promises = [];

        snapshot.forEach((childSnapshot) => {
          promises.push(createGetTokensPromise(childSnapshot.key));
        });

        return Promise.all(promises).then((result) => {
          let tokens = [];

          result.forEach((userTokens) => {
            if (userTokens) {
              for (var key in userTokens.val()) {
                if (key) {
                  tokens.push(key);
                }
              }
            }
          });

          return admin.database().ref("users/" + event.data.val().authorID).once("value").then((snapshot) => {
            var payload = {
              data: {
                type: "livetickerAdded",
                livetickerID: event.params.pushID,
                authorID: event.data.val().authorID,
                title: event.data.val().title,
                author: snapshot.val().userName
              }
            };
            return admin.messaging().sendToDevice(tokens, payload).then((response) => {
              console.log("Success");
            });
          });
        });
      });
    } else {
      return;
    }
  }
});

/************************************************/
/***************** Events Push ******************/
/***********************************************/

exports.events = functions.database.ref('/contents/{livetickerID}/{pushID}').onWrite(event => {
  if (event.data.exists() && !event.data.previous.exists()) {

    //Creation Event shouldn't send out Notification. This is handled by Liveticker Push
    if (event.data.val().contentType === "state" && event.data.val().content === "created") {
      return;
    }

    return admin.database().ref("notifications/" + event.params.livetickerID).once('value').then((snapshot) => {
      let promises = [];
      snapshot.forEach((childSnapshot) => {
        promises.push(createGetTokensPromise(childSnapshot.key));
      });

      return Promise.all(promises).then((result) => {
        let tokens = [];

        result.forEach((userTokens) => {
          if (userTokens) {
            for (var key in userTokens.val()) {
              if (key) {
                tokens.push(key);
              }
            }
          }
        });

        return admin.database().ref("liveticker/" + event.params.livetickerID).once("value").then((snapshot) => {
          var payload = {};
          if (event.data.val().type === "image") {
            payload = {
              data: {
                type: "livetickerEventAdded",
                contentType: event.data.val().type,
                livetickerID: event.params.livetickerID,
                title: snapshot.val().title,
                authorID: snapshot.val().authorID,
                caption: event.data.val().caption,
                thumbnail: event.data.val().thumbnail
              }
            };
          } else if (event.data.val().type == "text") {
            payload = {
              data: {
                type: "livetickerEventAdded",
                contentType: event.data.val().type,
                livetickerID: event.params.livetickerID,
                title: snapshot.val().title,
                authorID: snapshot.val().authorID,
                content: event.data.val().content
              }
            };
          } else {
            payload = {
              data: {
                type: "livetickerEventAdded",
                contentType: event.data.val().type,
                livetickerID: event.params.livetickerID,
                title: snapshot.val().title,
                authorID: snapshot.val().authorID,
                content: event.data.val().content
              }
            };
          }

          return admin.messaging().sendToDevice(tokens, payload).then((response) => {
            console.log("Success");
          });
        });
      });
    });
  }
});

/************************************************/
/***************** Authors Push ******************/
/***********************************************/

exports.users = functions.database.ref('/users/{userID}/').onWrite(event => {
  //User was added or changed
  if (event.data.exists()) {
    var firebaseObject = event.data.val();
    if (firebaseObject.userName !== "Anonymous") {
      firebaseObject.objectID = event.params.userID;
      return channelIndex.saveObject(firebaseObject);
    }
  }

  //User was deleted
  if (!event.data.exists() && event.data.previous.exists()) {

    var objectID = event.params.userID;
    // Remove the object from Algolia
    return channelIndex.deleteObject(objectID);
  }
});

/************************************************/
/******************* Search ********************/
/***********************************************/

exports.search = functions.database.ref('/request/{userID}/search/{pushID}').onWrite(event => {
  var resultsLiveticker = [];
  var resultsChannel = [];

  if (event.data.exists()) {
    var searchVal = utilities.sanitizeString(event.data.val());

    var queries = [{
      indexName: 'liveticker',
      query: searchVal,
      restrictSearchableAttributes: [
        'title',
        'status',
        'description'
      ],
      length: 50,
    }, {
      indexName: 'channels',
      query: searchVal,
      restrictSearchableAttributes: [
        'username'
      ],
      length: 10,
    }];

    return algolia.search(queries).then((result) => {
      for (var h in result.results[0].hits) {
        resultsLiveticker.push(result.results[0].hits[h]);
      }

      for (var h in result.results[1].hits) {
        resultsChannel.push(result.results[1].hits[h]);
      }

      var authorIDs = [];
      resultsLiveticker.forEach((value) => {
        authorIDs[value.authorID] = true;
      });

      var promises = [];
      for (var key in authorIDs) {
        promises.push(createUserDetailsPromise(key));
      }

      return Promise.all(promises);
    }).then((promisesResult) => {
      var authors = [];
      promisesResult.forEach((value) => {
        authors[value.authorID] = value;

      });

      resultsLiveticker.forEach((value) => {
        value.livetickerID = value.objectID;
        value.userName = authors[value.authorID].userName;
        value.profilePicture = authors[value.authorID].profilePicture;
      });

      resultsChannel.forEach((value) => {
        value.userID = value.objectID;
      });

      return admin.database().ref("/result/" + event.params.userID + "/search/" + event.params
        .pushID).set({
        state: "success",
        timestamp: Date.now(),
        livetickerResults: utilities.toObject(resultsLiveticker),
        channelResults: utilities.toObject(resultsChannel)
      });
    }).catch((e) => {
      return admin.database().ref("/result/" + event.params.userID + "/search/" + event.params.pushID)
        .set({
          state: "error",
          timestamp: Date.now(),
          errorDetails: e
        });
    });
  }
});

var searchLiveticker = function(searchValue) {
  return new Promise(function(resolve, reject) {
    return index.search({
      query: searchValue,
      restrictSearchableAttributes: [
        'title',
        'status',
        'description'
      ]
    }).then((result) => {
      resolve(result);
    }).catch((e) => {
      reject();
    });
  });
}

var searchChannel = function(searchValue) {
  return new Promise(function(resolve, reject) {
    return channelIndex.search({
      query: searchValue,
      restrictSearchableAttributes: [
        'userName'
      ]
    }).then((result) => {
      resolve(result);
    }).catch((e) => {
      reject();
    });
  });
}

/************************************************/
/*************** Add Liveticker ****************/
/***********************************************/

exports.addLiveticker = functions.database.ref('/request/{userID}/addLiveticker/{pushID}').onWrite(event => {
  if (event.data.val() != null && event.params.pushID != null) {

    var tempLiveticker = new liveticker(event.data.val());
    tempLiveticker.setAuthorID(event.params.userID);

    //Check validity of liveticker contents
    var validateResult = tempLiveticker.validate();
    if (validateResult !== true) {
      return admin.database().ref("/result/" + event.params.userID + "/addLiveticker/" + event.params.pushID)
        .set({
          state: "error",
          timestamp: Date.now(),
          errorDetails: validateResult
        });
    }

    //Add Liveticker to database
    return admin.database().ref("liveticker/").push(tempLiveticker.getData()).then((snapshot) => {
      //Start Liveticker Schedule if start date is in the future
      if (tempLiveticker.getData().state !== "started") {
        //Push to Start Schedule Queue
        admin.database().ref("/queue/scheduleLivetickerStart/tasks").push({
          livetickerID: snapshot.key,
          authorID: tempLiveticker.getData().authorID,
          date: tempLiveticker.getData().stateTimestamp
        }).then((egal) => {
          return admin.database().ref("/contents/" + snapshot.key).push({
            authorID: tempLiveticker.getData().authorID,
            type: "state",
            content: "created",
            timestamp: Date.now(),
            livetickerID: snapshot.key
          }).then((snap) => {
            return admin.database().ref("/result/" + event.params.userID + "/addLiveticker/" + event.params
              .pushID).set({
              state: "success",
              timestamp: Date.now(),
              successDetails: snapshot.key
            });
          }).catch((e) => {
            return admin.database().ref("/result/" + event.params.userID + "/addLiveticker/" + event.params
              .pushID).set({
              state: "error",
              timestamp: Date.now(),
              errorDetails: "Error adding Liveticker!"
            });
          });
        }).catch((e) => {
          return admin.database().ref("/result/" + event.params.userID + "/addLiveticker/" + event.params
            .pushID).set({
            state: "error",
            timestamp: Date.now(),
            errorDetails: "Error adding Liveticker!"
          });
        });
      } else {
        var promise1 = admin.database().ref("/contents/" + snapshot.key).push({
          authorID: tempLiveticker.getData().authorID,
          type: "state",
          content: "created",
          timestamp: Date.now(),
          livetickerID: snapshot.key
        });

        var promise2 = admin.database().ref("/contents/" + snapshot.key).push({
          authorID: tempLiveticker.getData().authorID,
          type: "state",
          content: "started",
          timestamp: Date.now(),
          livetickerID: snapshot.key
        });

        return Promise.all([promise1, promise2]).then((result) => {
          return admin.database().ref("/result/" + event.params.userID + "/addLiveticker/" + event.params
            .pushID).set({
            state: "success",
            timestamp: Date.now(),
            successDetails: snapshot.key
          });
        }).catch((e) => {
          return admin.database().ref("/result/" + event.params.userID + "/addLiveticker/" + event.params
            .pushID).set({
            state: "error",
            timestamp: Date.now(),
            errorDetails: "Error adding Liveticker!"
          });
        });
      }
    }).catch((e) => {
      //Error adding the Liveticker to the database
      return admin.database().ref("/result/" + event.params.userID + "/addLiveticker/" + event.params.pushID)
        .set({
          state: "error",
          timestamp: Date.now(),
          errorDetails: "Error adding Liveticker!"
        });
    });
  }
});

/************************************************/
/************* Delete Liveticker ****************/
/***********************************************/

exports.deleteLiveticker = functions.database.ref('/request/{userID}/deleteLiveticker/{pushID}').onWrite(event => {
  if (event.data.exists()) {
    return admin.database().ref("/liveticker/" + event.data.val().livetickerID).once("value").then((snapshot) => {
      if (event.params.userID === snapshot.val().authorID) {
        return admin.database().ref("/liveticker/" + event.data.val().livetickerID).remove().then((result) => {
          return admin.database().ref("/result/" + event.params.userID + "/deleteLiveticker/" + event
            .params
            .pushID).set({
            state: "success",
            timestamp: Date.now()
          });
        }).catch((e) => {
          return admin.database().ref("/result/" + event.params.userID + "/deleteLiveticker/" + event
            .params
            .pushID).set({
            state: "error",
            errorDetails: e,
            timestamp: Date.now()
          });
        });
      } else {
        return admin.database().ref("/result/" + event.params.userID + "/deleteLiveticker/" + event.params
          .pushID).set({
          state: "error",
          errorDetails: "No permission to delete liveticker!",
          timestamp: Date.now()
        });
      }
    }).catch((e) => {
      return admin.database().ref("/result/" + event.params.userID + "/deleteLiveticker/" + event.params
        .pushID).set({
        state: "error",
        errorDetails: e,
        timestamp: Date.now()
      });
    });
  }
});

/************************************************/
/***************** Add Event *******************/
/***********************************************/
//Sanitize Liveticker Events and create Push notifications
exports.addLivetickerEvent = functions.database.ref('/request/{userID}/addLivetickerEvent/{pushID}').onWrite(event => {
  if (event.data.exists()) {
    var tempEvent = new livetickerEvent(event.data.val());

    tempEvent.sanitize();

    var validate = tempEvent.validate();
    if (validate !== true) {
      return admin.database().ref("/result/" + event.params.userID + "/addLivetickerEvent/" + event.params
        .pushID).set({
        state: "error",
        errorDetails: validate,
        timestamp: Date.now()
      });
    }

    return admin.database().ref("liveticker/" + event.data.val().livetickerID).once("value").then((snapshot) => {
      if (snapshot.val().authorID === event.params.userID && snapshot.val().state === "started") {
        return admin.database().ref("contents/" + event.data.val().livetickerID).push(tempEvent.getEventDataForStorage())
          .then((result) => {
            return admin.database().ref("/result/" + event.params.userID + "/addLivetickerEvent/" + event.params
              .pushID).set({
              state: "success",
              timestamp: Date.now()
            });
          }).catch((e) => {
            return admin.database().ref("/result/" + event.params.userID + "/addLivetickerEvent/" + event.params
              .pushID).set({
              state: "error",
              errorDetails: e,
              timestamp: Date.now()
            });
          });
      } else {
        return admin.database().ref("/result/" + event.params.userID + "/addLivetickerEvent/" + event.params
          .pushID).set({
          state: "error",
          errorDetails: "No permission!",
          timestamp: Date.now()
        });
      }
    }).catch((e) => {
      return admin.database().ref("/result/" + event.params.userID + "/addLivetickerEvent/" + event.params
        .pushID).set({
        state: "error",
        errorDetails: e,
        timestamp: Date.now()
      });
    });
  }
});

/************************************************/
/************ Toggle Liveticker State ***********/
/***********************************************/

exports.toggleLivetickerState = functions.database.ref('/request/{userID}/toggleState/{pushID}').onWrite(event => {
  if (event.data.exists()) {
    return admin.database().ref("liveticker/" + event.data.val().livetickerID).once('value').then((snapshot) => {
      if (snapshot.val().authorID === event.params.userID) {
        if (snapshot.val().state === "not started") {
          return admin.database().ref("liveticker/" + event.data.val().livetickerID).update({
            state: "started",
            stateTimestamp: Date.now(),
            idState: event.params.userID + "ab"
          }).then((result) => {
            return admin.database().ref("contents/" + event.data.val().livetickerID).push({
              authorID: event.params.userID,
              type: "state",
              content: "started",
              timestamp: Date.now(),
              livetickerID: event.data.val().livetickerID
            });
          }).then((value) => {
            return admin.database().ref("/result/" + event.params.userID + "/toggleState/" + event.params
              .pushID).set({
              state: "success",
              timestamp: Date.now()
            });
          }).catch((e) => {
            return admin.database().ref("/result/" + event.params.userID + "/toggleState/" + event.params
              .pushID).set({
              state: "error",
              errorDetails: e,
              timestamp: Date.now()
            });
          });
        } else if (snapshot.val().state === "started") {
          return admin.database().ref("liveticker/" + event.data.val().livetickerID).update({
              state: "finished",
              stateTimestamp: Date.now(),
              idState: event.params.userID + "c"
            })
            .then((result) => {
              return admin.database().ref("contents/" + event.data.val().livetickerID).push({
                authorID: event.params.userID,
                type: "state",
                content: "finished",
                timestamp: Date.now(),
                livetickerID: event.data.val().livetickerID
              });
            }).then((value) => {
              return admin.database().ref("/result/" + event.params.userID + "/toggleState/" + event.params
                .pushID).set({
                state: "success",
                timestamp: Date.now()
              });
            }).catch((e) => {
              return admin.database().ref("/result/" + event.params.userID + "/toggleState/" + event.params
                .pushID).set({
                state: "error",
                errorDetails: e,
                timestamp: Date.now()
              });
            });
        } else {
          return admin.database().ref("/result/" + event.params.userID + "/toggleState/" + event.params
            .pushID).set({
            state: "error",
            errorDetails: "Liveticker state can't be toggled!",
            timestamp: Date.now()
          });
        }
      }
    }).catch((e) => {
      return admin.database().ref("/result/" + event.params.userID + "/toggleState/" + event.params
        .pushID).set({
        state: "error",
        errorDetails: e,
        timestamp: Date.now()
      });
    });
  }
});

/************************************************/
/*********** Add to Recently Visited  **********/
/***********************************************/
/*
exports.addToRecentlyVisited = functions.database.ref('/request/{userID}/addToRecentlyVisited/{pushID}').onWrite(
  event => {
    if (event.data.exists()) {
      return admin.database().ref('recentlyVisited/' + event.params.userID + "/" + event.data.val().livetickerID).set({
        timestamp: Date.now()
      });
    }
  });
  */
/************************************************/
/*************** Edit Liveticker ****************/
/***********************************************/

exports.editLiveticker = functions.database.ref('/request/{userID}/editLiveticker/{pushID}').onWrite(event => {
  if (event.data.exists()) {

    return admin.database().ref('liveticker/' + event.data.val().livetickerID).once('value').then(liveticker => {
      if (event.params.userID === liveticker.val().authorID) {
        var result = {};

        if (event.data.val().title) {
          let title = utilities.sanitizeString(event.data.val().title);
          if (utilities.checkValidString(title, false, 5, 30)) {
            result["title"] = title;
          } else {
            return admin.database().ref("/result/" + event.params.userID + "/editLiveticker/" + event.params
              .pushID).set({
              state: "error",
              timestamp: Date.now()
            });
          }
        }

        if (event.data.val().description) {
          let description = utilities.sanitizeString(event.data.val().description);
          if (utilities.checkValidString(description, true, 0, 999)) {
            result["description"] = description;
          } else {
            return admin.database().ref("/result/" + event.params.userID + "/editLiveticker/" + event.params
              .pushID).set({
              state: "error",
              timestamp: Date.now()
            });
          }
        }

        if (event.data.val().status) {
          let status = utilities.sanitizeString(event.data.val().status);
          if (utilities.checkValidString(status, true, 0, 140)) {
            result["status"] = status;
          } else {
            return admin.database().ref("/result/" + event.params.userID + "/editLiveticker/" + event.params
              .pushID).set({
              state: "error",
              timestamp: Date.now()
            });
          }
        }

        if (result) {
          return admin.database().ref('liveticker/' + event.data.val().livetickerID).update(result).then(result => {
            return admin.database().ref("/result/" + event.params.userID + "/editLiveticker/" + event.params
              .pushID).set({
              state: "success",
              timestamp: Date.now()
            });
          });
        } else {
          return admin.database().ref("/result/" + event.params.userID + "/editLiveticker/" + event.params
            .pushID).set({
            state: "error",
            timestamp: Date.now()
          });
        }
      } else {
        return admin.database().ref("/result/" + event.params.userID + "/editLiveticker/" + event.params
          .pushID).set({
          state: "error",
          timestamp: Date.now()
        });
      }
    });
  }
});

/************************************************/
/**************** Edit Channel ******************/
/***********************************************/

exports.editChannel = functions.database.ref('/request/{userID}/editChannel/{pushID}').onWrite(event => {
  if (event.data.exists()) {
    var tempUser = new userClass(event.data.val());
    tempUser.sanitize();
    var validation = tempUser.validate();

    if (validation !== true) {
      return admin.database().ref("/result/" + event.params.userID + "/editChannel/" + event.params
        .pushID).set({
        state: "error",
        errorDetails: validation,
        timestamp: Date.now()
      });
    }

    admin.database().ref("usernames/" + tempUser.getData().userName).set(event.params.userID).then((result) => {
      admin.database().ref("users/" + event.params.userID).set(tempUser.getData()).then((snapshot) => {
        return admin.database().ref("/result/" + event.params.userID + "/editChannel/" + event.params
          .pushID).set({
          state: "success",
          timestamp: Date.now()
        });
      }).catch((e) => {
        return admin.database().ref("/result/" + event.params.userID + "/editChannel/" + event.params
          .pushID).set({
          state: "error",
          errorDetails: "Username already exists!",
          timestamp: Date.now()
        });
      });
    }).catch((e) => {
      return admin.database().ref("/result/" + event.params.userID + "/editChannel/" + event.params
        .pushID).set({
        state: "error",
        errorDetails: "Username already exists!",
        timestamp: Date.now()
      });
    });
  }
});

/************************************************/
/************ Subscribe-To Listener ************/
/***********************************************/

exports.subscribedTo = functions.database.ref('/subscribedTo/{userID}/{pushID}').onWrite(event => {
  if (event.data.exists()) {
    //Verify user account. If not valid, delete subscribed to and subscription entry
    return admin.auth().getUser(event.params.pushID).then((userRecord) => {
      return admin.database().ref("subscriptions/" + event.params.pushID + "/" + event.params.userID).set(
        true);
    }).catch((error) => {
      //User invalid. Delete both entries
      return Promise.all([deleteSubscribedTo(event.params.pushID, event.params.userID), deleteSubscription(
        event.params.pushID, event.params.userID)]);
    });
  } else {
    return admin.database().ref("subscriptions/" + event.params.pushID + "/" + event.params.userID).remove();
  }
});

//Return Promise of all livetickers of given user
var deleteSubscribedTo = function(channel, user) {
  return new Promise(function(resolve, reject) {
    admin.database().ref("subscribedTo/" + user + "/" + channel).remove().then((snap) => {
      resolve();
    }).catch((e) => {
      reject();
    });
  });
}

//Return Promise of all livetickers of given user
var deleteSubscription = function(channel, user) {
  return new Promise(function(resolve, reject) {
    admin.database().ref("subscriptions/" + channel + "/" + user).remove().then((snap) => {
      resolve();
    }).catch((e) => {
      reject();
    });
  });
}

/************************************************/
/************ Subscription Listener ************/
/***********************************************/

exports.subscriptions = functions.database.ref('/subscriptions/{userID}/{pushID}').onWrite(event => {

  //Increment subscription counter
  admin.database().ref("users/" + event.params.userID + "/subscriberCount").transaction((current) => {
    if (event.data.exists() && !event.data.previous.exists()) {
      return (current || 0) + 1;
    } else if (!event.data.exists() && event.data.previous.exists()) {
      return (current || 0) - 1;
    }
  });
});


/************************************************/
/**************** Like Count  ******************/
/***********************************************/

exports.likeCount = functions.database.ref('/liked/{livetickerID}/{userID}').onWrite(event => {
  return admin.database().ref("liveticker/" + event.params.livetickerID + "/likeCount").transaction(
    (current) => {
      if (event.data.exists() && !event.data.previous.exists()) {
        return (current || 0) + 1;
      } else if (!event.data.exists() && event.data.previous.exists()) {
        return (current || 0) - 1;
      }
    }).then(() => {
    console.log("Trainsaction finished");
  });
});


/************************************************/
/*************** COMMENT Count  *****************/
/***********************************************/

exports.commentCount = functions.database.ref('/comments/{livetickerID}/{pushID}').onWrite(event => {
  //Increment subscription counter
  admin.database().ref("liveticker/" + event.params.livetickerID + "/commentCount").transaction((current) => {
    if (event.data.exists() && !event.data.previous.exists()) {
      return (current || 0) + 1;
    } else if (!event.data.exists() && event.data.previous.exists()) {
      return (current || 0) - 1;
    }
  }).then(() => {
    console.log("Trainsaction finished");
  });;
});

/************************************************/
/************** Viewer  Count  ******************/
/***********************************************/

exports.viewerCount = functions.database.ref('/viewer/{livetickerID}/{pushID}').onWrite(event => {
  //Increment subscription counter
  admin.database().ref("viewerCount/" + event.params.livetickerID).transaction((current) => {
    if (event.data.exists() && !event.data.previous.exists()) {
      return (current || 0) + 1;
    } else if (!event.data.exists() && event.data.previous.exists()) {
      return (current || 0) - 1;
    }
  }).then(() => {
    console.log("Trainsaction finished");
  });;
});

/************************************************/
/********** Sanitize Viewer  Count  *************/
/***********************************************/

exports.sanitizeViewerCount = functions.https.onRequest((req, res) => {
  admin.database().ref('viewer').once('value').then((snapshot) => {
    snapshot.forEach((childSnapshot) => {
      childSnapshot.forEach((viewerEntry) => {
        if (viewerEntry.val() < Date.now() - 600000) {
          viewerEntry.ref.remove();
        }
      });
    })
  });
  f

  res.end();
});

/************************************************/
/**************** ADD COMMENT  ******************/
/***********************************************/

exports.addComment = functions.database.ref('/request/{userID}/addComment/{pushID}').onWrite(event => {
  if (event.data.exists()) {
    if (event.data.val().livetickerID) {

      return admin.database().ref("/lastComment/" + event.params.userID).once("value").then(timestamp => {
        if (timestamp.val() && Date.now() - timestamp.val() < 30000) {
          return admin.database().ref("/result/" + event.params.userID + "/addComment/" + event.params
            .pushID).set({
            state: "error",
            errorDetails: "spam",
            timestamp: Date.now()
          });
        } else {
          return admin.database().ref("/lastComment/" + event.params.userID).set(Date.now()).then(() => {
            var tempComment = new comment(event.data.val());
            var validate = tempComment.validate();
            if (validate !== true) {
              return admin.database().ref("/result/" + event.params.userID + "/addComment/" + event.params
                .pushID).set({
                state: "error",
                errorDetails: validate,
                timestamp: Date.now()
              });
            }

            tempComment.setAuthorID(event.params.userID);
            tempComment.sanitize();

            return admin.database().ref("/comments/" + event.data.val().livetickerID).push(tempComment.getData())
              .then(
                (snapshot) => {
                  return admin.database().ref("/result/" + event.params.userID + "/addComment/" + event.params
                    .pushID).set({
                    state: "success",
                    timestamp: Date.now()
                  });
                }).catch((e) => {
                return admin.database().ref("/result/" + event.params.userID + "/addComment/" + event.params
                  .pushID).set({
                  state: "error",
                  errorDetails: e,
                  timestamp: Date.now()
                });
              });
          });
        }
      });
    }
  }
});


/************************************************/
/************* REQUEST Anon Feed  ****************/
/***********************************************/

exports.anonFeed = functions.database.ref('/request/{userID}/anonFeed/{pushID}').onWrite(event => {
  if (event.data.exists()) {
    return admin.database().ref("liveticker/").orderByChild("state").equalTo(event.data.val().extra).limitToLast(10)
      .once(
        "value").then((snapshot) => {

        var authors = [];
        snapshot.forEach((childSnapshot) => {
          authors[childSnapshot.val().authorID] = true;
        });

        var promises = [];
        for (var key in authors) {
          promises.push(new createUserDetailsPromise(key));
        }

        return Promise.all(promises).then((result) => {

          var authorData = [];
          result.forEach((value) => {
            authorData[value.authorID] = value;
          });

          var livetickers = snapshot.val();
          for (var key in livetickers) {
            livetickers[key].userName = authorData[livetickers[key].authorID].userName;
            livetickers[key].profilePicture = authorData[livetickers[key].authorID].profilePicture;
          }

          return admin.database().ref("/result/" + event.params.userID + "/anonFeed/" + event.params
            .pushID).set({
            state: "success",
            livetickers: livetickers,
            timestamp: Date.now()
          });
        }).catch((e) => {

        });
      });
  }
});


/************************************************/
/************* REQUEST COMMENTS  ****************/
/***********************************************/

exports.requestComments = functions.database.ref('/request/{userID}/comments/{pushID}').onWrite(event => {
  if (event.data.exists()) {
    if (event.data.val().livetickerID) {
      return admin.database().ref('/comments/' + event.data.val().livetickerID).orderByChild("timestamp").once(
        'value').then((snapshot) => {

        var comments = snapshot.val();

        console.log(comments);

        var authors = [];
        snapshot.forEach((childSnapshot) => {
          authors[childSnapshot.val().authorID] = true;
        });

        console.log(authors);

        var promises = [];
        for (var key in authors) {
          promises.push(new createUserDetailsPromise(key));
        };

        return Promise.all(promises).then((result) => {

          console.log(result);

          var authorData = [];
          result.forEach((value) => {
            authorData[value.authorID] = value;
          });

          console.log(authorData);

          for (var key in comments) {
            comments[key].userName = authorData[comments[key].authorID].userName;
            comments[key].profilePicture = authorData[comments[key].authorID].profilePicture;
          }

          console.log(comments);

          return admin.database().ref("/result/" + event.params.userID + "/comments/" + event.params
            .pushID).set({
            state: "success",
            comments: comments,
            timestamp: Date.now(),
            successDetails: snapshot.key
          });
        }).catch((e) => {
          return admin.database().ref("/result/" + event.params.userID + "/comments/" + event.params
            .pushID).set({
            state: "error",
            errorDetails: e,
            timestamp: Date.now()
          });
        });
      }).catch((e) => {
        return admin.database().ref("/result/" + event.params.userID + "/comments/" + event.params
          .pushID).set({
          state: "error",
          errorDetails: e,
          timestamp: Date.now()
        });
      });
    }
  }
});

/************************************************/
/****************** NEW USER  ******************/
/***********************************************/

exports.createNewUser = functions.auth.user().onCreate(event => {
  var userData = {};

  //TODO Check if user is anonymous

  userData.profilePicture =
    "https://firebasestorage.googleapis.com/v0/b/pro4-84e1f.appspot.com/o/profilePictures%2Fprofile_placeholder.png?alt=media&token=859811c6-f183-4cba-8ce7-6f23186b6303";

  userData.titlePicture =
    "https://firebasestorage.googleapis.com/v0/b/pro4-84e1f.appspot.com/o/titlePictures%2Ftitle_placeholder.jpg?alt=media&token=5766f9b6-cc4e-494a-83cc-e1cb882eeaf7";

  /*
  userData.userName = generateUsername({
    words: 3,
    number: true
  }).dashed;
  */

  userData.userName = "Anonymous";

  return admin.database().ref("users/" + event.data.uid).set(userData);
});

/************************************************/
/**************** REQUEST FEED  ****************/
/***********************************************/

exports.requestFeed = functions.database.ref('/request/{userID}/feed/{pushID}').onWrite(event => {

  //Check if entry is a valid request and not a deletion event
  if (event.data.exists() && !event.data.previous.exists()) {
    //Check if entry is a valid request and not a deletion event
    //Create Promise chain for all types of Livetickers that we want to obtain
    return Promise.all([requestRecentlyVisitedFeeds(event.params.userID),
        requestSubscriptionFeeds(event.params.userID)
      ])
      .then(function(allData) {

        //All Promises Resolved, none were rejected
        //Iterate over livetickers and get all author IDs
        var subscriptionLivetickers = allData.pop();
        var recentLivetickers = allData.pop();

        var authors = [];
        if (recentLivetickers) {
          recentLivetickers.forEach((ticker) => {
            if (ticker && ticker.authorID) {
              authors[ticker.authorID] = true;
            }
          });
        }

        if (subscriptionLivetickers) {
          subscriptionLivetickers.forEach((authorLivetickers) => {
            if (authorLivetickers) {
              authorLivetickers.forEach((ticker) => {
                if (ticker && ticker.authorID) {
                  authors[ticker.authorID] = true;
                }
              });
            }
          });
        }

        let promises = [];
        for (let key in authors) {
          promises.push(createUserDetailsPromise(key));
        }

        //Get all users from the IDs and add the details to the livetickers
        return Promise.all(promises).then(function(authorResult) {
          var authorArray = [];
          for (var i = 0; i < authorResult.length; i++) {
            authorArray[authorResult[i].authorID] = authorResult[i];
          }

          var result = {
            state: "success",
            timestamp: Date.now()
          };

          result.recentLivetickers = [];
          result.subscriptionLivetickers = [];

          if (recentLivetickers) {
            recentLivetickers.forEach((ticker) => {
              if (ticker) {
                if (authorArray[ticker.authorID].userName) {
                  ticker.userName = authorArray[ticker.authorID].userName;
                }
                if (authorArray[ticker.authorID].profilePicture) {
                  ticker.profilePicture = authorArray[ticker.authorID].profilePicture;
                }

                result.recentLivetickers.push(ticker);
              }
            });
          }

          if (subscriptionLivetickers) {
            subscriptionLivetickers.forEach((authorLivetickers) => {
              if (authorLivetickers) {
                authorLivetickers.forEach((ticker) => {
                  if (ticker) {
                    if (authorArray[ticker.authorID].userName) {
                      ticker.userName = authorArray[ticker.authorID].userName;
                    }
                    if (authorArray[ticker.authorID].profilePicture) {
                      ticker.profilePicture = authorArray[ticker.authorID].profilePicture;
                    }

                    result.subscriptionLivetickers.push(ticker);
                  }
                });
              }
            });
          }

          return admin.database().ref("/result/" + event.params.userID + "/feed/" + event.params.pushID).set(
            result);
        }).catch((e) => {
          //One or more Promises were rejected. Store error to Database
          return admin.database().ref("/result/" + event.params.userID + "/feed/" + event.params.pushID).set({
            state: "error",
            timestamp: Date.now(),
            errorDetails: "Fetching User Details failed!",
            blub: e
          });
        });
      }).catch((e) => {
        //One or more Promises were rejected. Store error to Database
        return admin.database().ref("/result/" + event.params.userID + "/feed/" + event.params.pushID).set({
          state: "error",
          timestamp: Date.now(),
          errorDetails: "Fetching Livetickers failed!"
        });
      });
  }
});

//Return Promise of all recently visited livetickers of given user
var requestRecentlyVisitedFeeds = function(userID) {
  return new Promise(function(resolve, reject) {
    return admin.database().ref("recentlyVisited/" + userID).limitToLast(3).once(
      'value').then(
      (snapshot) => {
        if (snapshot.hasChildren()) {
          let promises = [];
          snapshot.forEach((childSnapshot) => {
            if (childSnapshot.key) {
              promises.push(createGetLivetickerPromise(childSnapshot.key, true));
            }
          });
          Promise.all(promises).then((results) => {
            resolve(results);
          }).catch((e) => {
            reject(e);
          });
        } else {
          resolve();
        }
      }).catch((e) => {
      reject(e);
    });
  });
}

//Return Promise of all subscribed livetickers of given user
var requestSubscriptionFeeds = function(userID) {
  return new Promise(function(resolve, reject) {
    return admin.database().ref("subscribedTo/" + userID).once('value').then(
      (snapshot) => {
        if (snapshot.hasChildren()) {
          let promises = [];
          snapshot.forEach((childSnapshot) => {
            promises.push(createGetLivetickersFromUserPromise(childSnapshot.key, false));
          });
          Promise.all(promises).then((results) => {
            resolve(results);
          }).catch((e) => {
            reject(e);
          });
        } else {
          resolve();
        }
      }).catch((e) => {
      reject(e);
    });
  });
}

/************************************************/
/************** Sanitize Functions **************/
/***********************************************/

exports.sanitizeRequests = functions.https.onRequest((req, res) => {
  admin.database().ref('request').remove().then((snapshot) => {
    res.end();
  });
});

exports.sanitizeResults = functions.https.onRequest((req, res) => {
  admin.database().ref('result').once('value').then((snapshot) => {
    snapshot.forEach((childSnapshot) => {
      childSnapshot.forEach((childSnapshot2) => {
        childSnapshot2.forEach((childSnapshot3) => {
          if (childSnapshot3.val().timestamp < Date.now() - 600000) {
            childSnapshot3.ref.remove();
          }
        });
      });
    });
    res.end();
  });
});

/************************************************/
/*************** Helper functions ***************/
/***********************************************/

//Creates a Promise which returns a Liveticker from a given key
var createGetLivetickerPromise = function(key, privateAllowed) {
  return new Promise((resolve, reject) => {
    return admin.database().ref("liveticker/" + key).once("value").then((snapshot) => {
      if (snapshot.val()) {
        if (!privateAllowed && snapshot.val().privacy !== "public") {
          resolve();
        }
        let ticker = snapshot.val();
        ticker.livetickerID = key;
        resolve(ticker);
      } else {
        resolve();
      }
    }).catch((e) => {
      //Something went wrong while getting recently visited Liveticker
      console.log("Error getting Liveticker with key " + key);
      reject(e);
    });
  });
}

//Creates promise that gets user details of given userID
var createUserDetailsPromise = function(userID) {
  return new Promise(function(resolve, reject) {
    return admin.database().ref("users/" + userID).once("value").then((snapshot) => {
      var result = snapshot.val();
      result.authorID = snapshot.key;
      resolve(result);
    }).catch((e) => {
      reject(e);
    });
  });
}

//Creates a Promise which returns "live" or "starting" Livetickers from given user
var createGetLivetickersFromUserPromise = function(key, privateAllowed) {
  return new Promise((resolve, reject) => {
    return admin.database().ref("liveticker/").orderByChild("idState").startAt(key + "a").endAt(key + "ab").once(
        "value")
      .then((snapshot) => {
        if (snapshot.hasChildren()) {
          let livetickers = [];
          snapshot.forEach((childSnapshot) => {
            if (childSnapshot.val()) {
              if (!privateAllowed && childSnapshot.val().privacy !== "public") {

              } else {
                let ticker = childSnapshot.val();
                ticker.livetickerID = childSnapshot.key;
                livetickers.push(ticker);
              }
            }
          });
          if (livetickers && livetickers[0]) {
            resolve(livetickers);
          }
        }
        resolve();
      }).catch((e) => {
        console.log("Error geting livetickers from user with key " + key);
        reject(e);
      });
  });
}

var createCheckAuthorPromise = function(authorID) {
  return new Promise((resolve, reject) => {
    admin.auth().getUser(authorID).then((userRecord) => {
      //User exists. Return userRecord
      resolve(userRecord);
    }).catch((e) => {
      //Authentication failed!
      reject(e);
    });
  });
}

var createGetTokensPromise = function(userID) {
  return new Promise((resolve, reject) => {
    admin.database().ref("registrationTokens/" + userID).once("value").then((snapshot) => {
      resolve(snapshot);
    }).catch((e) => {
      reject(e);
    });
  });
}
