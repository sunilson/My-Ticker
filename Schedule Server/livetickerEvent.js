function LivetickerEvent(data) {
  this.data = data;
  this.eventEntry = {};
}

LivetickerEvent.prototype.getEventEntry = function() {
  return this.eventEntry;
}

module.exports = LivetickerEvent;
