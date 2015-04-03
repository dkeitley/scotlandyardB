'use strict'
var PlayerServer = require('./player_server.js');
var GameServer   = require('./game_server.js');

/**
 * Constructor for the class. Needs to initialise the players server
 * and game server, create the list of colours and initialise a game id
 * @constructor
 */
function Server() 
{
    this.game_id = -1;
    this.colours = ['Black', 'Blue', 'Green', 'Red', 'White', 'Yellow'];
	 
    //TODO
    var self = this;
   	self.player_server = new PlayerServer();
   	self.game_server = new GameServer();
}

/**
 * Function that will start up the server. It should set the game_server
 * and the player_server to listen on the correct ports. It should also
 * connect up the events emitted by the player server so that the correct
 * information is passed onto the game server
 * @param player_port
 * @param game_port
 */
Server.prototype.start = function(player_port, game_port) 
{
    //TODO
    var self = this;
    self.player_server.listen(player_port);
    self.game_server.listen(game_port);
    self.game_server.on('initialised', function(data)
    {
    	self.game_id = data;
    });
    self.register();
    self.move();
}


/**
 * Function to close down the player server and the game server
 */
Server.prototype.close = function()
{
    //TODO
    var self = this;
    self.player_server.close();
    self.game_server.close();
}


/**
 * Function to retrieve the id of the game being played
 * @returns {number|*}
 */
Server.prototype.gameId = function() 
{
    //TODO
    var self = this;
    return self.game_id;
}


/**
 * Function to extract the next colour out of the arrays of colours
 * When a colour is extracted, that colour is removed from the list
 * @returns {colour}
 */
Server.prototype.getNextColour = function() 
{
    //TODO
    var colour =  this.colours[0];
    this.colours.shift();
    return colour;
}

Server.prototype.register = function()
{
	var self = this;
	self.player_server.on('register', function(player,student_id)
	{
		var colour = self.getNextColour();
		var game_id = self.gameId();
		self.game_server.addPlayer(player, colour, game_id); 
	});
}

Server.prototype.move = function()
{
	var self = this;
	self.player_server.on('move', function(player,connection)
	{
		self.game_server.makeMove(player, connection);
	});
}

module.exports = Server;




































