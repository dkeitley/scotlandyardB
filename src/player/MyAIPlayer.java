package player;

import java.util.*;
import java.lang.Math;
import scotlandyard.*;
import solution.*;

/* An AI to play as MrX X in a game of ScotlandYardModel currently 
 * the AI makes random moves. A function that scores the board is
 * implemented but not currently used. DAN - talk to me about the MrXMoves 
 * param of score - its implementaton is important!!!!! 
 */



/*Current Issues
* 1) Null pointer in nextPlayer(), line 145
*/

class MyAIPlayer implements Player
{

	class AIModel extends ScotlandYardModel implements Spectator {
	
		List<Move> movesFromCurrent; 
		int mrXLocation;

		public AIModel(int numDetectives, List<Boolean> showRounds, 
		String graphFilename, int mrXLocation, List<Move> movesFromCurrent) {
			super(numDetectives, showRounds, graphFilename);
			this.mrXLocation = mrXLocation;
			this.movesFromCurrent = movesFromCurrent; 
		}

		public void notify(Move move) {
			
			if(move instanceof MoveTicket) {
				MoveTicket moveTicket = (MoveTicket)move;
				this.play(moveTicket);
				if(move.colour.equals(Colour.Black)) {
					mrXLocation = moveTicket.target;
				}
			} else if (move instanceof MoveDouble) {
				MoveDouble moveDouble = (MoveDouble)move;
				this.play(moveDouble);
				if(move.colour.equals(Colour.Black)) {
					mrXLocation = moveDouble.move2.target;
				}
			} else {
				this.play((MovePass)move);
			}
			this.nextPlayer();
		}

		public void addMove(Move move) {
			movesFromCurrent.add(move);
		}
	}

	private Graph<Integer, Route> graph;
	private ScotlandYardView initialView;
	private AIModel currentGame;
	private List<Move> movesPlayed;
	private String graphFilename;

    public MyAIPlayer(ScotlandYardView view, String graphFilename)
    {
    	System.out.println("MyAIPlayer Constructor");
    	initialView = view;
	ScotlandYardGraphReader reader = new ScotlandYardGraphReader();
	try { graph = reader.readGraph(graphFilename); } 
	catch(Exception e) { System.err.println(e.getMessage()); }
	movesPlayed = new ArrayList<Move>();
	this.graphFilename = graphFilename;
	
	currentGame = createInitialModel(view,graphFilename);
    }

    private AIModel createInitialModel(ScotlandYardView model, String graphFilename) {
	List<Colour> colours = model.getPlayers();
   	int numDetectives = colours.size();
   	AIModel newGame = new AIModel(numDetectives,model.getRounds(),graphFilename,0,
   	new ArrayList<Move>());
   	colours.add(Colour.Black);
	Set<Ticket> ticketTypes = new HashSet<Ticket>();
	ticketTypes.add(Ticket.Taxi);	
	ticketTypes.add(Ticket.Bus);	
	ticketTypes.add(Ticket.Underground);
		
	for(Colour colour : colours) {
		Map<Ticket,Integer> tickets = new HashMap<Ticket,Integer>();
		for(Ticket ticket : ticketTypes) {
			tickets.put(ticket,model.getPlayerTickets(colour,ticket));
		}
		if(colour.equals(Colour.Black)) {
			tickets.put(Ticket.Secret,model.getPlayerTickets(colour,Ticket.Secret));
			tickets.put(Ticket.Double,model.getPlayerTickets(colour,Ticket.Double));
		}
		newGame.join(null,colour,model.getPlayerLocation(colour),tickets);
	}
	return newGame;
    }

    public Move notify(int location, Set<Move> moves) 
    {
    	Set<AIModel> games = createGames(currentGame,moves,1);
    	Move bestMove = null;
    	double highestScore = Double.NEGATIVE_INFINITY;
    	for(AIModel game : games) {
		double score = score(game,game.mrXLocation);
		if(score > highestScore) {
			highestScore = score;
			int size = game.movesFromCurrent.size();
			bestMove = game.movesFromCurrent.get(size-1);
		}
    	}
    	return bestMove;
    }

    private AIModel createAIModel(AIModel previousState, Move move) {
	AIModel newModel = createInitialModel(initialView,graphFilename);
	newModel.addMove(move);
	List<Move> newMoves = previousState.movesFromCurrent;
	if(move instanceof MoveTicket) {
		newMoves.add((MoveTicket) move);
	} else if (move instanceof MoveDouble) {
		MoveDouble doubleMove = (MoveDouble) move;
		newMoves.add(doubleMove.move1);
		newMoves.add(doubleMove.move2);
	} 
	return syncModel(newModel,newMoves);
    }

    private AIModel syncModel(AIModel model, List<Move> newMoves) {
	for(Move move : movesPlayed) {
		if(move instanceof MoveTicket) {
			MoveTicket moveTicket = (MoveTicket) move;
			model.play(moveTicket);
		} else if (move instanceof MoveDouble) {
			MoveDouble doubleMove = (MoveDouble) move;
			model.play(doubleMove);
		}
		model.nextPlayer();
	}
	for(Move newMove : newMoves) {
		model.addMove(newMove);
		if(newMove instanceof MoveTicket) {
			MoveTicket moveTicket = (MoveTicket) newMove;
			model.play(moveTicket);
		} else if (newMove instanceof MoveDouble) {
			MoveDouble doubleMove = (MoveDouble) newMove;
			model.play(doubleMove);
		}
		model.nextPlayer();
	}
	return model;
    } 

    private Set<AIModel> createGames(AIModel model, Set<Move> moves, int depth) {	
	Set<AIModel> leafGames = new HashSet<AIModel>();
	Set<AIModel> nextGames = new HashSet<AIModel>();
	int currentDepth = 0;
	for(Move move: moves) {
		leafGames.add(createAIModel(model,move));
	}
	currentDepth++;

	while(currentDepth!=depth) {
		for(AIModel game : leafGames) {
			for(Move move : game.validMoves(game.getCurrentPlayer())) {
				nextGames.add(createAIModel(game,move));
			}
		}
		leafGames = nextGames;
		nextGames.clear();
		currentDepth++;
	}
	return leafGames;
    }  

    //A rough board scoring function - weights of relatve components still
    // to be sorted out NOTE: @param mrXMoves MUST only contains moveTickets - it should not contains
    //MovePass as then the game would be over and and double moves mrX plays should be inserted
    //as 2 separate moveTickets
    public double score(AIModel model, int mrXLocation /*List<MoveTicket> mrXMoves*/)
    {
    	System.out.println("Entered Score");
    	//System.out.println("Location on line 130: " + mrXLocation);
    	List<Integer> movesAwayFromDetectives = movesAwayFromDetectives(mrXLocation, model);
	//System.out.println("movesAwayFromDetectives calculated: " + movesAwayFromDetectives);
    	int closesedDetective = Collections.min(movesAwayFromDetectives);

    	//Set<Integer> possibleLoctions = possibleLocations(model, mrXMoves);
    	double score = 0;
    	score += orderOfNode(mrXLocation);

    	score += adjacentNodeOrder(mrXLocation);

    	score -= distanceFromCenter(mrXLocation) / 100;

    	score += 2 * average(movesAwayFromDetectives);
    	score += model.getPlayerTickets(Colour.Black, Ticket.Secret);
    	score += model.getPlayerTickets(Colour.Black, Ticket.Double);
    	//score += possibleLoctions.size();
    	//score += howSpreadOut(possibleLoctions) / 1000;
    	if(closesedDetective == 1) score += -100;
    	else if(closesedDetective == 0) score += -1000;
    	else score += 5 * closesedDetective;
    	return score;
    }
    
    // returns the order of @param node
    private int orderOfNode(int node)
    {
    	return graph.getEdgesFrom(node).size();
    }
    
    //returns the average order of the adjacent nodes
    private float adjacentNodeOrder(int node)
    {
    	int total = 0;
    	Set<Edge<Integer, Route>> edges = graph.getEdgesFrom(node);
    	for(Edge<Integer, Route> edge : edges)
    	{
    		total += orderOfNode(edge.target());
    	}
    	return (float) total / orderOfNode(node);
    }
    
    // returns list of integers representing number of moves away
    // each detective is not considering tickets (this took too long to run) except 
    // disregarding boat edges
    private List<Integer> movesAwayFromDetectives(int mrXLocation, ScotlandYardModel model)
    {
    	//System.out.println("MrXLocation: " + mrXLocation);
    	Set<Integer> visited = new HashSet<Integer>();
    	//System.out.println("movessAway function entered");
    	Set<Integer> detectiveLocations = detectiveLocations(model);
    	System.out.println("detective locations calculated: " + detectiveLocations);
    	Set<Integer> locations = new HashSet<Integer>();
    	List<Integer> detectiveMovesAway = new ArrayList<Integer>();
    	locations.add(mrXLocation);
    	for(int i = 0; detectiveMovesAway.size() < detectiveLocations.size(); i++)
    	{
    		Set<Integer> tempLocations = new HashSet<Integer>();
    		visited.addAll(locations);
    		for(int location : locations)
    		{
    			if(detectiveLocations.contains(location)) detectiveMovesAway.add(i);
    			//System.out.println("locations loop entered");
    			//System.out.println("Location: " + location);
    			Set<Edge<Integer, Route>> edges = graph.getEdgesFrom(location);
    			//System.out.println("edges set created: " + edges);
    			for(Edge<Integer, Route> edge : edges)
    			{
    			//	System.out.println("edge loop entered");
    				if(visited.contains(edge.target()) || edge.data().equals(Route.Boat)) continue;
    				tempLocations.add(edge.target());
    				//System.out.println("added to tempLocations");	
    			}
    			//System.out.println("End of location loop");	
    		}
    		//System.out.println("Line 160");
    		locations = tempLocations;
    	}
    	System.out.println(detectiveMovesAway);
    	return detectiveMovesAway;
    }
    
  //returns the locations of all the detectives
    private Set<Integer> detectiveLocations(ScotlandYardModel model)
    {
    	List<Colour> players = model.getPlayers();
    	Set<Integer> detectiveLocations = new HashSet<Integer>();
    	for(Colour player : players)
    	{
    		if(player.equals(Colour.Black)) continue;
    		detectiveLocations.add(model.getPlayerLocation(player));
    	}
    	return detectiveLocations;
    }
    
    //returns the possible locations mrX could be for @param model having played @param
    //mrXMoves NOTE: @param mrXMoves MUST only contains moveTickets - it should not contains
    //MovePass as then the game would be over and and double moves mrX plays should be inserted
    //as 2 separate moveTickets
    public Set<Integer> possibleLocations(ScotlandYardModel model, List<MoveTicket> mrXMoves)
    {
    	Set<Integer> possibleLocations = new HashSet<Integer>();
    	possibleLocations.add(model.getPlayerLocation(Colour.Black));
    	int currentRound = model.getRound();
    	int lastShowRound = getLastShowRond(model);
    	for(int i = lastShowRound + 1; i <= currentRound; i++)
    	{
    		MoveTicket moveTicket = mrXMoves.get(i - 1);
    		possibleLocations = possibleLocations2(moveTicket, possibleLocations);	
    	}
    	return possibleLocations;
    }
    
    //returns the possible locations mrX could be if he was known to be at one of the 
    //@param nodes and used a @param moveTicket  
    private Set<Integer> possibleLocations2(MoveTicket moveTicket, Set<Integer> nodes)
    {
    	Set<Integer> newPossibleNodes = new HashSet<Integer>();
    	Ticket ticketUsed = moveTicket.ticket;
    	for(int node : nodes)
    	{
    		for(Edge<Integer, Route> edge : graph.getEdgesFrom(node))
    		{
    			Ticket ticketReqired = Ticket.fromRoute(edge.data());
    			if(ticketUsed.equals(ticketReqired) || ticketUsed.equals(Ticket.Secret)) newPossibleNodes.add(edge.target());
    		}
    	}
    	return newPossibleNodes;
    }
    
    //returns the last round that has been played that was a show round
    private int getLastShowRond(ScotlandYardModel model)
    {
    	int currentRound = model.getRound();
    	List<Boolean> rounds = model.getRounds();
    	int lastShowRound = currentRound;
    	while(true)
    	{
    		if(rounds.get(lastShowRound) || lastShowRound == 0) break;
    		else lastShowRound --;
    	}
    	return lastShowRound;
    }
    
    //returns the approximate area ocupied by the @param nodes
    public int howSpreadOut(Set<Integer> nodes)
    {
    	PositionsReader reader = new PositionsReader("resources/pos.txt");
		try { reader.read(); }
		catch(Exception e) { System.err.println(e); }
		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
		for(int node : nodes)
		{
			PositionsReader.Point point = reader.getPositionNode(node);
			if(point.x > maxX) maxX = point.x;
			if(point.x < minX) minX = point.x;
			if(point.y > maxY) maxY = point.y;
			if(point.y < minY) minY = point.y;
		}
		return (maxX - minX) * (maxY - minY);
    }
    
    //takes average of list of integers
    private double average(List<Integer> list)
    {
    	int total = 0;
    	for(int num : list)
    	{
    		total += num;
    	}
    	return total / list.size();
    }
    
    // finds the straight line distance a point is from the center 
    // of the board approximating the centre as point 113
    private double distanceFromCenter(int node)
    {
    	PositionsReader reader = new PositionsReader("resources/pos.txt");
		try { reader.read(); }
		catch(Exception e) { System.err.println(e); }
		PositionsReader.Point point = reader.getPositionNode(node);
		int xDistance = point.x - 468;
		int yDistance = point.y - 413;
		return Math.sqrt(xDistance * xDistance + yDistance * yDistance);
    }
}
