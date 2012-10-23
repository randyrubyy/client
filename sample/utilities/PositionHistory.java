package sample.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;
import sample.object.SampleWorldModel;

public class PositionHistory {
	// public static final Logger LOG = Logger.getLogger(PositionHistory.class);

	public static final int DISTANCE_EQUALITY_TRESHOLD = 1000;
	// 保存周期数
	public static final int DELETE_AFTER = 30;

	int latestX;
	int latestY;
	int currentX;
	int currentY;

	Human agent;
	Path desiredPath;
	SampleWorldModel world;

	StandardEntity desiredLocation;

	public StandardEntity getDesiredLocation() {
		return desiredLocation;
	}

	StandardEntity currentLocation;

	// Get Current Location
	public StandardEntity getCurrentLocation() {
		return currentLocation;
	}

	StandardEntity latestLocation;

	protected Map<StandardEntity, Integer> unreachableLocationTimes;

	public PositionHistory() {
		this(null, null);
	}

	public PositionHistory(Human agent, SampleWorldModel world) {
		this.agent = agent;
		this.world = world;
		unreachableLocationTimes = new HashMap<StandardEntity, Integer>();
	}

	public void process(int timestep) {
		if (agent == null || world == null) {
			return;
		}
		updateCurrentValues();
		if (isInDesiredLocation()) {
			desiredLocation = null;
		} else {
			if (isInSameLocation()) {
				// LOG.warn("Found unreachable location: " + desiredLocation_);
				unreachableLocationTimes.put(desiredLocation, timestep);
			}
		}
		updateLatestValues();
		removeOldLocations(timestep);
	}

	public boolean hasMoveFailed() {
		if (agent == null || world == null) {
			return false;
		}
		if (isInDesiredLocation()) {
			return false;
		} else {
			if (isInSameLocation()) {
				return true;
			}
		}
		return false;
	}

	protected void updateCurrentValues() {
		currentLocation = agent.getPosition(world);
		if (agent.isXDefined() && agent.isYDefined()) {
			currentX = agent.getX();
			currentY = agent.getY();
		}
	}

	protected void updateLatestValues() {
		latestLocation = currentLocation;
		latestX = currentX;
		latestY = currentY;
	}

	// 判断是否移动
	public boolean isInSameLocation() {
		if (currentLocation.equals(latestLocation)) {
			int xDiff, yDiff, distance;

			xDiff = Math.abs(currentX - latestX);
			yDiff = Math.abs(currentY - latestY);
			distance = xDiff + yDiff;
			// LOG.debug("In same location with distance: " + distance);
			return distance < DISTANCE_EQUALITY_TRESHOLD;
		} else {
			return false;
		}
	}

	public boolean isInDesiredLocation() {
		if (desiredLocation == null) {
			return true;
		}
		return currentLocation.equals(desiredLocation);
	}

	public void setDesiredLocation(StandardEntity location) {
		desiredLocation = location;
	}

	public void setDesiredLocation(EntityID id) {
		StandardEntity entity;

		entity = world.getEntity(id);
		setDesiredLocation(entity);
	}

	public Set<StandardEntity> getUnreachableLocations() {
		Set<StandardEntity> locations;

		locations = unreachableLocationTimes.keySet();
		return locations;
	}

	public void addUnreachableLocations(
			Collection<? extends StandardEntity> locations, int time) {
		// LOG.warn("Found unreachable locations: " + locations);
		for (StandardEntity location : locations) {
			unreachableLocationTimes.put(location, time);
		}
	}

	@Override
	public String toString() {
		return "Position History - Unreachable: "
				+ unreachableLocationTimes.toString();
	}

	private void removeOldLocations(int time) {
		List<StandardEntity> list;

		list = new ArrayList<StandardEntity>();
		list.addAll(unreachableLocationTimes.keySet());
		for (StandardEntity entity : list) {
			int t;

			t = unreachableLocationTimes.get(entity);
			if (t + DELETE_AFTER < time) {
				unreachableLocationTimes.remove(entity);
			}
		}
	}

	public void setDesiredPath(Path path) {
		desiredPath = path;
	}
}
