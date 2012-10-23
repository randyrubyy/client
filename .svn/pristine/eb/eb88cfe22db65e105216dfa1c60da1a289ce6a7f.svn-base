package sample.object.Partition;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Building;
import rescuecore2.worldmodel.EntityID;
import sample.object.SampleWorldModel;
import sample.object.StandardPoint;

public class FirePartition {
	List<Building> buildings;
	List<EntityID> assignedAgents;
	Building targetBuilding;

	public FirePartition(List<Building> buildings, Building centerBuilding) {
		this.buildings = buildings;
		assignedAgents = new ArrayList<EntityID>();
		targetBuilding = centerBuilding;
	}

	public Building getTagetBuilding() {
		return targetBuilding;
	}

	public List<Building> getBuildings() {
		return buildings;
	}

	public void setBuildings(List<Building> buildings) {
		this.buildings = buildings;
	}

	public void assignAgent(EntityID agent) {
		assignedAgents.add(agent);
	}

	public List<EntityID> getAssignedAgents() {
		return assignedAgents;
	}

	/**
	 * Adds a Building to this fire region.
	 * 
	 * @param entity
	 *            Given StandardEntity for the region.
	 */

	public void add(Building entity) {
		buildings.add(entity);
	}

	/**
	 * Checks whether given entity is in this region or not.
	 * 
	 * @param entity
	 *            Given Building.
	 * @return Returns true if the given entity is in this region.
	 */

	public boolean contains(Building entity) {
		return buildings.contains(entity);
	}

	public StandardPoint<Integer> getCenter(SampleWorldModel model) {
		int minX = 0;
		int minY = 0;
		int maxX = 0;
		int maxY = 0;
		boolean isFirst = true;
		for (Building building : buildings) {
			Pair<Integer, Integer> location = building.getLocation(model);
			int x = location.first();
			int y = location.second();
			if (isFirst) {
				minX = x;
				minY = y;
				maxX = x;
				maxY = y;
			} else {
				minX = Math.min(x, minX);
				maxX = Math.max(x, maxX);
				minY = Math.min(y, minY);
				maxY = Math.max(y, maxY);
			}
		}
		int x = (minX + maxX) / 2;
		int y = (minY + maxY) / 2;
		StandardPoint<Integer> center = new StandardPoint<Integer>(x, y);
		return center;
	}

	public int getSize() {
		int sum = 0;
		for (Building building : buildings) {
			int size = building.getTotalArea();
			sum += size;
		}
		return sum;
	}

}
