package player;

import gui.Gui;
import net.PlayerFactory;
import scotlandyard.Colour;
import scotlandyard.Player;
import scotlandyard.ScotlandYardView;
import scotlandyard.Spectator;

import java.io.IOException;

import java.util.*;

public class AIPlayerFactory implements PlayerFactory
{
	protected Map<Colour, PlayerType> typeMap;
	protected Set<Colour> colours;

	public enum PlayerType
	{
		AI, GUI
	}

	String imageFilename;
	String positionsFilename;

	protected List<Spectator> spectators;
	Gui gui;

	public AIPlayerFactory()
	{
		typeMap = new HashMap<Colour, PlayerType>();
		typeMap.put(Colour.Black, AIPlayerFactory.PlayerType.AI);
		typeMap.put(Colour.Blue, AIPlayerFactory.PlayerType.GUI);
		typeMap.put(Colour.Green, AIPlayerFactory.PlayerType.GUI);
		typeMap.put(Colour.Red, AIPlayerFactory.PlayerType.GUI);
		typeMap.put(Colour.White, AIPlayerFactory.PlayerType.GUI);
		typeMap.put(Colour.Yellow, AIPlayerFactory.PlayerType.GUI);

		positionsFilename = "resources/pos.txt";
		imageFilename = "resources/map.jpg";

		spectators = new ArrayList<Spectator>();
		colours = new HashSet<Colour>();
		colours.add(Colour.Black);
		colours.add(Colour.Blue);
		colours.add(Colour.Green);
		colours.add(Colour.Red);
		colours.add(Colour.White);
		colours.add(Colour.Yellow);
	}

	public AIPlayerFactory(Map<Colour, PlayerType> typeMap, String imageFilename, String positionsFilename)
	{
		this.typeMap = typeMap;
		this.imageFilename = imageFilename;
		this.positionsFilename = positionsFilename;
		spectators = new ArrayList<Spectator>();

		colours = new HashSet<Colour>();
		colours.add(Colour.Black);
		colours.add(Colour.Blue);
		colours.add(Colour.Green);
		colours.add(Colour.Red);
		colours.add(Colour.White);
		colours.add(Colour.Yellow);
	}

	@Override
	public Player player(Colour colour, ScotlandYardView view, String mapFilename)
	{
		switch (typeMap.get(colour))
		{
		case AI:
			MyAIPlayer newPlayer = new MyAIPlayer(view, mapFilename);
			spectators.add(newPlayer);
			return newPlayer;
		case GUI:
			return gui(view);
		default:
			MyAIPlayer newPlayer2 = new MyAIPlayer(view, mapFilename);
			spectators.add(newPlayer2);
			return newPlayer2;
		}
	}

	@Override
	public void ready()
	{
		if (gui != null)
			gui.run();
	}

	@Override
	public List<Spectator> getSpectators(ScotlandYardView view)
	{
		return spectators;
	}

	@Override
	public void finish()
	{
		if (gui != null)
			gui.update();
	}

	private Gui gui(ScotlandYardView view)
	{
		System.out.println("GUI");
		if (gui == null)
			try
			{
				gui = new Gui(view, imageFilename, positionsFilename);
				spectators.add(gui);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		return gui;
	}
}

