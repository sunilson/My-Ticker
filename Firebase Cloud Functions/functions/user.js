var utilities = require(__dirname + '/utilities.js');

function User(data) {
  this.data = data;
}

User.prototype.getData = function() {
  this.data.userID = null;
  return this.data;
}

User.prototype.sanitize = function() {
  //Sanitize Strings
  this.data.status = utilities.sanitizeString(this.data.status);
  this.data.info = utilities.sanitizeString(this.data.info);
  this.data.userName = utilities.sanitizeStringStrict(this.data.userName);
}

User.prototype.validate = function() {
  //Check if required data is correct
  if (!utilities.checkValidString(this.data.status, true, 0, 120)) {
    console.log('error', "Rejected new Liveticker. Reason: Status Invalid");
    return "Status invalid!";
  } else if (!utilities.checkValidString(this.data.info, true, 0, 999)) {
    console.log('error', "Rejected new Liveticker. Reason: Info Invalid");
    return "Info invalid!";
  } else if (!utilities.checkValidString(this.data.userName, false, 5, 30) || this.data.userName === "Anonymous") {
    console.log('error', "Rejected new Liveticker. Reason: Username Invalid");
    return "Username invalid!";
  } else if (!utilities.validateImageURL(this.data.profilePicture)) {
    console.log('error', "Rejected new Liveticker. Reason: Profile Picture invalid");
    return "Profile Picture invalid!";
  } else if (!utilities.validateImageURL(this.data.titlePicture)) {
    console.log('error', "Rejected new Liveticker. Reason: Title Picture invalid");
    return "Title Picture invalid!";
  } else {
    return true;
  }
}

module.exports = User;
