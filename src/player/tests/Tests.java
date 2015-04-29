package player.tests;

import scotlandyard.*;
import solution.*;
import player.*;
import gui.Gui;

import java.util.*;

public class Tests
{
	private static String positionsFilename = "resources/pos.txt";
    private static String imageFilename     = "resources/map.jpg";
	
	public static void main(String[] args)
	{
		System.out.println("Started tests");
		/*test1();
		test2();
		test3();
		test4();
		System.out.println("tests to check for Errors started");
		test5();
		test6();
		test7();
		test8();
		test9();
		test10();*/
		test11();
		/*test12();
		test13();
		test14();*/
		System.out.println("Finished tests");
	}
	
	/* puts mrX in location where all but one move puts him right next
	 * to a detective
	 */
	private static void test1()
	{
		Map<Ticket,Integer> mrXTickets= ModelCreator.createMrXTicketMap(4, 4, 4, 0, 0);
		TestModel model = ModelCreator.createModel(43, 44, 74, 15, 5, 8, mrXTickets);
		Set<Integer> validLocations = new HashSet<Integer>();
		validLocations.add(57);
		runTest("Corner case", model, validLocations);
		return;
	}
	
	/*All detectives are 'above' mrX he should move down as far as
	 * possible
	 */
	private static void test2()
	{
		TestModel model = ModelCreator.createModel(80, 48, 47, 23, 19, 49);
		Set<Integer> validLocations = new HashSet<Integer>();
		validLocations.add(98);
		validLocations.add(99);
		validLocations.add(111);
		validLocations.add(112);
		validLocations.add(113);
		runTest("Basic move away test", model, validLocations);
		return;
	}
	
	//MrX should move away from the detectives
	private static void test3()
	{
		Map<Ticket,Integer> mrXTickets= ModelCreator.createMrXTicketMap(4, 4, 4, 0, 0);
		TestModel model = ModelCreator.createModel(111, 133, 23, 102, 34, 79, mrXTickets);
		Set<Integer> validLocations = new HashSet<Integer>();
		validLocations.add(163);
		validLocations.add(153);
		validLocations.add(124);
		runTest("Basic move away test 2", model, validLocations);
		return;
	}
	
	//MrX should not move towards the detectives
	private static void test4()
	{
		Map<Ticket,Integer> mrXTickets= ModelCreator.createMrXTicketMap(4, 4, 4, 0, 0);
		TestModel model = ModelCreator.createModel(10, 22, 23, 13, 51, 33, mrXTickets);
		Set<Integer> validLocations = new HashSet<Integer>();
		validLocations.add(2);
		runTest("Dont get caught test from gameplay", model, validLocations);
		return;
	}
	
	//Error test - move made that is impossible
	private static void test5()
	{
		Map<Ticket,Integer> mrXTickets= ModelCreator.createMrXTicketMap(4, 4, 4, 0, 0);
		TestModel model = ModelCreator.createModel(11, 22, 23, 13, 51, 33, mrXTickets);
		Set<Integer> validLocations = new HashSet<Integer>();
		validLocations.add(3);
		validLocations.add(10);
		runTest("Dont make illegal move", model, validLocations);
		return;
	}
	
	
	//Error test - game halts instead of ending when MrX can't make move 
	private static void test6()
	{
		Map<Ticket,Integer> mrXTickets= ModelCreator.createMrXTicketMap(4, 4, 4, 0, 0);
		TestModel model = ModelCreator.createModel(21, 33, 10, 5, 6, 7, mrXTickets);
		Set<Integer> validLocations = new HashSet<Integer>();
		validLocations.add(21);
		runTest("Game halt test", model, validLocations);
		return;
	}
	
	//Trapped test - MrX played an impossible move to 199
	private static void test7()
	{
		Map<Ticket,Integer> mrXTickets= ModelCreator.createMrXTicketMap(23, 17, 7, 0, 0);
		TestModel model = ModelCreator.createModel(173, 187, 160, 174, 135, 171,mrXTickets);
		Set<Integer> validLocations = new HashSet<Integer>();
		validLocations.add(188);
		runTest("Trapped test", model, validLocations);
		return;
	}
	
	//Depth test - Alpha-Beta reached a depth of 22 - this is questionable !!!!
	private static void test8()
	{
		Map<Ticket,Integer> mrXTickets= ModelCreator.createMrXTicketMap(31, 16, 4, 0, 0);
		TestModel model = ModelCreator.createModel(187, 171, 188, 175, 135, 142,mrXTickets);
		Set<Integer> validLocations = new HashSet<Integer>();
		validLocations.add(172);
		validLocations.add(185);
		validLocations.add(198);
		runTest("Depth test", model, validLocations);
		return;
	}
	
	//mrX tells us he is playing a  move that make him take a secret move to where is currently is 
	private static void test9()
	{
		Map<Ticket,Integer> mrXTickets= ModelCreator.createMrXTicketMap(18, 7, 6, 0, 4);
		TestModel model = ModelCreator.createModel(77, 124, 79, 111, 66, 46, mrXTickets);
		Set<Integer> validLocations = new HashSet<Integer>();
		validLocations.add(95);
		validLocations.add(96);
		validLocations.add(76);
		validLocations.add(78);
		validLocations.add(94);
		runTest("not move test", model, validLocations);
		return;
	}
	
	//Double move test 1 - Makes a double move because he is forced to. 
	private static void test10()
	{
		Map<Ticket,Integer> mrXTickets= ModelCreator.createMrXTicketMap(18, 7, 6, 2, 4);
		TestModel model = ModelCreator.createModel(170, 156, 186, 184, 193, 158, mrXTickets);
		Set<Integer> validLocations = new HashSet<Integer>();
		validLocations.add(115);
		runTest("Forced double move", model, validLocations);
		return;
	}
	
	//Double move test 2 - Double backs on itself
	private static void test11()
	{
		Map<Ticket,Integer> mrXTickets= ModelCreator.createMrXTicketMap(18, 7, 6, 2, 4);
		TestModel model = ModelCreator.createModel(162, 108, 161, 174, 135, 199, mrXTickets);
		testCreatorHelper(model);
		Set<Integer> validLocations = new HashSet<Integer>();
		validLocations.add(162);
		runTest("Double back on self", model, validLocations);
		return;
	}
	
	//puts mrX in a move where he should clearly make a double move
		private static void test12()
		{
			TestModel model = ModelCreator.createModel(43, 8, 44, 58, 5, 16);
			testCreatorHelper(model);
			Set<Integer> validLocations = new HashSet<Integer>();
			validLocations.add(73);
			runTest("use double move when should", model, validLocations);
			return;
		}
		
		//use single move when should - i.e. not double move
		private static void test13()
		{
			TestModel model = ModelCreator.createModel(63, 35, 62, 58, 66, 6);
			Set<Integer> validLocations = new HashSet<Integer>();
			validLocations.add(64);
			validLocations.add(65);
			validLocations.add(34);
			validLocations.add(100);
			validLocations.add(79);
			validLocations.add(80);
			validLocations.add(48);
			runTest("use double move when should", model, validLocations);
			return;
		}
		
		private static void test14()
		{
			Map<Ticket,Integer> mrXTickets = ModelCreator.createMrXTicketMap(25, 5, 12, 1, 2);
			TestModel model = ModelCreator.createModel(98, 46, 93, 122, 111, 163, mrXTickets);
			Set<Integer> validLocations = new HashSet<Integer>();
			validLocations.add(97);
			validLocations.add(99);
			runTest("use double move when should", model, validLocations);
			return;
		}
	
	private static void testCreatorHelper(TestModel model)
	{
		Set<Integer> possibleLocations = new HashSet<Integer>();
		for(Move move : model.validMoves(Colour.Black))
		{
			if (move instanceof MoveTicket) possibleLocations.add(((MoveTicket) move).target);
			else if (move instanceof MoveDouble) possibleLocations.add(((MoveDouble) move).move2.target);
		}
		System.out.println("possible Moves -> " + possibleLocations);
		try 
		{ 
			Gui test = new Gui(model, imageFilename, positionsFilename);
			test.run();
		}
		catch(Exception e) {}
	}
	
	
	
	private static void runTest(String testName, TestModel model, Set<Integer> validLocations)
	{
		List<List<Move>> movesList = new ArrayList<List<Move>>();
		runTest(testName, model, movesList, validLocations);
	}
	
	private static void runTest(String testName, TestModel model, List<List<Move>> movesList, Set<Integer> validLocations)
	{
		Move move2 = model.getPlayerMove(Colour.Black);
		turn(model, move2);
		for(List<Move> moves : movesList)
		{
			for(Move move : moves)
			{
				turn(model, move);
			}
			Move move3 = model.getPlayerMove(Colour.Black);
			turn(model, move3);
		}
		int mrXLocation = model.getMrXLocation();
		System.out.println("---------------");
		System.out.println("test: " + testName );
		System.out.println("MrX at : " + mrXLocation);
		System.out.println("valid locations " +  validLocations);
		if(validLocations.contains(mrXLocation)) System.out.println("PASS");
		else System.out.println("<-------------------- FAIL -------------------->");
		System.out.println("---------------");
	}
	
	private static void turn(ScotlandYardModel model, Move move)
	{
		if(!move.colour.equals(model.getCurrentPlayer())) 
		{
			System.err.println("tried to play move in model on wrong turn");
			return;
		}
		if (move instanceof MoveTicket) model.play((MoveTicket) move);
		else if (move instanceof MoveDouble) model.play((MoveDouble) move);
		else if (move instanceof MovePass) model.play((MovePass) move);
		model.nextPlayer();
	}
}
