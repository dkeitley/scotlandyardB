package player;

import java.util.*;
import java.lang.Math;
import scotlandyard.*;
import solution.*;

public class GameTreeNode {

	private AIModel model;
	private Set<GameTreeNode> leafNodes;
	private GameTreeNode parentNode;
	private double score;
	private Move move;

	public GameTreeNode(AIModel model) {
		this.model = model;
		leafNodes = new HashSet<GameTreeNode>();
	}

	public void setScore(double score)  {
		this.score = score;
	}

	public void setLeafNodes(Set<GameTreeNode> leafNodes) {
		this.leafNodes = leafNodes;
	}

	public void addLeafNode(GameTreeNode node) {
		leafNodes.add(node);
	}

	public void setParentNode(GameTreeNode node) {
		parentNode = node;
	}

	public setMove(Move move) {
		this.move = move;
	}
}
