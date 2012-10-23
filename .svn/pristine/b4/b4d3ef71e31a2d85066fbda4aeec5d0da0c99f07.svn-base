package sample.object.Partition;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import sample.object.SampleWorldModel;
import sample.object.StandardPoint;

//分区内实体
public class EntityInPartition {
	// private static final Log LOG = LogFactory.getLog(EntityRegion.class);

	int no;
	List<Building> buildings;
	List<Road> roads;
	int row;
	int column;
	PartitionMap partitionGroup;
	int assignCost;

	public EntityInPartition(int no) {
		this(no, new ArrayList<Building>(), new ArrayList<Road>());
	}

	/**
	 * 建立一个分区
	 * 
	 * @param no
	 *            Region number.
	 * @param buildings
	 *            List of buildings.
	 * @param nodes
	 *            List of nodes.
	 */

	public EntityInPartition(int no, List<Building> buildings, List<Road> roads) {
		this.no = no;
		this.buildings = buildings;
		this.roads = roads;
	}

	public List<Building> getBuildings() {
		return buildings;
	}

	public void setBuildings(List<Building> buildings) {
		this.buildings = buildings;
	}

	public List<Road> getRoads() {
		return roads;
	}

	public void setRoads(List<Road> roads) {
		this.roads = roads;
	}

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	/**
	 * Adds a StandardEntity to this region.
	 * 
	 * @param entity
	 *            Given StandardEntity for the region.
	 */

	public void add(StandardEntity entity) {
		if (entity instanceof Building) {
			buildings.add((Building) entity);
		} else if (entity instanceof Road) {
			roads.add((Road) entity);
		} else {
			// LOG.warn("Unexpected entity type: " + entity);
		}
	}

	/**
	 * 分区内是否包含该实体
	 * 
	 * @param entity
	 *            Given StandardEntity.
	 * @return Returns true if the given entity is in this region.
	 */

	public boolean contains(StandardEntity entity) {
		if (entity instanceof Building) {
			return buildings.contains((Building) entity);
		} else if (entity instanceof Road) {
			return roads.contains((Road) entity);
		} else {
			// LOG.warn("Unexpected entity type at contains: " + entity);
			return false;
		}
	}

	public StandardPoint<Integer> getCenter(SampleWorldModel model) {
		int minX = 0;
		int minY = 0;
		int maxX = 0;
		int maxY = 0;
		boolean isFirst = true;
		List<Area> entities;

		entities = new ArrayList<Area>();
		entities.addAll(buildings);
		entities.addAll(roads);
		for (Area area : entities) {
			Pair<Integer, Integer> location = area.getLocation(model);
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

	public int calculateAssignCost(GroupingType type) {
		int cost;

		cost = 0;
		if (type == GroupingType.BuildingCount) {
			cost = buildings.size();
		} else if (type == GroupingType.RoadCount) {
			cost = roads.size();
		} else if (type == GroupingType.TotalBuildingArea) {
			for (Building building : buildings) {
				cost += building.getTotalArea();
			}
		}
		assignCost = cost;
		return cost;
	}

	@Override
	public String toString() {
		return "Partition #" + no;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public PartitionMap getRegionGroup() {
		return partitionGroup;
	}

	public void setRegionGroup(PartitionMap regionGroup) {
		this.partitionGroup = regionGroup;
	}

	public int getAssignCost() {
		return assignCost;
	}

}
