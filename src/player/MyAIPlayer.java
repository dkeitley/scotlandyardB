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
* 1) Currently manually entered first Mr X location as location not know until notify
* 2) Game time outs after 'Interrupted Exception'. 
* 3) Need to tidy up, add comments etc. 
*/

class MyAIPlayer implements Player
{

	private Graph<Integer, Route> graph;
	private ScotlandYardView initialView;
	private AIModel currentGame;
	private String graphFilename;
	private Set<Colour> colours;

    public MyAIPlayer(ScotlandYardView view, String graphFilename,Set<Colour> colours)
    {
    	System.out.println("MyAIPlayer Constructor");
    	initialView = view;
	ScotlandYardGraphReader reader = new ScotlandYardGraphReader();
	try { graph = reader.readGraph(graphFilename); } 
	catch(Exception e) { System.err.println(e.getMessage()); }
	this.graphFilename = graphFilename;
	this.colours = colours;
	currentGame = new AIModel(view,graphFilename,new ArrayList<Move>(),colours);
	
    }

    public Move notify(int location, Set<Move> moves) 
    {    
	Set<AIModel> leafGames = new HashSet<AIModel>();
	Set<AIModel> nextGames = new HashSet<AIModel>();
	List<Move> movesPlayed = new ArrayList<Move>(currentGame.getMovesPlayed());
	int currentDepth = 0;
	int depth = 1;
	
	for(Move move: moves) {
		List<Move> possibleMovesPlayed = new ArrayList<Move>(movesPlayed);
		possibleMovesPlayed.add(move);
		leafGames.add(new AIModel(initialView,graphFilename,possibleMovesPlayed,colours));
	}
	currentDepth++;

	while(currentDepth!=depth) {
		for(AIModel game : leafGames) {
			//here do we need to assume detectives play best possible move for part5?
			for(Move move : game.validMoves(game.getCurrentPlayer())) {
				List<Move> movesPlayedByGame = game.getMovesPlayed();
				movesPlayedByGame.add(move);
				AIModel newModel = new AIModel(initialView,graphFilename,
				movesPlayedByGame,colours);
				nextGames.add(newModel);
			}
		}
		leafGames = nextGames;
		nextGames.clear();
		currentDepth++;
	}
	Set<AIModel> games = leafGames;
    	Move bestMove = null;
    	double highestScore = Double.NEGATIVE_INFINITY;
    	
    	for(AIModel game : games) {    		
		double score = score(game,game.getMrXLocation());
		System.out.println("Score: " + score);
		if(score > highestScore) {
			highestScore = score;
			int size = game.getMovesPlayed().size();
			bestMove = game.getMovesPlayed().get(size-1);
		}
    	}
    	return bestMove;
    }


    //A rough board scoring function - weights of relatve components still
    // to be sorted out NOTE: @param mrXMoves MUST only contains moveTickets - it should not contains
    //MovePass as then the game would be over and and double moves mrX plays should be inserted
    //as 2 separate moveTickets
    public double score(AIModel model, int mrXLocation /*List<MoveTicket> mrXMoves*/)
    {

    	List<Integer> movesAwayFromDetectives = movesAwayFromDetectives(mrXLocation, model);
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
    	Set<Integer> visited = new HashSet<Integer>();
    	Set<Integer> detectiveLocations = detectiveLocations(model);
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
    			Set<Edge<Integer, Route>> edges = graph.getEdgesFrom(location);
    			for(Edge<Integer, Route> edge : edges)
    			{
    				if(visited.contains(edge.target()) || edge.data().equals(Route.Boat)) continue;
    				tempLocations.add(edge.target());
    			}
    		}
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
