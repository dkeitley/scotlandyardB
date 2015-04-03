package player;

import scotlandyard.*;
import solution.*;

import java.util.*;

class ModelCreator
{	
	public static void main(String[] args)
	{
		//number of detectives; show rounds as a comma seperated list
		ScotlandYardModel model = createModel(2, "0,3,5,8");
		
		//are you mrX?; numTaxi; numBus; numUnderground; numDouble; numSecret
		Map<Ticket, Integer> ticketMapMrX = createTicketMap(true, 5,4,8,1,3);
		Map<Ticket, Integer> ticketMapDetective1 = createTicketMap(false, 5,5,5,0,0);
		Map<Ticket, Integer> ticketMapDetective2 = createTicketMap(false, 5,5,5,0,0);
		
		int mrXLocation = 8;
		
		// player; colour; start location; ticket map
		model.join(new MyAIPlayer(model, "resources/graph.txt"), Colour.Black, mrXLocation, ticketMapMrX);
		model.join(new RandomPlayer(), Colour.Red, 171, ticketMapDetective1);
		model.join(new RandomPlayer(), Colour.Blue, 60, ticketMapDetective2);
		
		int numTurns = 12;
		
		while(!model.isGameOver())
		{
			model.turn();
		}
		System.out.println(model.getWinningPlayers() + " won");
	}
	
	private static Map<Ticket, Integer> createTicketMap(boolean mrX, int taxi, int bus, int underground, int doubleMoves, int secret)
	{
		Map<Ticket, Integer> tickets = new HashMap<Ticket, Integer>();
		tickets.put(Ticket.Taxi, taxi);
		tickets.put(Ticket.Bus, bus);
		tickets.put(Ticket.Underground, underground);
		if(mrX)
		{
			tickets.put(Ticket.Double, doubleMoves);
			tickets.put(Ticket.Secret, secret);
		}
		return tickets;
	}
	
	private static ScotlandYardModel createModel(int numDetectives, String showRounds)
	{
		 List<Boolean> rounds = createShowRoundsList(showRounds);
		 try
		 {
		 	return new ScotlandYardModel(numDetectives, rounds, "resources/graph.txt"); 
		 }
		 catch(Exception e) 
		 {
		 	return null;
		 }
	}
	
	private static List<Boolean> createShowRoundsList(String showRounds)
	{
		String[] rounds = showRounds.split(" , | ,|, |,");
		Boolean[] roundsList = new Boolean[25]; // 24 rounds 
		Arrays.fill(roundsList, false);
		for(String round : rounds)
		{
			int showRound = Integer.parseInt(round);
			roundsList[showRound] = true;
		}
		return Arrays.asList(roundsList);
	}
}
