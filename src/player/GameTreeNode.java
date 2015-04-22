package player;

import java.util.*;
import java.lang.Math;
import scotlandyard.*;
import solution.*;

public class GameTreeNode {

	protected AIModel model;
	private Set<GameTreeNode> leafNodes;
	private double score;
	private Move move;
	private double value;
	private double alpha;
	private double beta; 

	public GameTreeNode(AIModel model) {
		this.model = model;
		leafNodes = new HashSet<GameTreeNode>();
		alpha = Double.NEGATIVE_INFINITY;
		beta = Double.POSITIVE_INFINITY;
	}

	public void setScore(double score)  {
		this.score = score;
	}

	public double getScore() {
		return score;
	}

	public void setLeafNodes(Set<GameTreeNode> leafNodes) {
		this.leafNodes = leafNodes;
	}

	public Set<GameTreeNode> getLeafNodes() {
		return leafNodes;
	}

	public void addLeafNode(GameTreeNode node) {
		leafNodes.add(node);
	}

	public void setMove(Move move) {
		this.move = move;
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

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public double getAlpha() {
		return alpha;
	}

	public double getBeta() {
		return beta; 
	}

	public void setBeta(double beta) {
		this.beta = beta;
	}

	public boolean isMaximizer() {
		if(model.getCurrentPlayer().equals(Colour.Black)) return true;
		else return false;
	}
	

	



	
}
