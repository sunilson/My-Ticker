var utilities = require(__dirname + '/utilities.js');

function Liveticker(data) {
  this.data = data;
  this.data.commentCount = 0;
  this.data.likeCount = 0;
}

Liveticker.prototype.setUsername = function(username) {
  this.data.userName = username;
}

Liveticker.prototype.setProfilePicture = function(url) {
  this.data.profilePicture = url;
}

Liveticker.prototype.setAuthorID = function(id) {
  this.data.authorID = id;
}

Liveticker.prototype.getData = function() {
  return {
    "title": this.data.title,
    "description": this.data.description,
    "authorID": this.data.authorID,
    "privacy": this.data.privacy,
    "state": this.data.state,
    "idState": this.data.idState,
    "status": this.data.status,
    "commentCount": this.data.commentCount,
    "likeCount": this.data.likeCount,
    "stateTimestamp": this.data.stateTimestamp
  }
}

Liveticker.prototype.validate = function() {

  this.data.title = utilities.sanitizeString(this.data.title);
  this.data.description = utilities.sanitizeString(this.data.description);
  this.data.authorID = utilities.sanitizeStringStrict(this.data.authorID);
  this.data.privacy = utilities.sanitizeStringStrict(this.data.privacy);
  this.data.stateTimestamp = utilities.sanitizeNumber(this.data.stateTimestamp);
  this.data.status = utilities.sanitizeString(this.data.status);
  this.data.commentCount = utilities.sanitizeNumber(this.data.commentCount);
  this.data.likeCount = utilities.sanitizeNumber(this.data.likeCount);

  if (this.data.stateTimestamp <= Date.now()) {
    console.log('warn', 'Start Date was too small!');
    this.data.stateTimestamp = Date.now();
    this.data.state = "started";
    this.data.idState = this.data.authorID + "ab";
  } else {
    if (this.data.stateTimestamp > Date.now() + 432000000) {
      console.log('warn', 'Start Date was too high!');
      this.data.stateTimestamp = Date.now() + 432000000;
    }
    this.data.state = "not started";
    this.data.idState = this.data.authorID + "a";
  }

  //Check if required data is correct
  if (!utilities.checkValidString(this.data.title, false, 5, 30)) {
    console.log('error', "Rejected new Liveticker. Reason: Title Invalid");
    return "Title invalid!";
  } else if (!utilities.checkValidString(this.data.description, true, 0, 9999)) {
    console.log('error', "Rejected new Liveticker. Reason: Description Invalid");
    return "Description invalid!";
  } else if (!utilities.checkValidString(this.data.status, true, 0, 140)) {
    console.log('error', "Rejected new Liveticker. Reason: Status Invalid");
    return "Status invalid!";
  } else if (this.data.privacy !== "public" && this.data.privacy !== "private") {
    console.log('error',
      "Rejected new Liveticker. Reason: Privacy not set");
    return "Privacy must be set!";
  } else if (!utilities.checkValidNumber(this.data.stateTimestamp)) {
    console.log('error', "Rejected new Liveticker. Reason: Start Date invalid");
    return "Start Date invalid!";
  } else if (!utilities.checkValidNumber(this.data.commentCount)) {
    console.log('error', "Rejected new Liveticker. Reason: Comment count invalid");
    return "Comment count invalid!";
  } else if (!utilities.checkValidNumber(this.data.likeCount)) {
    console.log('error', "Rejected new Liveticker. Reason: Like count invalid");
    return "Like count invalid!";
  } else {
    return true;
  }
}

module.exports = Liveticker;
