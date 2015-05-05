'use strict'
var net = require('net');
var events = require('events');
var util = require('util');


function PlayerServer() {
    var self = this;
    this.server = net.createServer(function(player) {
    	player.on('data',function(data) {
		var connection = JSON.parse(data);
			if(connection.type == 'REGISTER')  {
				self.emit('register',player,connection.student_id);
			} else if(connection.type == 'MOVE') {
				self.emit('move',player,connection);
			}
		});
    });
    
}
util.inherits(PlayerServer, events.EventEmitter);


PlayerServer.prototype.onInput = function(player, data) {
    //TODO
}


PlayerServer.prototype.listen = function(port) {
    this.server.listen(port);
}


PlayerServer.prototype.close = function() {
    this.server.close();
}


module.exports = PlayerServer;
