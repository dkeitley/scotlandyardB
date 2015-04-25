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

class MyAIPlayer implements Player, Spectator
{

	private Graph<Integer, Route> graph;
	private ScotlandYardView InitialView;
	private AIModel currentGame;
	private GameTreeNode rootNode;
	private String graphFilename;
	private boolean firstMove;
	private boolean alreadyNotified;

	public MyAIPlayer(ScotlandYardView view, String graphFilename)
	{
		this.InitialView = view;
		this.graphFilename = graphFilename;
		firstMove = true;
		alreadyNotified = false;
	}
	
	//notifies the model that someone has made a move
	public void notify(Move move)
	{
		if(alreadyNotified == true) {
			alreadyNotified = false;
			return;
		} else {
			rootNode.model.notify(move);
		}
		//System.out.println("MrXLocation: " + rootNode.model.getMrXLocation());
	}
	
	//function to set up model on first move
	private void firstMove(int mrXLocation)
	{
		ScotlandYardGraphReader reader = new ScotlandYardGraphReader();
		try
		{
			graph = reader.readGraph(graphFilename);
		} 
		catch (Exception e)
		{
			System.err.println(e.getMessage());
		}
		currentGame = new AIModel(InitialView, graphFilename, mrXLocation);
		rootNode = new GameTreeNode(currentGame,null);
		firstMove = false;
	}

	//Selects best move based on the best score returned from minimax algorithm
	public Move notify(int location, Set<Move> moves)
	{	
		if(firstMove) firstMove(location);

		//long startTime = System.currentTimeMillis();
		double bestScore = alphaBeta(rootNode,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,0,2);
		Move bestMove = findBestMove(bestScore);
		System.out.println("Move to be played: " + bestMove + " " + bestScore);
		
		rootNode.clearChildren();
		rootNode.model.notify(bestMove);
		alreadyNotified = true;
		
		return bestMove;		
    }

    //finds the move to play based on the scores of each GameTreeNode 
    private Move findBestMove(double bestScore) {
    	Move bestMove = null;
	for(GameTreeNode node : rootNode.getChildren()) {
		if(node.getValue() == bestScore) {
			bestMove = node.getMove();
		}
	}
	return bestMove;
    }

    //test function: prints the sequence of moves to get to the current node in game tree	
    private void printTree(double score, GameTreeNode node) {
	System.out.println("-------------------------------------");

	List<GameTreeNode> route = new ArrayList<GameTreeNode>();
	route.add(node);
	
	while(node.getParent()!=null) {
		route.add(node.getParent());
		node = node.getParent();
	}

	System.out.println(route.get(route.size()-2).getMove());
	for(int i = route.size()-3; i>=0; i--) {
		GameTreeNode pathNode = route.get(i);
		if(!pathNode.getMove().equals(null)) {
			System.out.println("          |");
			System.out.println("          |");
			System.out.println("          V");
			System.out.println("");
			System.out.println(pathNode.getMove());
		}
	}

	System.out.println("");
	System.out.println("Score: " + score);
	System.out.println("-------------------------------------");
    } 

    private GameTreeNode createChild(GameTreeNode node, Move move) {
   	AIModel newModel = node.model.copy();
	newModel.turn(move);
	return new GameTreeNode(newModel,move,node);
    }

    //Uses alpha-beta pruning to find the best game scenario using the set of validMoves
    private double alphaBeta(GameTreeNode node, double alpha, double beta, int currentDepth, int maxDepth) {
    
	//long time = System.currentTimeMillis();
	//printTree(node.getValue(),node);
	if(currentDepth == maxDepth) return score(node.model);

	Set<Move> validMoves = node.model.validMoves(node.model.getCurrentPlayer());
	Iterator iterator = validMoves.iterator();
		
		for(int i = 1; i <= validMoves.size();i++) {
			GameTreeNode child = createChild(node,(Move) iterator.next());
			node.addChild(child);
			
			if(node.isMaximizer()) {
				node.setValue(Math.max(alphaBeta(child, alpha, beta, currentDepth+1,maxDepth),node.getValue()));
				alpha = Math.max(alpha, node.getValue());
			
			} else { 
				node.setValue(Math.min(alphaBeta(child,alpha,beta,currentDepth+1,maxDepth),node.getValue()));
				beta = Math.min(beta,node.getValue());
			}
			if(beta <= alpha) break;			
		}
		return node.getValue();
    }

	// A rough board scoring function - weights of relatve components still
	// to be sorted out
	public double score(AIModel model)
	{
		int mrXLocation = model.getMrXLocation();
		List<Integer> movesAwayFromDetectives = movesAwayFromDetectives(mrXLocation, model);
		int closesedDetective = Collections.min(movesAwayFromDetectives);
		Set<Integer> possibleLoctions = possibleLocations(model);
		double score = 0;
		score += orderOfNode(mrXLocation);
		score += adjacentNodeOrder(mrXLocation);
		score -= distanceFromCenter(mrXLocation) / 100;
		score += 2 * average(movesAwayFromDetectives);
		score += model.getPlayerTickets(Colour.Black, Ticket.Secret);
		score += model.getPlayerTickets(Colour.Black, Ticket.Double);
		score += possibleLoctions.size();
		score += howSpreadOut(possibleLoctions) / 1000;
		if (closesedDetective == 1)
			score += -100;
		else if (closesedDetective == 0)
			score += -1000;
		else
			score += 5 * closesedDetective;
		return score;
	}

	// returns the order of @param node
	private int orderOfNode(int node)
	{
		return graph.getEdgesFrom(node).size();
	}

	// returns the average order of the adjacent nodes
	private float adjacentNodeOrder(int node)
	{
		int total = 0;
		Set<Edge<Integer, Route>> edges = graph.getEdgesFrom(node);
		for (Edge<Integer, Route> edge : edges)
		{
			total += orderOfNode(edge.target());
		}
		return (float) total / orderOfNode(node);
	}

	// returns list of integers representing number of moves away
	// each detective is not considering tickets (this took too long to run)
	// except disregarding boat edges
	private List<Integer> movesAwayFromDetectives(int mrXLocation, ScotlandYardModel model)
	{
		Set<Integer> visited = new HashSet<Integer>();
		Set<Integer> detectiveLocations = detectiveLocations(model);
		Set<Integer> locations = new HashSet<Integer>();
		List<Integer> detectiveMovesAway = new ArrayList<Integer>();
		locations.add(mrXLocation);
		for (int i = 0; detectiveMovesAway.size() < detectiveLocations.size(); i++)
		{
			Set<Integer> tempLocations = new HashSet<Integer>();
			visited.addAll(locations);
			for (int location : locations)
			{
				if (detectiveLocations.contains(location))
					detectiveMovesAway.add(i);
				Set<Edge<Integer, Route>> edges = graph.getEdgesFrom(location);
				for (Edge<Integer, Route> edge : edges)
				{
					if (visited.contains(edge.target())
							|| edge.data().equals(Route.Boat))
						continue;
					tempLocations.add(edge.target());
				}
			}
			locations = tempLocations;
		}
		return detectiveMovesAway;
	}

	// returns the locations of all the detectives
	private Set<Integer> detectiveLocations(ScotlandYardModel model)
	{
		List<Colour> players = model.getPlayers();
		Set<Integer> detectiveLocations = new HashSet<Integer>();
		for (Colour player : players)
		{
			if (player.equals(Colour.Black))
				continue;
			detectiveLocations.add(model.getPlayerLocation(player));
		}
		return detectiveLocations;
	}

	// returns the possible locations mrX could be for @param model
	public Set<Integer> possibleLocations(AIModel model)
	{
		List<MoveTicket> mrXMoves = model.getMrXMoves();
		Set<Integer> possibleLocations = new HashSet<Integer>();
		possibleLocations.add(model.getPlayerLocation(Colour.Black));
		int currentRound = model.getRound();
		int lastShowRound = getLastShowRond(model);
		for (int i = lastShowRound + 1; i <= currentRound; i++)
		{
			MoveTicket moveTicket = mrXMoves.get(i - 1);
			possibleLocations = possibleLocations2(moveTicket, possibleLocations);
		}
		return possibleLocations;
	}

	// returns the possible locations mrX could be if he was known to be at one
	// of the @param nodes and used a @param moveTicket
	private Set<Integer> possibleLocations2(MoveTicket moveTicket, Set<Integer> nodes)
	{
		Set<Integer> newPossibleNodes = new HashSet<Integer>();
		Ticket ticketUsed = moveTicket.ticket;
		for (int node : nodes)
		{
			for (Edge<Integer, Route> edge : graph.getEdgesFrom(node))
			{
				Ticket ticketReqired = Ticket.fromRoute(edge.data());
				if (ticketUsed.equals(ticketReqired)
						|| ticketUsed.equals(Ticket.Secret))
					newPossibleNodes.add(edge.target());
			}
		}
		return newPossibleNodes;
	}

	// returns the last round that has been played that was a show round
	private int getLastShowRond(ScotlandYardModel model)
	{
		int currentRound = model.getRound();
		List<Boolean> rounds = model.getRounds();
		int lastShowRound = currentRound;
		while (true)
		{
			if (rounds.get(lastShowRound) || lastShowRound == 0)
				break;
			else
				lastShowRound--;
		}
		return lastShowRound;
	}

	// returns the approximate area ocupied by the @param nodes
	public int howSpreadOut(Set<Integer> nodes)
	{
		PositionsReader reader = new PositionsReader("resources/pos.txt");
		try
		{
			reader.read();
		} catch (Exception e)
		{
			System.err.println(e);
		}
		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
		for (int node : nodes)
		{
			PositionsReader.Point point = reader.getPositionNode(node);
			if (point.x > maxX)
				maxX = point.x;
			if (point.x < minX)
				minX = point.x;
			if (point.y > maxY)
				maxY = point.y;
			if (point.y < minY)
				minY = point.y;
		}
		return (maxX - minX) * (maxY - minY);
	}

	// takes average of list of integers
	private double average(List<Integer> list)
	{
		int total = 0;
		for (int num : list)
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
		try
		{
			reader.read();
		} catch (Exception e)
		{
			System.err.println(e);
		}
		PositionsReader.Point point = reader.getPositionNode(node);
		int xDistance = point.x - 468;
		int yDistance = point.y - 413;
		return Math.sqrt(xDistance * xDistance + yDistance * yDistance);
	}
}

