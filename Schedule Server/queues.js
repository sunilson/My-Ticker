var scheduler = require(__dirname + '/scheduler.js');
var utilities = require(__dirname + '/utilities.js');
var logger = require(__dirname + '/logger.js');
var liveticker = require(__dirname + '/liveticker.js');
var livetickerEntry = require(__dirname + '/livetickerEvent.js')
const Queue = require('firebase-queue');

var exports = module.exports = {};

/*
exports.startLivetickerPushQueue = function(db) {

  logger.log("warn", "Starting LivetickerPush Queue!");

  var options = {
    'specId': 'pushSpec'
  };

  var pushLivetickerQueue = new Queue(db.ref('queue').child("livetickerQueue"), options, function(data,
    progress, resolve, reject) {

    setTimeout(function() {
      resolve(data);
    }, 1000);
  });
}

exports.startAddLivetickerQueue = function(admin) {
  var db = admin.database();
  var addLivetickerRef = db.ref('liveticker');
  var userRef = db.ref('users');

  logger.log("warn", "Starting LivetickerAdd Queue!");

  var options = {
    'specId': 'addLivetickerSpec'
  };

  var addLivetickerQueue = new Queue(db.ref('queue').child("livetickerQueue"), options, function(data,
    progress, resolve, reject) {

    logger.log("info", "Starting to add new Liveticker!");

    var tempLiveticker = new liveticker(data);
    tempLiveticker.sanitize();

    //Check if user with given ID exists and is registered
    admin.auth().getUser(tempLiveticker.getLivetickerEntry().authorID).then((userRecord) => {

      //TODO User auf Korrektheit überprüfen

      var validateResult = tempLiveticker.validate();

      if (validateResult !== true) {
        logger.log("error", validateResult);
        reject(validateResult);
        return;
      }
      //Handle start Date
      tempLiveticker.applyStartDate();

      //Store Liveticker in Database
      addLivetickerRef.push(tempLiveticker.getData()).then((snap) => {
        logger.log("info", "Added new Liveticker: " + snap.key);
        //Start Schedule if liveticker not started yet
        if (tempLiveticker.getData().status !== "started") {
          scheduler.startLivetickerAtDate(tempLiveticker.getData().start, "liveticker/" + snap
            .key,
            snap.key,
            db);
        }
        setTimeout(function() {
          var key = {
            "key": snap.key
          }
          resolve(key);
        }, 1000);
      }, (error) => {
        logger.log('error',
          "Rejected new Liveticker. Reason: Could not write to Database. Error: " + error);
        reject(error);
      });
    }).catch(function(error) {
      reject(error);
    });
  });
}

exports.startPushNotificationQueue = function(db) {}

exports.startAddLivetickerEventQueue = function(admin) {

  var db = admin.database();
  logger.log("warn", "Starting Liveticker Add Event Queue!");

  var options = {
    'specId': 'addEventSpec'
  };

  var addLivetickerEventQueue = new Queue(db.ref('queue').child("addLivetickerEventQueue"), options, function(data,
    progress, resolve, reject) {


  });
}

exports.startPushLivetickerEventQueue = function(db) {

}


exports.startRecentlyVisitedQueue = function(db) {

  logger.log("warn", "Starting Recently Visited Queue!");

  var options = {
    'specId': 'recentlyVisitedSpec'
  };

  var addLivetickerQueue = new Queue(db.ref('queue').child("recentlyVisitedQueue"), options, function(data,
    progress, resolve, reject) {

    //TODO Daten in RecentlyVisited schreiben

  });
}


exports.startLivetickerEditQueue = function(db) {

}


exports.startLivetickerRequestFeedQueue = function(db) {

  logger.log('warn', "Starting Liveticker Request Feed Queue!");

  var options = {
    'specId': 'requestSpec'
  };

  var requestFeedQueue = new Queue(db.ref('queue').child("requestLivetickerQueue"), options, function(data,
    progress, resolve, reject) {

    var result = {};
    var ownLivetickers = {};
    var recentLivetickers = {};

    //Get all own started livetickers
    getOwnLivetickers = new Promise(function(resolve, reject) {
      logger.log('info', "Getting all started Livetickers from user " + data.userID);
      db.ref("liveticker").orderByChild("authorID").equalTo(data.userID).limitToFirst(3).once(
          'value')
        .then(
          (snapshot) => {
            for (var key in snapshot.val()) {
              if (snapshot.val()[key].status === "started") {
                ownLivetickers[key] = snapshot.val()[key];
              }
            }
            result["ownLivetickers"] = ownLivetickers;

            resolve();
          });
    });

    //Get recently visited Livetickers
    getRecentlyVisitedLivetickers = new Promise(function(resolve, reject) {
      db.ref("recentlyVisited/" + data.userID).once('value').then((snapshot) => {
        logger.log('info', "Getting all recently visited Livetickers from user " + data.userID);
        var length = snapshot.numChildren();
        var counter = 0;

        for (var key in snapshot.val()) {
          db.ref("liveticker/public/" + key).once('value').then(function(snapshot) {
            counter++;
            recentLivetickers[snapshot.key] = snapshot.val();
            if (counter == length) {
              result["recentLivetickers"] = recentLivetickers;
              resolve();
            }
          }.bind(key));
        }
      });
    });

    getOwnLivetickers.then(function(result) {
      getRecentlyVisitedLivetickers.then(function(result) {

      }, function(err) {
        //TODO Get Subscription Livetickers
      });
    }, function(err) {

    });

    /*
      //Get all subscription user IDs
      logger.log("info", "Getting all subscriptions from " +
        data.userID);

      db.ref("userSubscribedTo/" + data.userID).once('value').then((snapshot) => {

        //Get all started Livetickers from subscriptions
      });
    });


    setTimeout(function() {
      logger.log("info", "Returning feed result for user " + data.userID);
      resolve(result);
    }, 1000);
  });
}
*/
