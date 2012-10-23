package sample.object.Road;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;
import sample.object.Line;
import sample.object.SampleWorldModel;

public class RoadModel {
	public static final int DEFAULT_MAIN_ROAD_LINE_COUNT = 3;
	public static final int MINIMUM_ROAD_COUNT = 3;

	List<RoadGroup> mainRoads;
	SampleWorldModel model;

	int mainRoadLineCount;

	/**
	 * 
	 * @param model
	 */

	public RoadModel(SampleWorldModel model) {
		// logger.setLevel(Level.ALL);
		this.model = model;

		this.mainRoadLineCount = getMaximumRoadLines();
		while (mainRoadLineCount > 1) {
			this.mainRoads = getMainRoadGroups();
			if (mainRoads.size() < MINIMUM_ROAD_COUNT) {
				mainRoadLineCount--;
			} else {
				break;
			}
		}
		// logger.log(Level.INFO, mainRoads.size() + " main roads are found.");

	}

	/**
	 * Checks whether given road is main road or not.
	 * 
	 * @param road
	 *            Given Road
	 * @return Rturns true if the road is main road.
	 */

	protected boolean isMainRoad(Road road) {
		// TODO:3000 might be wrong
		return road.getShape().getBounds().getWidth() >= 3000;
		// return road.getLinesToHead() + road.getLinesToTail() >=
		// mainRoadLineCount;
	}

	/**
	 * 
	 * @return
	 */

	public List<Line> getMainRoadLines() {
		List<Line> lines = new ArrayList<Line>();
		for (RoadGroup roadGroup : mainRoads) {
			lines.add(getRoadGroupLine(roadGroup));
		}
		return lines;
	}

	/**
	 * 
	 * @return
	 */

	public List<Line> getMainRoadGroupLines() {
		List<RoadGroup> roadGroups = mainRoads;
		List<Line> lines = new ArrayList<Line>();
		for (RoadGroup roadGroup : roadGroups) {
			lines.add(getRoadGroupLine(roadGroup));
		}
		return lines;
	}

	/**
	 * Returns the lise of main road groups.
	 * 
	 * @return
	 */

	protected List<RoadGroup> getMainRoadGroups() {
		// logger.log(Level.FINER, "BEGIN: Detecting Main Roads.");
		List<RoadGroup> groups = new ArrayList<RoadGroup>();
		for (Road road : getRoads()) {
			// logger.log(Level.FINEST, "Detected some road part.");
			if (isMainRoad(road)) {
				// logger.log(Level.FINEST, "Detected main road part.");
				if (!RoadGroup.contains(groups, road)) {
					RoadGroup group = getRoadGroup(road);
					groups.add(group);
					// logger.log(Level.FINEST,group.toString() );
				}
			}
		}
		// logger.log(Level.FINER, "END: Detecting Main Roads.");
		return groups;
	}

	public Line getRoadLine(Road road) {
		List<EntityID> ids;

		ids = road.getNeighbours();
		Area head = (Area) model.getEntity(ids.get(0));
		Area tail = (Area) model.getEntity(ids.get(1));
		return model.getLine(head, tail);
	}

	public Line getRoadGroupLine(RoadGroup roadGroup) {
		Area head = (Area) model.getEntity(roadGroup.getHead());
		Area tail = (Area) model.getEntity(roadGroup.getTail());
		return model.getLine(head, tail);
	}

	public RoadGroup getRoadGroup(Road road) {
		List<Road> roadList = new ArrayList<Road>();
		roadList.add(road);
		RoadGroup group = new RoadGroup(roadList);
		extend(group);
		return group;
	}

	private void extend(RoadGroup roadGroup) {
		boolean extended = true;
		while (extended) {
			extended = extendHead(roadGroup);
		}
		extended = true;
		while (extended) {
			extended = extendTail(roadGroup);
		}
	}

	private boolean extendTail(RoadGroup roadGroup) {
		List<Road> roadList = roadGroup.roads;
		Road road = roadList.get(roadList.size() - 1);
		Road previus = road;
		if (roadList.size() > 1) {
			previus = roadList.get(roadList.size() - 2);
		}
		Area tail = (Area) model.getEntity(roadGroup.getTail());
		List<Road> tailRoads = getRoads(tail);
		Line roadLine = getRoadLine(road);
		Road newRoad = null;
		for (Road tailRoad : tailRoads) {
			if (!tailRoad.equals(road) && !tailRoad.equals(previus)) {
				Line line = getRoadLine(tailRoad);
				if (line.hasSimilarM(roadLine)) {
					newRoad = tailRoad;
					break;
				}
			}
		}
		if (newRoad == null) {
			return false;
		} else {
			roadList.add(newRoad);
			return true;
		}
	}

	private boolean extendHead(RoadGroup roadGroup) {
		List<Road> roads = roadGroup.roads;
		Road road = roads.get(0);
		Road previus = road;
		if (roads.size() > 1) {
			previus = roads.get(1);
		}
		Area head = (Area) model.getEntity(roadGroup.getHead());
		List<Road> headRoads = getRoads(head);
		Line roadLine = getRoadLine(road);
		Road newRoad = null;
		for (Road headRoad : headRoads) {
			if (!headRoad.equals(road) && !headRoad.equals(previus)) {
				Line line = getRoadLine(headRoad);
				if (line.hasSimilarM(roadLine)) {
					newRoad = headRoad;
					break;
				}
			}
		}
		if (newRoad == null) {
			return false;
		} else {
			roads.add(0, newRoad);
			return true;
		}
	}

	public List<Road> getRoads(Area node) {
		List<EntityID> ids = node.getNeighbours();
		List<Road> roadList = new ArrayList<Road>();
		for (EntityID id : ids) {
			StandardEntity entity = model.getEntity(id);
			if (entity instanceof Road) {
				roadList.add((Road) entity);
			}
		}
		return roadList;
	}

	public List<Road> getRoads(List<Area> nodes) {
		List<Road> roads = new ArrayList<Road>();
		for (Area node : nodes) {
			roads.addAll(getRoads(node));
		}
		return roads;
	}

	public List<Road> getConnectedRoads(Road road) {
		List<Area> nodes = new ArrayList<Area>();
		nodes.add((Area) model.getEntity(road.getNeighbours().get(0)));
		nodes.add((Area) model.getEntity(road.getNeighbours().get(1)));
		return getRoads(nodes);
	}

	public List<Road> getRoads() {
		return model.getEntitiesOfType(Road.class, StandardEntityURN.ROAD);
	}

	public int getMainRoadLineCount() {
		return mainRoadLineCount;
	}

	public void setMainRoadLineCount(int mainRoadLineCount) {
		this.mainRoadLineCount = mainRoadLineCount;
	}

	public List<RoadGroup> getMainRoads() {
		return mainRoads;
	}

	public void setMainRoads(List<RoadGroup> mainRoads) {
		this.mainRoads = mainRoads;
	}

	public int getMainRoadNo(Road road) {
		int i = 0;
		for (RoadGroup roadGroup : mainRoads) {
			if (roadGroup.contains(road)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public int getMaximumRoadLines() {
		int max = 0;
		// TODO:should be fixed for the new simulator
		/*
		 * List<Road> roads = model.getEntitiesOfType(Road.class,
		 * StandardEntityURN.ROAD); for(Road road : roads){ int nLines =
		 * road.getLinesToHead() + road.getLinesToTail(); max = Math.max(max,
		 * nLines); }
		 */
		return max;
	}
}
