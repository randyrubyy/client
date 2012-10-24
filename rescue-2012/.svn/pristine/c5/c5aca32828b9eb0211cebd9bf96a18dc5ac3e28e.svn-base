package sample.utilities.Search;

import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;
import sample.object.SampleWorldModel;
import sample.utilities.Search.Node;

public class BlockRepairCostFunction extends CostFunction<Node> {

	// TODO: replace with the real value.
	public static final double MAX_ROAD_LENGHT = 10000;

	SampleWorldModel world;

	public BlockRepairCostFunction(SampleWorldModel world) {
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
		double cost;

		cost = 0;
		if (road.isBlockadesDefined()) {
			for (EntityID id : road.getBlockades()) {
				Blockade block;

				block = (Blockade) world.getEntity(id);
				cost += block.getRepairCost();
			}
		}
		return cost;
	}

	@Override
	public double getHeuristic(Node node, Node goal) {
		return 0;
	}
}
