package player;

import java.util.*;
import java.lang.Math;
import scotlandyard.*;
import solution.*;

public class GameTreeNode {

	protected AIModel model;
	private Set<GameTreeNode> children;
	private final GameTreeNode parent; 
	private final Move move;
	private double value;


	public GameTreeNode(AIModel model, Move move) {
		this.model = model;
		this.move = move;
		children = new HashSet<GameTreeNode>();
		parent = null;
		if(isMaximizer()) value = Double.NEGATIVE_INFINITY;
		else value = Double.POSITIVE_INFINITY;
	}

	public GameTreeNode(AIModel model, Move move, GameTreeNode node) {
		this.model = model;
		this.move = move;
		children = new HashSet<GameTreeNode>();
		if(isMaximizer()) value = Double.NEGATIVE_INFINITY;
		else value = Double.POSITIVE_INFINITY;
		parent = node;
	}
	
	public GameTreeNode getParent() {
		return parent;
	}

	public Set<GameTreeNode> getChildren() {
		return children;
	}

	public void clearChildren() {
		children.clear();
	}

	public void addChild(GameTreeNode node) {
		children.add(node);
	}

	public Move getMove() {
		return move;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value; 
	}

	public boolean isMaximizer() {
		if(model.getCurrentPlayer().equals(Colour.Black)) return true;
		else return false;
	}
	public void reset() {
		children.clear();
		value = Double.NEGATIVE_INFINITY;
	}
}

