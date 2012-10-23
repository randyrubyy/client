package sample.utilities.Search;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Stack;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;
import sample.object.SampleWorldModel;
import sample.utilities.Path;
import sample.utilities.PositionLocate;
import sample.utilities.Search.AbstractSearch;
import sample.utilities.Search.Node;
import sample.utilities.Search.NodeCreator;

/**
 * A star search class.
 */
public final class AStarSearch extends AbstractSearch {

	// private static final Logger LOG = Logger.getLogger(AStarSearch.class);
	// private Collection<StandardEntity> standardGoals;

	private Collection<Node> startNodes;
	private Collection<Node> endNodes;
	private Node endPointNode;

	private PriorityQueue<AStarSearchNode> map;
	private HashMap<Node, AStarSearchNode> history;
	private int stepCount;
	private int limit;
	boolean limitExceeded;

	/**
	 * Construct a new AStar Search.
	 * 
	 * @param world
	 *            The world model to search.
	 * @param ignoreBlockedRoads
	 *            Whether searches should treat blocked roads as passable or
	 *            not. If true then blocked roads will be ignored, i.e. not
	 *            included in the path. If false then blocked roads will be
	 *            included in the path.
	 */
	public AStarSearch(SampleWorldModel model) {
		initialize(model);
	}

	protected void initialize(SampleWorldModel model) {
		this.model = model;
		history = new HashMap<Node, AStarSearchNode>();
		map = new PriorityQueue<AStarSearchNode>();
	}

	/**
	 * Gives a path from start to goal entity
	 * 
	 * @param from
	 *            start entity
	 * @param to
	 *            finish entity
	 * @param cost
	 *            gives the cost of the path
	 * @return Returns the list EntityId which contains found path
	 * 
	 */

	protected Path getPath(Area from, Point fromPoint,
			Collection<? extends Area> goals, Point goalPoint) {
		Path path;

		path = getPath(from, fromPoint, goals, goalPoint, 0);
		return path;
	}

	protected Path getPath(Area from, Point fromPoint,
			Collection<? extends Area> goals, Point goalPoint, int limit) {
		AStarSearchNode finalNode, currentSearchNode;
		Stack<EntityID> reversedPath;
		Path path;
		Node startNode;

		this.limit = limit;
		limitExceeded = false;
		endNodes = null;
		endPointNode = null;
		stepCount = 0;
		history.clear();
		if (fromPoint == null) {
			fromPoint = PositionLocate.getPosition(from, model);
		}
		startNode = NodeCreator.getNode(fromPoint, (Area) from);
		startNodes = new ArrayList<Node>();
		startNodes.add(startNode);// model_.getConnectedTerminalNodes(from);
		if (goalPoint != null) {
			StandardEntity entity;

			if (goals.size() == 1) {
				entity = goals.iterator().next();
				endPointNode = NodeCreator.getNode(goalPoint, (Area) entity);
				endNodes = new ArrayList<Node>();
				endNodes.add(endPointNode);
			}
		}
		if (endNodes == null) {
			endNodes = model.getConnectedTerminalNodes(goals);
		}
		// standardGoals = new ArrayList<StandardEntity>(endNodes);

		finalNode = search();
		if (finalNode == null) {
			// LOG.warn("No path found from " + startNodes + " to " +
			// endNodes);
			return null;
		}
		if (Double.isInfinite(finalNode.getPathCost())) {

			path = new Path();
			path.setCost(finalNode.getPathCost());
			return path;
		}
		if (limitExceeded) {
			path = new Path();
			path.setLimitExceeded(true);
			return path;
		}

		reversedPath = new Stack<EntityID>();
		currentSearchNode = finalNode;
		while (currentSearchNode != null) {
			Node current, previus;
			AStarSearchNode parent;

			current = currentSearchNode.getState();
			parent = currentSearchNode.getParent();

			if (parent != null) {
				EntityID id;

				previus = parent.getState();
				id = current.getCommonConnection(previus);
				if (reversedPath.isEmpty() || !reversedPath.peek().equals(id)) {
					reversedPath.push(id);
				}
				currentSearchNode = currentSearchNode.getParent();
			} else {
				if (current.isTerminal()) {
					EntityID id;

					id = current.getConnectedEntityId();
					if (reversedPath.isEmpty()
							|| !reversedPath.peek().equals(id)) {
						reversedPath.push(id);
					}
				}
				currentSearchNode = null;
			}
		}

		// remove the current road just in case
		// if(reversedPath.size() > 1){
		// reversedPath.pop();
		// }
		path = new Path();
		path.setCost(finalNode.getPathCost());
		// reversedPath.pop();
		while (!reversedPath.isEmpty()) {
			EntityID id;

			id = reversedPath.pop();
			path.add(id);
		}
		if (endPointNode != null) {
			path.setTarget(endPointNode.getPosition());
		}
		return path;
	}

	/*
	 * A search algorithm
	 */
	private AStarSearchNode search() {
		AStarSearchNodeComparator comperator;

		comperator = new AStarSearchNodeComparator();
		map = new PriorityQueue<AStarSearchNode>(4, comperator);

		for (Node node : startNodes) {
			AStarSearchNode searchNode;

			searchNode = new AStarSearchNode(0, null, node);
			map.add(searchNode);
			history.put(node, searchNode);
		}
		while (true) {
			AStarSearchNode searchNode;
			Node node;

			if (map.isEmpty() || map.peek() == null) {
				break;
			}

			searchNode = map.poll();
			if (Double.isInfinite(searchNode.getPathCost())) {
				// LOG.debug("Path has infinite cost in " + stepCount_ +
				// " steps");
				return searchNode;
			}
			node = searchNode.getState();
			if (endNodes.contains(node)) {
				// LOG.warn("Path is found within " + stepCount_ + " steps");
				return searchNode;
			} else {
				expand(searchNode);
			}
			stepCount++;
			if (limit > 0 && stepCount > limit) {
				// LOG.warn("Path exceeded limit " + limit_);
				limitExceeded = true;
				return searchNode;
			}
		}
		// LOG.warn("Path cannot not be found after " + stepCount_ + " steps");
		return null;
	}

	public void expand(AStarSearchNode searchNode) {
		Node node;
		Collection<Node> neighbours;

		node = searchNode.getState();
		neighbours = node.getNeighbors(model);
		if (neighbours == null) {
			return;
		}
		if (endPointNode != null) {
			EntityID commonId;

			commonId = endPointNode.getCommonConnection(node);
			if (commonId != null) {
				if (!neighbours.contains(endPointNode)) {
					neighbours.add(endPointNode);
				}
			}
		}
		for (Node neighbor : neighbours) {
			AStarSearchNode newNode;
			double pathCost, newCost;

			if ((neighbor == null)) {
				continue;
			}

			pathCost = searchNode.getPathCost();
			newCost = costFunction.getCost(node, neighbor);
			pathCost += newCost;
			newNode = new AStarSearchNode(pathCost, searchNode, neighbor);
			newNode.setHeuristic(newCost);
			if (history.containsKey(neighbor)) {
				AStarSearchNode oldNode;

				oldNode = history.get(neighbor);
				if (oldNode.getPathCost() > newNode.getPathCost()) {
					map.remove(oldNode);
					history.remove(neighbor);
					newNode.setHeuristic(oldNode.getHeuristic());
				} else {
					continue;
				}
			} else {
				newNode.setHeuristic(getHeuristicCost(neighbor));
			}
			map.add(newNode);
			history.put(neighbor, newNode);
		}
	}

	@Override
	public Collection<StandardEntity> findNeighbours(StandardEntity e) {
		Collection<StandardEntity> result = new ArrayList<StandardEntity>();
		if (e instanceof Area) {
			Area a = (Area) e;
			for (EntityID next : a.getNeighbours()) {
				result.add(model.getEntity(next));
			}
		}
		return result;
	}

	public double getHeuristicCost(Node node) {
		double cost;

		if (endNodes.size() == 1) {
			Node goal;

			goal = endNodes.iterator().next();
			cost = costFunction.getHeuristic(node, goal);
		} else if (endNodes.size() > 1) {
			double min;
			boolean first;

			min = 0;
			first = true;
			for (Node goalNode : endNodes) {
				double d;

				d = costFunction.getHeuristic(node, goalNode);
				if (first || d < min) {
					min = d;
					first = false;
				}
			}
			cost = min;
		} else {
			cost = 0;
		}
		return cost;
	}
}
