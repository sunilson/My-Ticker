var utilities = require(__dirname + '/utilities.js');

function Comment(data) {
  this.data = data;
  if (this.data.livetickerID) {
    delete this.data.livetickerID;
  }
}

Comment.prototype.setUsername = function(username) {
  this.data.userName = username;
}

Comment.prototype.setProfilePicture = function(url) {
  this.data.profilePicture = url;
}

Comment.prototype.setAuthorID = function(id) {
  this.data.authorID = id;
}

Comment.prototype.getData = function() {
  return this.data;
}

Comment.prototype.sanitize = function() {
  //Sanitize Strings
  this.data.content = utilities.sanitizeString(this.data.content);
  this.data.authorID = utilities.sanitizeStringStrict(this.data.authorID);
  this.data.timestamp = Date.now();
}

Comment.prototype.validate = function() {
  //Check if required data is correct
  if (!utilities.checkValidString(this.data.content, false, 1, 120)) {
    console.log('error', "Rejected new Comment. Reason: Content Invalid");
    return "Content invalid!";
  } else {
    return true;
  }
}


module.exports = Comment;
