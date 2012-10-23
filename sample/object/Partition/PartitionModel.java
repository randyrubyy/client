package sample.object.Partition;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;
import sample.object.Direction;
import sample.object.Line;
import sample.object.SampleWorldModel;
import sample.utilities.PositionLocate;

public class PartitionModel {

	public static final int DEFAULT_LINE_COUNT = 1;
	public static final int DEFAULT_REGION_COLUMN_COUNT = 6;
	public static final int DEFAULT_TEAM = 6;

	SampleWorldModel world;
	EntityInPartition[][] entityInPartition;
	List<PartitionMap> partitionGroups;
	Map<PartitionMap, EntityID> partitionAssignements;
	GroupingType groupingType;

	public PartitionModel(SampleWorldModel model, GroupingType type) {
		int teamSize;

		this.world = model;
		groupingType = type;
		teamSize = 25;
		this.entityInPartition = calculatePartitions(teamSize);
		partitionAssignements = new HashMap<PartitionMap, EntityID>();
	}

	/**
	 * 计算分区数量
	 * 
	 * @return Returns the list of regions found.
	 */

	public int getExpectedRegionCount(int teamSize) {
		int n;

		n = (int) Math.ceil(Math.sqrt(teamSize) + 1);

		return n;
	}

	public EntityInPartition[][] calculatePartitions(int teamSize) {
		int n;
		EntityInPartition[][] regions;

		n = getExpectedRegionCount(teamSize);
		regions = calculateRegions(n, n);

		return regions;
	}

	public List<PartitionMap> calculatePartitionsMap(int teamSize,
			GroupingType type) {
		List<PartitionMap> groups;
		int c, totalCost, averageCost;

		groups = new ArrayList<PartitionMap>();
		for (int i = 0; i < teamSize; i++) {
			groups.add(new PartitionMap(i));
		}
		c = 0;
		totalCost = 0;
		for (EntityInPartition[] regionRow : entityInPartition) {
			for (EntityInPartition region : regionRow) {
				int cost;
				cost = region.calculateAssignCost(type);
				totalCost += cost;
			}
		}
		averageCost = totalCost / teamSize;
		totalCost = 0;
		for (EntityInPartition[] regionRow : entityInPartition) {
			for (EntityInPartition region : regionRow) {
				PartitionMap group;
				group = groups.get(c);
				group.addRegion(region);
				totalCost += region.getAssignCost();
				if (totalCost >= averageCost * (c + 1)) {
					if (c < teamSize - 1) {
						c++;
					}
				}
			}
		}
		return groups;
	}

	// 将World Model内实体对应至区域内
	public EntityInPartition[][] calculateRegions(int row, int column) {
		EntityInPartition[][] regions;
		Collection<StandardEntity> entities;
		int n;

		regions = new EntityInPartition[row][column];
		n = 0;
		for (int r = 0; r < row; r++) {
			for (int c = 0; c < column; c++) {
				EntityInPartition region;

				region = new EntityInPartition(n);
				region.setRow(r);
				region.setColumn(c);
				regions[r][c] = region;
				n++;
			}
		}
		entities = world
				.getEntitiesOfType(StandardEntityURN.BUILDING,
						StandardEntityURN.ROAD, StandardEntityURN.REFUGE,
						StandardEntityURN.AMBULANCE_CENTRE,
						StandardEntityURN.POLICE_OFFICE,
						StandardEntityURN.FIRE_STATION);
		for (StandardEntity entity : entities) {
			Point point;
			int c, r;

			point = PositionLocate.getPosition(entity, world);
			c = getColumn((int) world.getBounds().getMinX(), (int) world
					.getBounds().getWidth(), column, point.x);
			r = getColumn((int) world.getBounds().getMinY(), (int) world
					.getBounds().getHeight(), row, point.y);
			if (r >= 0 && c >= 0 && r < regions.length && c < regions[r].length) {
				regions[r][c].add(entity);
			}
		}
		return regions;
	}

	protected int getColumn(int left, int width, int columnCount, int value) {
		return (value - left) * columnCount / width;
	}

	/**
	 * Retunrs the list of regions
	 * 
	 * @return
	 */

	public EntityInPartition[][] getRegions() {
		return entityInPartition;
	}

	/**
	 * Sets the regions
	 * 
	 * @param regions
	 */

	public void setRegions(EntityInPartition[][] regions) {
		this.entityInPartition = regions;
	}

	/**
	 * This function is used to get diagonal lines of the map.
	 * 
	 * @return the list of two main diagonal lines of the map.
	 */
	protected List<Line> getDiagonalLines() {
		Rectangle2D bounds = world.getBounds();
		List<Line> lines = new ArrayList<Line>();
		lines.add(new Line(bounds.getMinX(), bounds.getMinY(),
				bounds.getMaxX(), bounds.getMaxY()));
		lines.add(new Line(bounds.getMinX(), bounds.getMaxY(),
				bounds.getMaxX(), bounds.getMinY()));
		return lines;
	}

	protected List<Line> getGridLines(int n) {
		Rectangle2D bounds = world.getBounds();
		List<Line> lines = new ArrayList<Line>();
		double h = bounds.getHeight();
		double w = bounds.getWidth();
		double dx = w / (n + 1);
		double dy = h / (n + 1);
		for (int i = 0; i < n; i++) {
			lines.add(new Line(bounds.getMinX(), bounds.getMinY() + (i + 1)
					* dy, bounds.getMaxX(), bounds.getMinY() + (i + 1) * dy));
			lines.add(new Line(bounds.getMinX() + (i + 1) * dx, bounds
					.getMinY(), bounds.getMinX() + (i + 1) * dx, bounds
					.getMaxY()));
		}
		return lines;
	}

	/**
	 * 
	 * @param <T>
	 * @param entities
	 * @param lines
	 * @return
	 */

	protected <T extends StandardEntity> List<String> getLineStrings(
			List<T> entities, List<Line> lines) {
		List<String> strings = new ArrayList<String>();
		for (StandardEntity entity : entities) {
			strings.add(getLineString(entity, lines));
		}
		return strings;
	}

	/**
	 * 
	 * @param entity
	 * @param lines
	 * @return
	 */

	protected String getLineString(StandardEntity entity, List<Line> lines) {
		String s = "";
		int x, y;
		if (entity instanceof Area) {
			Area building = (Area) entity;
			x = building.getX();
			y = building.getY();
		} else {
			return "";
		}
		for (Line line : lines) {
			if (line.isOnLeft(x, y)) {
				s += Direction.West.toChar();
			} else {
				s += Direction.East.toChar();
			}
		}
		return s;
	}

	/**
	 * 
	 * @return
	 */

	protected int getTotalBuildingArea() {
		List<Building> buildings = world.getEntitiesOfType(Building.class,
				StandardEntityURN.BUILDING);
		int totalArea = 0;
		for (Building building : buildings) {
			totalArea += building.getTotalArea();
		}
		return totalArea;
	}

	/**
	 * 
	 * @return
	 */

	protected List<Building> getBuildings() {
		return world.getEntitiesOfType(Building.class,
				StandardEntityURN.BUILDING);
	}

	public EntityInPartition getRegion(StandardEntity entity) {

		for (EntityInPartition[] regionRow : entityInPartition) {
			for (EntityInPartition region : regionRow) {
				if (region.contains(entity)) {
					return region;
				}
			}
		}
		return null;
	}

	public PartitionMap getRegionGroup(StandardEntity entity) {
		EntityInPartition region;
		PartitionMap group;

		region = getRegion(entity);
		if (region == null) {
			return null;
		}
		group = region.getRegionGroup();
		return group;
	}

	/**
	 * 根据实体获得该实体在第几个分区
	 * 
	 * @param entity
	 *            Given StandardEntity
	 * @return Returns corresponding region number.
	 */

	public int getRegionNo(StandardEntity entity) {
		EntityInPartition region = getRegion(entity);
		if (region == null) {
			return -1;
		} else {
			return region.getNo();
		}
	}

	public EntityInPartition getSmallest(List<EntityInPartition> regions,
			int limit) {
		List<EntityInPartition> temp = new ArrayList<EntityInPartition>();
		for (int i = 0; i < limit; i++) {
			if (i < regions.size()) {
				temp.add(regions.get(i));
			}
		}
		Comparator<EntityInPartition> comparator = Collections
				.reverseOrder(new RegionSizeComparator());
		Collections.sort(temp, comparator);
		return temp.get(0);
	}

	public void assignPartitionsToTeam() {
		assignPartitionsToEntities(world.getTeam());
	}

	public void assignPartitionsToEntities(List<EntityID> entities) {
		int i = 0;
		if (partitionGroups == null) {
			partitionGroups = calculatePartitionsMap(entities.size(),
					groupingType);
		}

		for (PartitionMap group : partitionGroups) {
			if (i < entities.size()) {
				EntityID id = entities.get(i % entities.size());
				partitionAssignements.put(group, id);
				i++;
			}
		}
	}

	public PartitionMap getAssignedPartitions(EntityID agentId) {
		for (PartitionMap regionGroup : partitionAssignements.keySet()) {
			EntityID id = partitionAssignements.get(regionGroup);
			if (id.equals(agentId)) {
				return regionGroup;
			}
		}
		return null;
	}

	public List<EntityInPartition> getNeighbors(EntityInPartition region,
			int level) {
		int column, row, minC, minR, maxC, maxR;
		List<EntityInPartition> regions;

		column = region.getColumn();
		row = region.getRow();
		minC = Math.max(0, column - level);
		minR = Math.max(0, row - level);
		maxC = Math.min(entityInPartition[0].length - 1, column + level);
		maxR = Math.min(entityInPartition.length - 1, row + level);
		regions = new ArrayList<EntityInPartition>();
		for (int r = minR; r <= maxR; r++) {
			for (int c = minC; c <= maxC; c++) {
				if (r != row || c != column) {
					regions.add(entityInPartition[r][c]);
				}
			}
		}
		return regions;
	}

	public Set<PartitionMap> getNeighbors(PartitionMap group, int level) {
		Set<PartitionMap> groups;

		groups = new HashSet<PartitionMap>();
		for (EntityInPartition region : group.getPartitions()) {
			List<EntityInPartition> neighborRegions;

			neighborRegions = getNeighbors(region, level);
			for (EntityInPartition neighborRegion : neighborRegions) {
				PartitionMap neighborGroup;

				neighborGroup = neighborRegion.getRegionGroup();
				if (!neighborGroup.equals(group)) {
					groups.add(neighborGroup);
				}
			}
		}
		return groups;
	}

}
