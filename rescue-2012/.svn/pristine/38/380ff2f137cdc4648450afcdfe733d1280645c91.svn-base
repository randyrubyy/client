package sample.utilities.Search;

import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;
import sample.object.SampleWorldModel;
import sample.utilities.Search.Node;

public class BlockRatioCostFunction extends CostFunction<Node> {

	// TODO: replace with the real value.
	public static final double MAX_ROAD_LENGHT = 10000;

	SampleWorldModel world;

	public BlockRatioCostFunction(SampleWorldModel world) {
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

			road = (Road) entity;
			cost = getCost(road);
		} else {
			cost = 0;
		}
		return cost;
	}

	public double getCost(Road road) {
		// TODO:does not work correctly
		if (!road.isBlockadesDefined()) {
			return 0;
		}
		return road.getBlockades().size();
		/*
		 * roadWidth = road.getWidth(); maxCost = getMaxCost(); block =
		 * road.getBlock(); cost = block / roadWidth * maxCost; return cost;
		 */
	}

	@Override
	public double getHeuristic(Node node, Node goal) {
		return 0;
	}
}
