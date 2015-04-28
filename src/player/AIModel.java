package player;

import java.util.*;
import java.lang.Math;

import scotlandyard.*;
import solution.*;


class AIModel extends ScotlandYardModel
{

	private List<Move> movesPlayed;
	private int initialMrXLocation;
	private int mrXLocation;
	private ScotlandYardView Initialview;
	private String graphFilename;
	private List<MoveTicket> mrXMoves;
	private List<Move> tempMoves;
	
	//creates a AI model as it would be at the start of the game when ready but before
	// any moves are played
	public AIModel(ScotlandYardView view, String graphFilename, int mrXLocation)
	{
		super(view.getPlayers().size() - 1, view.getRounds(), graphFilename);
		
		List<Colour> colours = view.getPlayers();
		Set<Ticket> ticketTypes = new HashSet<Ticket>();
		ticketTypes.add(Ticket.Taxi);
		ticketTypes.add(Ticket.Bus);
		ticketTypes.add(Ticket.Underground);
		for (Colour colour : colours)
		{
			Map<Ticket, Integer> tickets = new HashMap<Ticket, Integer>();
			for (Ticket ticket : ticketTypes)
			{
				tickets.put(ticket, view.getPlayerTickets(colour, ticket));
			}
			if (colour.equals(Colour.Black))
			{
				tickets.put(Ticket.Secret, view.getPlayerTickets(colour, Ticket.Secret));
				tickets.put(Ticket.Double, view.getPlayerTickets(colour, Ticket.Double));
				join(null, colour, mrXLocation, tickets);
			}
			else
			{
				join(null, colour, view.getPlayerLocation(colour), tickets);
			}
		}
		this.Initialview = view;
		this.initialMrXLocation = mrXLocation;
		this.graphFilename = graphFilename;
		this.movesPlayed = new ArrayList<Move>();
		this.mrXMoves = new ArrayList<MoveTicket>();
		this.tempMoves = new ArrayList<Move>();
	}
	
	//copys this AI model
	public AIModel copy()
	{
		AIModel modelCopy = new AIModel(Initialview, graphFilename, initialMrXLocation);
		for (Move move : movesPlayed)
		{
			modelCopy.turn(move);
		}
		return modelCopy;
	}
	
	//plays a moveTicket keeping track of moves that have been played
	@Override
	public void play(MoveTicket moveTicket)
	{
		if(moveTicket.colour.equals(Colour.Black) && moveTicket.target == getPlayerLocation(Colour.Black)) return;
		super.play(moveTicket);
		movesPlayed.add(moveTicket);
		if(moveTicket.colour.equals(Colour.Black))
		{
			mrXLocation = moveTicket.target;
			mrXMoves.add(moveTicket);
		}
	}
	
	//plays a moveDouble keeping track of moves that have been played
	@Override
	public void play(MoveDouble moveDouble)
	{
		//Michael could you explain the movesPlayed removed? 
		super.play(moveDouble);
		movesPlayed.remove(movesPlayed.size() - 1);
		movesPlayed.remove(movesPlayed.size() - 1);
		movesPlayed.add(moveDouble);
		Move move2 = (Move) moveDouble.move2;
		mrXLocation = ((MoveTicket)move2).target;
		mrXMoves.add((MoveTicket)moveDouble.move1);
		mrXMoves.add((MoveTicket)move2);
	}
	
	//plays a movePass keeping track of moves that have been played
	@Override
	public void play(MovePass movePass)
	{
		super.play(movePass);
		movesPlayed.add(movePass);
	}
	
	//plays the move and passes priority to next player 
	public void turn(Move move)
	{
		play(move);
		nextPlayer();
	}
	
	//like turn but deals with double move notifying 
	public void notify (Move move)
	{
		turn(move);
	}

	public List<Move> getMovesPlayed()
	{
		return movesPlayed;
	}
	

	public int getMrXLocation()
	{
		return mrXLocation;
	}
	
	public List<MoveTicket> getMrXMoves()
	{
		return mrXMoves;
	}
}
