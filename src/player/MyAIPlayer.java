package player;

import java.util.*;
import java.lang.Math;
import scotlandyard.*;

class MyAIPlayer implements Player
{
	private Graph<Integer, Route> graph;
    
	//Initialises graph 
    public MyAIPlayer(ScotlandYardView view, String graphFilename)
    {
		ScotlandYardGraphReader reader = new ScotlandYardGraphReader();
		try { graph = reader.readGraph(graphFilename); } 
		catch(Exception e) { System.err.println(e.getMessage()); }
    }
    
    //TODO this needs to be implemented in the next part
    public Move notify(int location, Set<Move> moves) 
    {
    	return null;
    }
    
    //A rough board scoring function - weights of relatve components still
    // to be sorted out 
    public double score(ScotlandYardView view, int mrXLocation)
    {
    	List<Integer> movesAwayFromDetectives = movesAwayFromDetectives(mrXLocation, view);
    	System.out.println(movesAwayFromDetectives);
    	double score = 0;
    	score += orderOfNode(mrXLocation);
    	score += adjacentNodeOrder(mrXLocation);
    	score -= distanceFromCenter(mrXLocation) / 100;
    	int closesedDetective = Collections.min(movesAwayFromDetectives);
    	if(closesedDetective <= 1) score += -100;
    	else score += 5 * closesedDetective;
    	score += 2 * average(movesAwayFromDetectives);
    	score += view.getPlayerTickets(Colour.Black, Ticket.Secret);
    	score += view.getPlayerTickets(Colour.Black, Ticket.Double);
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
    // each detective is 
    private List<Integer> movesAwayFromDetectives(int mrXLocation, ScotlandYardView view)
    {
    	Set<Integer> detectiveLocations = detectiveLocations(view);
    	int numDetectives =  detectiveLocations.size();
    	Set<Integer> locations = new HashSet<Integer>();
    	List<Integer> detectiveMovesAway = new ArrayList<Integer>();
    	locations.add(mrXLocation);
    	for(int i = 0; detectiveMovesAway.size() < numDetectives; i++)
    	{
    		Set<Integer> tempLocations = new HashSet<Integer>();
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
    				tempLocations.add(edge.target());
    			}
    		}
    		locations = tempLocations;
    	}
    	return detectiveMovesAway;
    }
    
    //returns the locations of all the detectives
    private Set<Integer> detectiveLocations(ScotlandYardView view)
    {
    	List<Colour> players = view.getPlayers();
    	Set<Integer> detectiveLocations = new HashSet<Integer>();
    	for(Colour player : players)
    	{
    		if(player.equals(Colour.Black)) continue;
    		detectiveLocations.add(view.getPlayerLocation(player));
    	}
    	System.out.println("detective locations -> " + detectiveLocations);
    	System.out.println("black at -> " + view.getPlayerLocation(Colour.Black));
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
