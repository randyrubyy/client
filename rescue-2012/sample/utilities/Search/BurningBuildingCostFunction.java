package sample.utilities.Search;

import java.util.HashSet;
import java.util.Set;

import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityConstants.Fieryness;
import rescuecore2.worldmodel.EntityID;
import sample.object.SampleWorldModel;
import sample.utilities.Search.Node;

public class BurningBuildingCostFunction extends CostFunction<Node> {

	// private static final Logger LOG = Logger
	// .getLogger(BurningBuildingCostFunction.class);
	// TODO: replace with the real value.

	SampleWorldModel world;

	public BurningBuildingCostFunction(SampleWorldModel world) {
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
		if (entity instanceof Building) {
			Building building;
			building = (Building) entity;

			cost = getCost(building);
		} else {
			cost = 0;
		}
		return cost;
	}

	public double getCost(Building building) {
		double cost;

		if (building.isFierynessDefined()) {
			Fieryness fieryness;

			fieryness = building.getFierynessEnum();
			if (Building.BURNING.contains(fieryness)) {
				cost = getMaxCost();
			} else {
				cost = 0;
			}
		} else {
			cost = 0;
		}
		return cost;
	}

	@Override
	public double getHeuristic(Node node, Node goal) {
		Set<EntityID> ids;

		ids = new HashSet<EntityID>();
		ids.addAll(node.getConnectedEntityIds());
		ids.addAll(goal.getConnectedEntityIds());
		for (EntityID id : ids) {
			StandardEntity entity;

			entity = world.getEntity(id);
			if (entity instanceof Building) {
				return getCost((Building) entity);
			}
		}
		return 0;
	}
}
