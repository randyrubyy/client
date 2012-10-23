package sample.utilities.Search;

import java.util.Collection;

import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;
import sample.object.SampleWorldModel;
import sample.utilities.Search.Node;

public class StuckAgentCostFunction extends CostFunction<Node> {
	// private final Logger LOG =
	// Logger.getLogger(StuckAgentCostFunction.class);

	SampleWorldModel world;

	public StuckAgentCostFunction(SampleWorldModel model) {
		world = model;
	}

	@Override
	public double getCost(Node current, Node next) {
		Collection<StandardEntity> humans = world.getEntitiesOfType(
				StandardEntityURN.AMBULANCE_TEAM,
				StandardEntityURN.POLICE_FORCE, StandardEntityURN.FIRE_BRIGADE,
				StandardEntityURN.CIVILIAN);
		double cost;
		EntityID commonAreaId;

		commonAreaId = current.getCommonConnection(next);
		cost = 0;
		for (StandardEntity entity : humans) {
			Human human = (Human) entity;
			EntityID positionId;

			positionId = human.getPosition();
			if (positionId.equals(commonAreaId)) {
				cost += getMaxCost();
			}
		}
		// LOG.trace("Stuck cost" + cost);
		return cost;
	}

	@Override
	public double getHeuristic(Node node, Node goal) {
		return 0;
	}

}
