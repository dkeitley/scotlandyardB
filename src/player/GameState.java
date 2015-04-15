package player;

import java.util.*;
import scotlandyard.*;
import solution.*;

public class GameState {

	private List<Colour> players;
	private Map<Colour,Integer> locations;
	private Map<Colour,Map<Ticket,Integer>> tickets;

	
	//Game state initialised with list of players, player locations and player tickets
	public GameState(ScotlandYardModel model, MoveTicket move, int location) {
		locations = new HashMap<Colour,Integer>();
		tickets = new HashMap<Colour,Map<Ticket,Integer>>();
		players = model.getPlayers();
		Set<Ticket> ticketTypes = new HashSet<Ticket>();
		ticketTypes.add(Ticket.Taxi);
		ticketTypes.add(Ticket.Bus);
		ticketTypes.add(Ticket.Underground);
		
		for(Colour player : players) {
			Boolean isCurrentPlayer; 
			Colour currentPlayer = model.getCurrentPlayer();
			
			if(player.equals(currentPlayer)) {
				isCurrentPlayer = true;

				System.out.println(move.target);
				locations.put(player,move.target);

			} else {
				isCurrentPlayer = false;
				//only works if current player is Mr X! 

				locations.put(player,model.getPlayerLocation(player));
			}

			Map<Ticket,Integer> ticketMap = new HashMap<Ticket,Integer>();
			for(Ticket ticketType : ticketTypes) {
				if(isCurrentPlayer && ticketType.equals(move.ticket)) {
					ticketMap.put(ticketType,model.getPlayerTickets(player,ticketType)-1);
				} else {
					ticketMap.put(ticketType,model.getPlayerTickets(player,ticketType));
				}
			}
			if(player.equals(Colour.Black)) {
				if(isCurrentPlayer && move.ticket.equals(Ticket.Secret)) {
					ticketMap.put(Ticket.Secret,model.getPlayerTickets(player,Ticket.Secret)-1);
				} else {
					ticketMap.put(Ticket.Secret,model.getPlayerTickets(player,Ticket.Secret));
				}
				if(isCurrentPlayer && move.ticket.equals(Ticket.Double)) {
					ticketMap.put(Ticket.Double,model.getPlayerTickets(player,Ticket.Double)-1);
				} else { 
					ticketMap.put(Ticket.Double,model.getPlayerTickets(player,Ticket.Double));
				}
			} 
			tickets.put(player,ticketMap);
		}
	}
	
	public GameState(ScotlandYardModel model, int location) {

		players = model.getPlayers();
		
		Set<Ticket> ticketTypes = new HashSet<Ticket>();
		ticketTypes.add(Ticket.Taxi);
		ticketTypes.add(Ticket.Bus);
		ticketTypes.add(Ticket.Underground);
		
		for(Colour player : players) {
		
			Boolean isCurrentPlayer; 
			if(player.equals(model.getCurrentPlayer())) {
				isCurrentPlayer = true;
			} else {
				isCurrentPlayer = false;				
			}
			
			locations.put(player,model.getPlayerLocation(player));
			
			Map<Ticket,Integer> ticketMap = new HashMap<Ticket,Integer>();
			for(Ticket ticketType : ticketTypes) {
				ticketMap.put(ticketType,model.getPlayerTickets(player,ticketType));
			}
			if(player.equals(Colour.Black)) {
				ticketMap.put(Ticket.Secret,model.getPlayerTickets(player,Ticket.Secret));
				ticketMap.put(Ticket.Double,model.getPlayerTickets(player,Ticket.Double));
			} 
			tickets.put(player,ticketMap);
		}
	}
	
	public List<Colour> getPlayers() {
		return players;
	}

	public int getPlayerLocation(Colour player) {
		return locations.get(player);
	}

	public int getPlayerTickets(Colour player, Ticket ticket) {
		Map<Ticket,Integer> ticketMap = tickets.get(player);
		return ticketMap.get(ticket);
	}
}
