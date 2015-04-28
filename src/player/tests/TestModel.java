package player.tests;

import scotlandyard.Colour;
import scotlandyard.MoveTicket;
import solution.ScotlandYardModel;
import java.util.*;

public class TestModel extends ScotlandYardModel
{
	int mrXLocation;
	
	public TestModel(int numDetectives, List<Boolean>rounds, String graphFilename, int initialMrXLocation)
	{
		super(numDetectives, rounds, graphFilename);
		this.mrXLocation = initialMrXLocation;
	}
	
	@Override
	public void play(MoveTicket moveTicket)
	{
		super.play(moveTicket);
		if (moveTicket.colour.equals(Colour.Black))
		{
			mrXLocation = moveTicket.target;
		}
	}
	
	public int getMrXLocation()
	{
		return mrXLocation;
	}
}
