package player.tests;

import scotlandyard.*;
import solution.*;
import player.*;

import java.util.*;

public class ModelCreator
{
	private static final String graphFilename =  "resources/graph.txt";
	
	public static TestModel createModel(int blackPos, int bluePos, int greenPos, int redPos, int whitePos, int yellowPos, Map<Ticket, Integer> ticketMapMrX)
	{
		//number of detectives; show rounds as a comma separated list
		TestModel model = createModel(5, "3, 8, 13, 18, 24", 24, blackPos);
		
		//are you mrX?; numTaxi; numBus; numUnderground; numDouble; numSecret
		Map<Ticket, Integer> ticketMapDetective1 = createTicketMap(false, 11,8,4,0,0);
		Map<Ticket, Integer> ticketMapDetective2 = createTicketMap(false, 11,8,4,0,0);
		Map<Ticket, Integer> ticketMapDetective3 = createTicketMap(false, 11,8,4,0,0);
		Map<Ticket, Integer> ticketMapDetective4 = createTicketMap(false, 11,8,4,0,0);
		Map<Ticket, Integer> ticketMapDetective5 = createTicketMap(false, 11,8,4,0,0);
		
		MyAIPlayer Ai = new MyAIPlayer(model, graphFilename);
		model.spectate(Ai);
		
		// player; colour; start location; ticket map
		model.join(Ai, Colour.Black, blackPos, ticketMapMrX);
		model.join(null, Colour.Blue, bluePos, ticketMapDetective1);
		model.join(null, Colour.Green, greenPos, ticketMapDetective2);
		model.join(null, Colour.Red, redPos, ticketMapDetective3);
		model.join(null, Colour.White, whitePos, ticketMapDetective4);
		model.join(null, Colour.Yellow, yellowPos, ticketMapDetective5);
		
		return model;
	}
	
	public static TestModel createModel(int blackPos, int bluePos, int greenPos, int redPos, int whitePos, int yellowPos)
	{
		Map<Ticket, Integer> ticketMapMrX = createTicketMap(true, 4, 4, 4, 2, 5);
		return createModel(blackPos, bluePos, greenPos, redPos, whitePos, yellowPos, ticketMapMrX);
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
	
	public static Map<Ticket, Integer> createMrXTicketMap(int taxi, int bus, int underground, int doubleMoves, int secret)
	{
		return createTicketMap(true, taxi, bus, underground, doubleMoves, secret);
	}
	
	private static TestModel createModel(int numDetectives, String showRounds, int numRounds, int initialMrXlocation)
	{
		 List<Boolean> rounds = createShowRoundsList(showRounds, numRounds);
		 try
		 {
		 	return new TestModel(numDetectives, rounds, graphFilename, initialMrXlocation); 
		 }
		 catch(Exception e) 
		 {
		 	return null;
		 }
	}
	
	private static List<Boolean> createShowRoundsList(String showRounds, int numRounds)
	{
		String[] rounds = showRounds.split(" , | ,|, |,");
		Boolean[] roundsList = new Boolean[numRounds + 1];
		Arrays.fill(roundsList, false);
		for(String round : rounds)
		{
			int showRound = Integer.parseInt(round);
			roundsList[showRound] = true;
		}
		return Arrays.asList(roundsList);
	}
}
