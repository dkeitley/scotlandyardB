package player;

import java.util.*;
import java.lang.Math;
import scotlandyard.*;
import solution.*;

class AIModel extends ScotlandYardModel implements Spectator {

	private List<Move> movesPlayed;
	private int mrXLocation;

	public AIModel(ScotlandYardView view, String graphFilename, List<Move> movesPlayed, Set<Colour> colours) {
		super(colours.size()-1, view.getRounds(),graphFilename);
		this.movesPlayed = movesPlayed;	   	
		
		Set<Ticket> ticketTypes = new HashSet<Ticket>();
		ticketTypes.add(Ticket.Taxi);	
		ticketTypes.add(Ticket.Bus);	
		ticketTypes.add(Ticket.Underground);
		for(Colour colour : colours) {
			Map<Ticket,Integer> tickets = new HashMap<Ticket,Integer>();
			for(Ticket ticket : ticketTypes) {
				tickets.put(ticket,view.getPlayerTickets(colour,ticket));
			}
			if(colour.equals(Colour.Black)) {
				tickets.put(Ticket.Secret,view.getPlayerTickets(colour,Ticket.Secret));
				tickets.put(Ticket.Double,view.getPlayerTickets(colour,Ticket.Double));
				join(null,colour,186,tickets);
			} else {
				join(null,colour,view.getPlayerLocation(colour),tickets);
			}
		}
		
		for(Move move : movesPlayed) {
			if(isGameOver() == false) {
				int target;
				if(move instanceof MoveTicket) {
					MoveTicket moveTicket = (MoveTicket) move;
					play(moveTicket);
					target = moveTicket.target;
				} else if (move instanceof MoveDouble) {
					MoveDouble doubleMove = (MoveDouble) move;
					play(doubleMove);
					target = doubleMove.move2.target;
				} else {
					play((MovePass)move);
					target = mrXLocation;
				}

				if (move.colour.equals(Colour.Black)) mrXLocation = target;
				nextPlayer();
				
			} else { break; }
		}
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
			this.movesPlayed.add(move);
		}

		public List<Move> getMovesPlayed() {
			return movesPlayed;
		}

		public int getMrXLocation() {
			return mrXLocation;
		}
	}
