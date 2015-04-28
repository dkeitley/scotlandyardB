package player;

import java.util.*;
import java.lang.Math;
import scotlandyard.*;
import solution.*;

public class GameTreeNode
{

	protected AIModel model;
	private Set<GameTreeNode> children;
	private Move move;
	private double value;
	private GameTreeNode parent; 

	public GameTreeNode(AIModel model)
	{
		this.model = model;
		children = new HashSet<GameTreeNode>();
		parent = null;
	}
	public void setParent(GameTreeNode node) { parent = node; }
	public GameTreeNode getParent() { return parent; }
	public void setChildren(Set<GameTreeNode> children) { this.children = children; }
	public Set<GameTreeNode> getChildren() { return children; }
	public void addChild(GameTreeNode node) { children.add(node); }
	public void setMove(Move move) { this.move = move; }
	public Move getMove() { return move; }
	public double getValue() { return value; }
	public void setValue(double value) { this.value = value; }

	public boolean isMaximizer() {
		if(model.getCurrentPlayer().equals(Colour.Black)) return true;
		else return false;
	}

}


