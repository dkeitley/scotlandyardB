package player;

import java.util.*;
import java.lang.Math;

import scotlandyard.*;
import solution.*;

// An AI to play as MrX X in a game of ScotlandYard


public class MyAIPlayer implements Player, Spectator
{

	private Graph<Integer, Route> graph;
	private ScotlandYardView InitialView;
	private AIModel currentGame;
	private String graphFilename;
	private boolean firstMove;
	private HashMap<Double, Move> firstMoves = new HashMap<Double, Move>();
	private boolean timeUp;
	private long startTime;

	public MyAIPlayer(ScotlandYardView view, String graphFilename)
	{
		System.out.println(view.isGameOver());
		this.InitialView = view;
		this.graphFilename = graphFilename;
		firstMove = true;
	}
	
	//notifies the model that a detective has made a move
	public void notify(Move move)
	{
		if(!move.colour.equals(Colour.Black)) currentGame.turn(move);
		return;
	}
	
	//function to set up model on first move - i.e first call of notify by the 
	//CS dept. client model
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
		firstMove = false;
	}

	//Selects best move based on the best score returned from alpha-beta algorithm
	//and notifies our internal model of the move
	public Move notify(int location, Set<Move> moves)
	{	
		startTime = System.currentTimeMillis();
		if(firstMove) firstMove(location);
		if(moves.size() == 0) return MovePass.instance(Colour.Black);
		timeUp = false;
		firstMoves.clear();
		Double score = 0.0;
		Double previousScore = 0.0;
		for(int i = 2; !timeUp; i++)
		{
			previousScore = score;
			score = bestMove(currentGame, Colour.Black, Double.POSITIVE_INFINITY, 0, i);
		}
		Move bestMove = firstMoves.get(previousScore);
		currentGame.turn(bestMove);
		return bestMove;
    }
	
	//gets the player that last made a move
	private Colour getPreviousPlayer(AIModel model)
	{
		List<Colour> colours = model.getPlayers();
		Colour currentPlayer = model.getCurrentPlayer();
		int playerNum = colours.indexOf(currentPlayer);
		if(playerNum == 0) return colours.get(colours.size() - 1);
		else return colours.get(playerNum - 1);
	}
	
	//calls CPUMove or humanMove depending on who's go it is and keeps track of time
	// and depth of tree and if game has been won
	private double bestMove(AIModel model, Colour colour, double currentExtreme, int depth, int maxDepth)
	{
		//System.out.println("in best move");
		//System.out.println(System.currentTimeMillis() - startTime);
		if(model.isGameOver())
		{
			//System.out.println("enetered game is over");
			if(model.getWinningPlayers().contains(Colour.Black)) return Double.POSITIVE_INFINITY;
			else return Double.NEGATIVE_INFINITY;
		}
		if(System.currentTimeMillis() - startTime > 12000) timeUp = true;
		if(timeUp) return Double.NaN;
		if(depth == maxDepth) return score(model);
		if(colour.equals(colour.Black)) return cpuMove(model, currentExtreme, depth, maxDepth, false);
		else if(getPreviousPlayer(model).equals(Colour.Black)) return humanMove(model, currentExtreme, depth, maxDepth, true);
		else return humanMove(model, currentExtreme, depth, maxDepth, false);
	}
	
	//Simulates what MrX wants to do i.e. chooses the highest scoring move passes up to it
	private double cpuMove(AIModel model, double currentExtreme, int depth, int maxDepth, boolean isParentMaximiser)
	{
		double currentMaximum = Double.NEGATIVE_INFINITY;
		for(Move move : model.validMoves(Colour.Black))
		{
			AIModel newModel = model.copy();
			newModel.turn(move);
			Double currentModel = bestMove(newModel, newModel.getCurrentPlayer(), currentMaximum, depth + 1, maxDepth);
			if(depth == 0) firstMoves.put(currentModel, move);
			if (currentModel > currentMaximum) currentMaximum = currentModel;
			if(currentMaximum > currentExtreme && !isParentMaximiser) return currentMaximum;
		}
		if(timeUp) return Double.NaN;
		return currentMaximum;
	}
	
	//Simulates what detective wants to do i.e. chooses the lowest scoring move passes up to it
	private double humanMove(AIModel model, double currentExtreme, int depth, int maxDepth, boolean isParentMaximiser)
	{
		double currentMinimum = Double.POSITIVE_INFINITY;;
		for(Move move : model.validMoves(model.getCurrentPlayer()))
		{
			AIModel newModel = model.copy();
			newModel.turn(move);
			Double currentModel = bestMove(newModel, newModel.getCurrentPlayer(), currentMinimum, depth + 1, maxDepth);
			if (currentModel < currentMinimum) currentMinimum = currentModel;
			if(currentMinimum < currentExtreme && isParentMaximiser) return currentMinimum;
		}
		if(timeUp) return Double.NaN;
		return currentMinimum;
	}

	//scores a board from point of view of MrX
	public double score(AIModel model)
	{
		int mrXLocation = model.getMrXLocation();
		List<Integer> movesAwayFromDetectives = movesAwayFromDetectives(mrXLocation, model);
		int closesedDetective = Collections.min(movesAwayFromDetectives);
		Set<Integer> possibleLoctions = possibleLocations(model);
		
		double validMoveScore = model.validMoves(Colour.Black).size() / 10;
		float adjacentNodeOrderScore = adjacentNodeOrder(mrXLocation);
		double fromCenterScore = - distanceFromCenter(mrXLocation) / 100;
		double detectiveMoveAwayScore = 2 * average(movesAwayFromDetectives);
		int secretScore = model.getPlayerTickets(Colour.Black, Ticket.Secret);
		int doubleScore = 200 * model.getPlayerTickets(Colour.Black, Ticket.Double);
		int possLocationScore = possibleLoctions.size();
		double spreadOutScore = howSpreadOut(possibleLoctions) / 1000;
		double ticketsScore = 0.0001 * model.getPlayerTickets(Colour.Black, Ticket.Bus) + 0.001 * model.getPlayerTickets(Colour.Black, Ticket.Underground);
		int deteciveScore;
		if (closesedDetective == 1) 
		{
			deteciveScore = -230;
			if(average(movesAwayFromDetectives) <= 1.8) deteciveScore += -10000;
		}
		
		else deteciveScore = 5 * closesedDetective;
		
		double score = validMoveScore + adjacentNodeOrderScore + fromCenterScore + detectiveMoveAwayScore + secretScore;
		score += secretScore + doubleScore + possLocationScore + spreadOutScore + deteciveScore + ticketsScore;
		
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
					if (visited.contains(edge.target()) || edge.data().equals(Route.Boat))
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
				if (ticketUsed.equals(ticketReqired) || ticketUsed.equals(Ticket.Secret))
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

	// takes average of 3 smallest numbers
	private double average(List<Integer> list)
	{
		int total = 0;
		List<Integer> copy = new ArrayList<Integer>(list);
		for(int i = 0; i < 3 ; i++)
		{
			Integer min = Collections.min(copy);
			copy.remove(min);
			total += min;
		}
		return total / 3;
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


