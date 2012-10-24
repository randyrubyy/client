package sample.utilities.Search;

import java.awt.Point;

import sample.object.SampleWorldModel;
import sample.utilities.Search.Node;

public class LengthCostFunction extends CostFunction<Node> {

	// private final Logger LOG = Logger.getLogger(LengthCostFunction.class);
	// TODO: replace with the real value.
	public static final double MAX_ROAD_LENGHT = 10000;

	SampleWorldModel world;

	public LengthCostFunction(SampleWorldModel world) {
		super();
		this.world = world;
	}

	@Override
	public double getCost(Node current, Node next) {
		Point p1, p2;
		double distance;
		double cost, maxCost, maxLength;

		p1 = current.getPosition();
		p2 = next.getPosition();
		distance = world.distance(p1, p2);

		maxCost = getMaxCost();
		maxLength = MAX_ROAD_LENGHT;
		cost = distance / maxLength * maxCost;
		// LOG.trace("Length cost:" + cost);
		return cost;
	}

	@Override
	public double getHeuristic(Node node, Node goal) {
		double cost;

		cost = getCost(node, goal);
		return cost;
	}
}
