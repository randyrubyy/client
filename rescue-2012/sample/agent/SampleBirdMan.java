package sample.agent;

import static rescuecore2.misc.Handy.objectsToIDs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.BirdMan;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityConstants.Fieryness;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import sample.message.MessagePriority;
import sample.message.Type.BuildingIsBurningMessage;
import sample.message.Type.BuildingIsExploredMessage;
import sample.message.Type.ExtinguishFireTaskMessage;
import sample.message.Type.Message;
import sample.message.Type.MessageCount;
import sample.message.Type.MessageType;
import sample.object.Building.BurningBuildingComparator;
import sample.utilities.DistanceComparator;
import sample.utilities.DistanceSorter;
import sample.utilities.Path;
import sample.utilities.Search.PathType;

/**
 * SEU's Fire Brigade
 */
public class SampleBirdMan extends SampleAgent<BirdMan> {

	private static final String MAX_WATER_KEY = "fire.tank.maximum";
	private static final String MAX_DISTANCE_KEY = "fire.extinguish.max-distance";
	private static final String MAX_POWER_KEY = "fire.extinguish.max-sum";

	private int maxDistance;
	private int maxPower;
	private static final int MIN_TOTAL_AREA_TO_CALL_HELP = 150;

	// private ExtinguishFireTask extinguishFireTask;
	public SampleBirdMan() {
		super();
	}

	/**
	 * Returns the agents number as string.
	 * 
	 * @return Agents number
	 */
	@Override
	public String toString() {
		return "SEU Fire Brigade " + getNo();
	}

	/**
	 * Called after agent is connected to kernel
	 */
	@Override
	protected void postConnect() {
		super.postConnect();
		worldmodel.indexClass(StandardEntityURN.BUILDING,
				StandardEntityURN.REFUGE);
		maxDistance = config.getIntValue(MAX_DISTANCE_KEY);
		maxPower = config.getIntValue(MAX_POWER_KEY);
	}

	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.BIRD_MAN);
	}

	/**
	 * Searches the burning buildings from the world model
	 * 
	 * @param sort
	 *            If this boolean is set to true, returning list will contain
	 *            the burning buildings according to distance.
	 * @return The list of burning buildings in the world model
	 */
	public List<Building> getBurningBuildingsSorted() {
		return getBurningBuildings(true, false);
	}

	public List<Building> getBurningBuildings() {
		return getBurningBuildings(false, false);
	}

	public List<Building> getBurningBuildings(boolean noInferno) {
		return getBurningBuildings(false, noInferno);
	}

	public List<Building> getBurningBuildings(boolean sort, boolean noInferno) {
		Collection<Building> e = worldmodel.getEntitiesOfType(Building.class,
				StandardEntityURN.BUILDING);
		List<Building> result = new ArrayList<Building>();
		for (Building b : e) {
			if (b.isOnFire()) {
				if (isInMyPartition(b) || isInNeighborPartition(b)) {
					result.add(b);
				} else {
					if (doesNeedHelp(b)) {
						result.add(b);
					}
				}
			}
		}
		if (noInferno) {
			result = removeInfono(result);
		}
		// Sort by distance
		if (sort) {
			Collections.sort(result, new DistanceComparator(location(),
					worldmodel));
		}
		return result;
	}

	protected void processMessage(MessageCount message) {
		int counter = message.getCounter();
		MessageCount reply = new MessageCount(counter + 1);
		sendMessage(reply, MessagePriority.High);
	}

	@Override
	protected void thinkAndAct() {
	}

	public List<Building> getHeatingAndBurningBuildings(boolean sort) {
		Collection<Building> e = worldmodel.getEntitiesOfType(Building.class,
				StandardEntityURN.BUILDING);
		List<Building> result = new ArrayList<Building>();
		for (Building b : e) {
			// if (b.isOnFire()) {
			if (b.isFierynessDefined())
				if (b.getFieryness() > 0 && b.getFieryness() < 4) {
					if (isInMyPartition(b) || isInNeighborPartition(b)) {
						result.add(b);
					} else {
						if (doesNeedHelp(b)) {
							result.add(b);
						}
					}
				}
		}
		if (sort) {
			Collections.sort(result, new DistanceComparator(location(),
					worldmodel));
		}
		return result;
	}

	public Building nextBuildingToExtinguish() {
		List<Building> burningBuildings = getBurningBuildingsSorted();
		if (burningBuildings.isEmpty()) {
			return null;
		} else {
			return burningBuildings.get(0);
		}
	}

	public Building nextNearBuildingToExtinguish() {
		List<Building> burningSeenBuildings, extinguishableBuildings;
		BurningBuildingComparator comperator;
		Building building;

		burningSeenBuildings = getBurningBuildingsFromChangeSet();
		extinguishableBuildings = new ArrayList<Building>();
		for (Building burningBuilding : burningSeenBuildings) {
			if (canExtinguish(burningBuilding)) {
				extinguishableBuildings.add(burningBuilding);
			}
		}

		if (extinguishableBuildings.isEmpty()) {
			return null;
		} else {
			comperator = new BurningBuildingComparator(worldmodel,
					getTimeStep());
			Collections.sort(extinguishableBuildings, comperator);
			building = extinguishableBuildings.get(0);
			return building;
		}
	}

	@Override
	protected void processMessage(Message message) {
		if (message instanceof BuildingIsExploredMessage) {
			BuildingIsExploredMessage explorationMessage = (BuildingIsExploredMessage) message;
			processExplorationMessage(explorationMessage);
		}

		if (message instanceof BuildingIsBurningMessage) {
			BuildingIsBurningMessage buildingIsBurningMessage = (BuildingIsBurningMessage) message;
			processBuildingIsBurningMessage(buildingIsBurningMessage);
		}

		if (message instanceof ExtinguishFireTaskMessage) {
			ExtinguishFireTaskMessage extinguishFireTaskMessage = (ExtinguishFireTaskMessage) message;
			processExtinguishFireTaskMessage(extinguishFireTaskMessage);
		}

	}

	// 响应中心任务
	@SuppressWarnings("unused")
	protected void processExtinguishFireTaskMessage(
			ExtinguishFireTaskMessage message) {

		EntityID targetBuildingID = new EntityID(message.getBuildingId());
		Building targetBuilding = (Building) worldmodel
				.getEntity(targetBuildingID);

		int agentID = message.getAgentId();
		if (me().getID().getValue() == agentID) {
		}
	}

	// 是否具有救火能力
	// 自己距离房屋的距离大于30米
	// 自己的水量大于0
	public boolean canExtinguish(Building building) {
		int distance;
		boolean canExtinguish;

		if (building == null) {
			return false;
		}
		distance = worldmodel.getDistance(me(), building);
		canExtinguish = distance <= maxDistance && canSee(building.getID())
				&& me().getWater() > 0;
		return canExtinguish;
	}

	@Override
	protected boolean canRescue() {
		return false;
	}

	// Send Extinguish
	public void sendExtinguish(Building building) {
		if (me().getWater() < maxPower)
			sendExtinguish(timeStep, building.getID(), me().getWater());
		else
			sendExtinguish(timeStep, building.getID(), maxPower);
	}

	public boolean doesNeedHelp(Building building) {
		if (building.isFierynessDefined() && building.isTotalAreaDefined()) {

			if ((building.getFierynessEnum() == Fieryness.INFERNO || worldmodel
					.getTotalBurningArea(building) > MIN_TOTAL_AREA_TO_CALL_HELP)) {

				return true;
			}
		}
		return false;

	}

	@Override
	public List<MessageType> getMessagesToListen() {
		List<MessageType> types;

		types = new ArrayList<MessageType>();
		types.add(MessageType.BuildingIsBurningMessage);
		types.add(MessageType.BuildingIsExtinguishedMessage);
		types.add(MessageType.BuildingIsExploredMessage);
		return types;
	}

	public int getMaxDistance() {
		return maxDistance;
	}

	// /////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////
	@Override
	protected void act(int time, ChangeSet changed, Collection<Command> heard) {
		int maxWater;
		updateUnexploredBuildings(changed);
		// if (location() instanceof Refuge) {
		// if (me().getWater() == maxWater) {
		// List<EntityID> path1 = null;
		// path1 = randomWalk();
		// Logger.info("Moving randomly");
		// /sendMove(time, path1);
		// return;
		// }
		//
		// }

		for (Command next : heard) {
			Logger.debug("Heard " + next);
		}
		FireBrigade me = me();
		// Are we currently filling with water?
		if (me.isWaterDefined() && me.getWater() < maxPower
				&& location() instanceof Refuge) {
			Logger.info("Filling with water at " + location());
			sendRest(time);
			return;
		}
		// Are we out of water?
		if (me.isWaterDefined() && me.getWater() == 0) {
			// Head for a refuge
			// List<EntityID> r=new ArrayList<EntityID>();
			// for(Refuge r1:getAllRefuges())
			// r.add(r1.getID());
			// List<EntityID> path = search.getp(me().getPosition(), r);
			Path path = getPathToRefuge();
			if (path != null) {
				Logger.info("Moving to refuge");
				sendMove(path);
				return;
			} else {
				Logger.debug("Couldn't plan a path to a refuge.");
				List<EntityID> path1 = null;
				path1 = getRandomWalk();
				Logger.info("Moving randomly");
				sendMove(time, path1);
				return;
			}
		}
		// Find all buildings that are on fire
		Collection<Building> all = getObserveBuilding();
		// Can we extinguish any right now?
		for (Building next : all)
			if (next.isFierynessDefined())
				if (next.getFieryness() > 0 && next.getFieryness() < 4) {
					if (model.getDistance(getID(), next.getID()) <= maxDistance) {
						Logger.info("Extinguishing " + next);
						sendExtinguish(time, next.getID(), maxPower);
						// sendSpeak(time, 1, ("Extinguishing " +
						// next).getBytes());
						return;
					}
				}
		// Plan a path to a fire
		List<Building> all1 = getHeatingAndBurningBuildings(true);
		if (all1 != null) {
			for (Building next : all1) {
				// List<EntityID> path = planPathToFire(next);
				Path path2 = search.getPath(me(), next, PathType.Shortest);
				if (path2 != null) {
					Logger.info("Moving to target");
					path2.removeLastEntity();
					if (path2 != null)
						sendMove(path2);
					return;
				}
			}
		}

		// List<Building> b1 = new ArrayList<Building>();
		// // =new ArrayList<Building>;
		// System.out.println(timeStep + "   " + getID() + "    " + changed
		// + "    " + unexploredBuildings1);
		// for (EntityID e : unexploredBuildings1) {
		// StandardEntity s = world.getEntity(e);
		// b1.add((Building) s);
		// }
		// Path path = search.getPath(me(), b1, PathType.LowBlockRepair);
		// // (me().getPosition(),unexploredBuildings);
		// if (path != null) {
		// Logger.info("Searching buildings");
		// System.out.println("  time " + time + "    " + getID()
		// +"     unexporedBuilding     ");
		// sendMove(path);
		// return;
		// }

		List<EntityID> path1 = null;
		Logger.debug("Couldn't plan a path to a fire.");
		path1 = getRandomWalk();
		Logger.info("Moving randomly");
		sendMove(time, path1);
	}

	private Collection<EntityID> getBurningBuildings1() {
		Collection<StandardEntity> e = model
				.getEntitiesOfType(StandardEntityURN.BUILDING);
		List<Building> result = new ArrayList<Building>();
		for (StandardEntity next : e) {
			if (next instanceof Building) {
				Building b = (Building) next;
				if (b.isOnFire() && b.isFierynessDefined())
					if (b.getFieryness() < 4) {
						result.add(b);
					}
			}
		}
		// Sort by distance
		Collections.sort(result, new DistanceSorter(location(), model));
		return objectsToIDs(result);
	}

	private void updateUnexploredBuildings(ChangeSet changed) {
		for (EntityID next : changed.getChangedEntities()) {
			unexploredBuildings1.remove(next);
		}
	}
}
