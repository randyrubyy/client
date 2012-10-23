package sample.utilities.Search;

import java.awt.Point;

import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;
import sample.object.SampleWorldModel;
import sample.object.Road.RoadUtilities;
import sample.utilities.Search.Node;

public class AdvancedBlockCostFunction extends CostFunction<Node> {
	// private static final Logger LOG = Logger
	// .getLogger(AdvancedBlockCostFunction.class);
	// TODO: replace with the real value.

	public static final double MAX_ROAD_LENGHT = 10000;

	SampleWorldModel world;

	public AdvancedBlockCostFunction(SampleWorldModel world) {
		super();
		this.world = world;
	}

	@Override
	public double getCost(Node current, Node next) {
		double cost;
		StandardEntity entity;
		EntityID id;

		id = current.getCommonConnection(next);
		entity = world.getEntity(id);
		if (entity instanceof Road) {
			Road road;

			// Point p1, p2;
			// Area area;
			road = (Road) entity;

			// area = new Area(road.getShape());
			// LOG.warn("P1: " + area.contains(p1));
			// LOG.warn("P2: " + area.contains(p2));

			cost = getCost(road, current.getPosition(), next.getPosition());
		} else {
			cost = 0;
		}
		return cost;
	}

	public double getCost(Road road, Point p1, Point p2) {
		boolean blocked;
		double cost;

		if (road.isBlockadesDefined()) {
			blocked = RoadUtilities.isBlocked(road, p1, p2, world);
			cost = blocked ? Double.POSITIVE_INFINITY : 0;
		} else {
			cost = getMaxCost();
		}
		return cost;
	}

	@Override
	public double getHeuristic(Node node, Node goal) {
		return 0;
	}
}
