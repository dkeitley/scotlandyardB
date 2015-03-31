package player;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class Positions
{	
	private List<String> positions;
	private String fileName;
	
	//gives the file name of the location of the positions txt file
	public Positions(String fileName)
	{
		this.fileName = fileName;
	}
	
	//reads in the positions file and saves it in varaible
	public void read() throws Exception
	{
		List<String> lines;
		Path path = Paths.get(fileName);
		lines = Files.readAllLines(path);
		this.positions = lines;
	}
	
	//returns the positions as a point of node number @param node
	public Point getPositionNode(int node)
	{
		String line = positions.get(node);
		String[] nums = line.split(" ");
		int x = Integer.parseInt(nums[1]);
		int y = Integer.parseInt(nums[2]);
		return new Point(x, y); // issues with equality  ??????
	}

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
