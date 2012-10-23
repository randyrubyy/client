package sample.utilities.Search;

import sample.utilities.Search.Node;

public class AStarSearchNode {

	private double pathCost;
	private double heuristic;
	private AStarSearchNode parent = null;
	private Node state = null;

	public AStarSearchNode(double pathCost, AStarSearchNode parent, Node state) {
		this.pathCost = pathCost;
		this.parent = parent;
		this.state = state;
	}

	public void addToPathCost(int cost) {
		pathCost += cost;
	}

	public AStarSearchNode getParent() {
		return parent;
	}

	public void setParent(AStarSearchNode parent) {
		this.parent = parent;
	}

	public double getPathCost() {
		return pathCost;
	}

	public void setPathCost(int pathCost) {
		this.pathCost = pathCost;
	}

	public Node getState() {
		return state;
	}

	public void setState(Node state) {
		this.state = state;
	}

	public double getHeuristic() {
		return heuristic;
	}

	public void setHeuristic(double heuristic) {
		this.heuristic = heuristic;
	}

	@Override
	public String toString() {
		return "Node( " + state + ", " + pathCost + ", " + heuristic + " )";
	}
}
