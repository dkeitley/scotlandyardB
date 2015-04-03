package player;

import java.util.*;
import java.lang.Math;

import scotlandyard.*;
import solution.*;

class MyAIPlayer implements Player
{
	private Graph<Integer, Route> graph;
	private ScotlandYardModel tempModel; //for testing
	private List<MoveTicket> tempMoves = new ArrayList<MoveTicket>(); //for testing
    
	//Initialises graph 
	//TODO Dan i'm not sure if this constructor is fixed i.e. you could
	// relace the view with a model ???????
    public MyAIPlayer(ScotlandYardView view, String graphFilename)
    {
		ScotlandYardGraphReader reader = new ScotlandYardGraphReader();
		try { graph = reader.readGraph(graphFilename); } 
		catch(Exception e) { System.err.println(e.getMessage()); }
		this.tempModel = (ScotlandYardModel) view;
    }
    
    //TODO this needs to be implemented in the next part
    //currently it contains test code
    public Move notify(int location, Set<Move> moves) 
    {
    	int choice = new Random().nextInt(moves.size());
        for (Move move : moves) 
        {
            if (choice == 0) 
            {
               //test code
            	System.out.println(possibleLocations(tempModel, tempMoves));
                System.out.println("MrX played	" + move.toString());
                
                if (move instanceof MoveDouble) 
        		{
        			MoveDouble moveDouble = (MoveDouble) move;
        			tempMoves.add(moveDouble.move1);
        			tempMoves.add(moveDouble.move2);
        		}
                else tempMoves.add((MoveTicket) move);
                //end of test code
                return move;
            }
            choice--;
        }

        return null;
    }
    
    //A rough board scoring function - weights of relatve components still
    // to be sorted out 
    public double score(ScotlandYardModel model, int mrXLocation)
    {
    	List<Integer> movesAwayFromDetectives = movesAwayFromDetectives(mrXLocation, model);
    	System.out.println("detective diatances -> " + movesAwayFromDetectives);
    	double score = 0;
    	score += orderOfNode(mrXLocation);
    	score += adjacentNodeOrder(mrXLocation);
    	score -= distanceFromCenter(mrXLocation) / 100;
    	int closesedDetective = Collections.min(movesAwayFromDetectives);
    	if(closesedDetective == 1) score += -100;
    	else if(closesedDetective == 0) score += -1000;
    	else score += 5 * closesedDetective;
    	score += 2 * average(movesAwayFromDetectives);
    	score += model.getPlayerTickets(Colour.Black, Ticket.Secret);
    	score += model.getPlayerTickets(Colour.Black, Ticket.Double);
    	return score;
    }
    
    // returns the order of node
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
    // each detective is not considering tickets (this took too long)
    private List<Integer> movesAwayFromDetectives(int mrXLocation, ScotlandYardModel model)
    {
    	Set<Integer> visited = new HashSet<Integer>();
    	Set<Integer> detectiveLocations = detectiveLocations(model);
    	int numDetectives =  detectiveLocations.size();
    	Set<Integer> locations = new HashSet<Integer>();
    	List<Integer> detectiveMovesAway = new ArrayList<Integer>();
    	locations.add(mrXLocation);
    	for(int i = 0; detectiveMovesAway.size() < numDetectives; i++)
    	{
    		Set<Integer> tempLocations = new HashSet<Integer>();
    		visited.addAll(locations);
    		for(int location : locations)
    		{
    			if(detectiveLocations.contains(location))
    			{
    				detectiveMovesAway.add(i);
    				detectiveLocations.remove(location);
    			}
    			Set<Edge<Integer, Route>> edges = graph.getEdgesFrom(location);
    			for(Edge<Integer, Route> edge : edges)
    			{
    				if(visited.contains(edge.target()) || edge.data().equals(Route.Boat)) continue;
    				tempLocations.add(edge.target());
    			}
    		}
    		locations = tempLocations;
    	}
    	return detectiveMovesAway;
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
    
    //returns the approximate area ocupied by the @param nodes
    public int howSreadOut(Set<Integer> nodes)
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
			else if(point.x < minX) minX = point.x;
			else if(point.y > maxY) maxY = point.y;
			else if(point.y < minY) minY = point.y;
		}
		return (maxX - minX) * (maxY - minY);
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
    
    // finds the start line distance a point is from the center 
    //approximating centre as point 113
    private double distanceFromCenter(int node)
    {
    	PositionsReader reader = new PositionsReader("resources/pos.txt");
		try
		{
			reader.read();
		}
		catch(Exception e)
		{
			System.err.println(e);
		}
		PositionsReader.Point point = reader.getPositionNode(node);
		int xDistance = point.x - 468;
		int yDistance = point.y - 413;
		return Math.sqrt(xDistance * xDistance + yDistance * yDistance);
    }
}

















