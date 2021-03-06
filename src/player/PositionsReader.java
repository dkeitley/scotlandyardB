package player;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class PositionsReader
{
	
	private List<String> positions;
	private String fileName;
	
	//saves location of file to read in from
	public PositionsReader(String fileName)
	{
		this.fileName = fileName;
	}
	
	//gets position from read in data
	public Point getPositionNode(int node)
	{
		String line = positions.get(node);
		String[] nums = line.split(" ");
		int x = Integer.parseInt(nums[1]);
		int y = Integer.parseInt(nums[2]);
		return new Point(x, y);
	}
	
	//reads in data from file
	public void read() throws Exception
	{
		List<String> lines;
		Path path = Paths.get(fileName);
		lines = Files.readAllLines(path);
		this.positions = lines;
	}
	
	//class to store coordinates in 2D space
	class Point
	{
		final int x;
		final int y;
		
		public Point(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
	}
}
