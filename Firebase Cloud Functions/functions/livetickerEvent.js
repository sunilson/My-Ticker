var utilities = require(__dirname + '/utilities.js');

function LivetickerEvent(data) {
  this.data = data;
}

LivetickerEvent.prototype.getEventData = function() {
  return this.data;
}

LivetickerEvent.prototype.getEventDataForStorage = function() {
  if (this.data.type === "image") {
    return {
      type: this.data.type,
      content: this.data.content,
      thumbnail: this.data.thumbnail,
      timestamp: Date.now(),
      caption: this.data.caption
    }
  } else {
    return {
      type: this.data.type,
      content: this.data.content,
      timestamp: Date.now()
    }
  }
}

LivetickerEvent.prototype.sanitize = function() {
  this.data.type = utilities.sanitizeStringStrict(this.data.type);
  if (this.data.type === "text") {
    this.data.content = utilities.sanitizeString(this.data.content);
  }
  if (this.data.caption != null) {
    this.data.caption = utilities.sanitizeString(this.data.caption);
  }
  this.data.authorID = utilities.sanitizeStringStrict(this.data.authorID);
  this.data.livetickerID = utilities.sanitizeStringStrict(this.data.livetickerID);
}

LivetickerEvent.prototype.validate = function() {
  switch (this.data.type) {
    case "text":
      if (!utilities.checkValidString(this.data.content, false, 1, 250)) {
        return "Text invald or too short/long! (Max. 250 words)"
      }
      break;
    case "image":
      if (!utilities.validateImageURL(this.data.content) || !utilities.validateImageURL(this.data.thumbnail)) {
        return "Invalid Image URL!";
      }
      break;
    default:
      return "Invalid Event type!";
      break;
  }

  return true;
}

module.exports = LivetickerEvent;
