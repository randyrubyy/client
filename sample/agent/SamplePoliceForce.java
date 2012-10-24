package sample.agent;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import sample.agent.SampleAgent;
import sample.message.MessagePriority;
import sample.message.Type.AgentIsStuckMessage;
import sample.message.Type.AssignFiredBuildingMessage;
import sample.message.Type.AssignRefugeMessage;
import sample.message.Type.BuildingIsExploredMessage;
import sample.message.Type.ClearPathIsNeededMessage;
import sample.message.Type.Message;
import sample.message.Type.MessageCount;
import sample.message.Type.MessageType;
import sample.message.Type.RoadIsClearedMessage;
import sample.object.SampleWorldModel;
import sample.object.Road.MultiBlockDistanceComparator;
import sample.object.Road.PathBlockState;
import sample.object.Road.RoadUtilities;
import sample.utilities.DistanceComparator;
import sample.utilities.Path;
import sample.utilities.DistanceUtilities;
import sample.utilities.PositionLocate;
import sample.utilities.Search.PathType;

/**
 * SEU's Police Office
 */
public class SamplePoliceForce extends SampleAgent<PoliceForce> {

	// 清障距离
	private static final String DISTANCE_KEY = "clear.repair.distance";

	// private ClearPathTask clearPathTask;

	private int distance;
	public boolean isDone = false;
	public boolean taskDone = false;
	public boolean isSelected = false;
	public boolean existPO = false;
	public int numOfRefuge;
	public int numOfPF;
	public Refuge targetRefuge;
	public EntityID targetBuilding = null;
	public ArrayList<StandardEntity> PF = new ArrayList<StandardEntity>();
	public ArrayList<StandardEntity> refuge = new ArrayList<StandardEntity>();
	public ArrayList<Integer> refugeSort = new ArrayList<Integer>();
	public ArrayList<Integer> PFSort = new ArrayList<Integer>();
	public HashMap<Integer, Integer> clearedBlockade = new HashMap<Integer, Integer>();
	public HashMap<Integer, Integer> numOfClear = new HashMap<Integer, Integer>();
	public ArrayList<EntityID> PFTarget = new ArrayList<EntityID>();

	public SamplePoliceForce() {
		super();

	}

	@Override
	public String toString() {
		return "SEU Police Force # " + getNo();
	}

	@Override
	protected void postConnect() {
		super.postConnect();
		distance = config.getIntValue(DISTANCE_KEY);

	}

	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.POLICE_FORCE);
	}

	public List<Road> getBlockedRoads() {
		List<Road> targets = new ArrayList<Road>();
		for (StandardEntity next : worldmodel
				.getEntitiesOfType(StandardEntityURN.ROAD)) {
			Road r = (Road) next;
			if (r.isBlockadesDefined() && !r.getBlockades().isEmpty()) {
				targets.add(r);
			}
		}
		Collections.sort(targets, new DistanceComparator(location(), worldmodel));
		return targets;
	}

	public List<Blockade> getBlockades() {
		List<Road> roads;
		List<Blockade> blockades;

		roads = worldmodel.getEntitiesOfType(Road.class, StandardEntityURN.ROAD);
		blockades = new ArrayList<Blockade>();
		for (Road road : roads) {
			if (road.isBlockadesDefined()) {
				List<EntityID> blockadeIds;

				blockadeIds = road.getBlockades();
				for (EntityID id : blockadeIds) {
					Blockade blockade;

					blockade = worldmodel.getEntity(id, Blockade.class);
					blockades.add(blockade);
				}
			}
		}
		return blockades;
	}

	public boolean isBuildingEntrance(EntityID entityId) {
		Area area = (Area) worldmodel.getEntity(entityId);
		List<EntityID> neighbors = area.getNeighbours();
		for (EntityID next : neighbors) {
			StandardEntity entity = worldmodel.getEntity(next);
			if (entity instanceof Building) {

				return true;
			}
		}
		return false;
	}

	public Area getAreaToClear() {
		Area area = (Area) location();
		List<EntityID> neighbors = area.getNeighbours();
		for (EntityID next : neighbors) {
			StandardEntity entity = model.getEntity(next);
			if (entity instanceof Road) {
				Area neighborRoad = (Area) entity;
				PathBlockState pbs;

				pbs = RoadUtilities.getPathBlockState(getMeAsHuman(),
						neighborRoad, worldmodel);
				if (pbs == PathBlockState.FirstPartIsBlocked) {
					return area;
				} else if (pbs == PathBlockState.SecondPartIsBlocked) {
					return neighborRoad;
				}
			}
		}
		return null;
	}

	public Blockade getTargetBlockade(Area area, int maxDistance) {
		if (area != null)
			if (!area.isBlockadesDefined()) {
				return null;
			}
		if (area == null)
			return null;
		List<EntityID> ids = area.getBlockades();
		Point agentPoint;
		// Find the first blockade that is in range.

		agentPoint = PositionLocate.getPosition(getMeAsHuman(), worldmodel);
		// int x = me().getX();
		// int y = me().getY();
		for (EntityID next : ids) {
			Blockade b = (Blockade) model.getEntity(next);
			double d;

			d = DistanceUtilities.getDistanceToBlock(b, agentPoint);
			if (maxDistance < 0 || d < maxDistance) {
				return b;
			}
		}
		return null;
	}

	public Path getTargetBlockadeAtLocation() {

		return null;
	}

	protected void processMessage(MessageCount message) {
		int counter = message.getCounter();
		MessageCount reply = new MessageCount(counter + 1);
		sendMessage(reply, MessagePriority.High);
	}

	@Override
	protected void thinkAndAct() {
	}

	public boolean isOnBlockedRoad() {
		StandardEntity location = location();
		if (location instanceof Road) {
			Road r = (Road) location;
			if (r.isBlockadesDefined() && r.getBlockades().size() > 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void processMessage(Message message) {
		if (message instanceof BuildingIsExploredMessage) {
			BuildingIsExploredMessage explorationMessage = (BuildingIsExploredMessage) message;
			processExplorationMessage(explorationMessage);
		}
		if (message instanceof ClearPathIsNeededMessage) {
			ClearPathIsNeededMessage clearPathMessage = (ClearPathIsNeededMessage) message;
			processClearPathMessage(clearPathMessage);
		}
		if (message instanceof AgentIsStuckMessage) {
			AgentIsStuckMessage agentIsStuckMessage = (AgentIsStuckMessage) message;
			processAgentIsStuckMessage(agentIsStuckMessage);
		}
		if (message instanceof AssignRefugeMessage) {
			AssignRefugeMessage assignRefugeMessage = (AssignRefugeMessage) message;
			processAssignRefugeMessage(assignRefugeMessage);
		}
		if (message instanceof RoadIsClearedMessage) {
			RoadIsClearedMessage roadIsClearMessage = (RoadIsClearedMessage) message;
			processRoadIsClearMessageMessage(roadIsClearMessage);
		}
		if (message instanceof AssignFiredBuildingMessage) {
			AssignFiredBuildingMessage assignBuildingMessage = (AssignFiredBuildingMessage) message;
			processAssignFiredBuildingMessage(assignBuildingMessage);
		}
	}

	protected void processAssignFiredBuildingMessage(
			AssignFiredBuildingMessage message) {
		EntityID agentID = message.getAgentId();
		EntityID targetBuildingID = message.getBuildingId();

		if (agentID.getValue() == this.getID().getValue()) {
			this.targetBuilding = targetBuildingID;

		}
	}

	protected void processRoadIsClearMessageMessage(RoadIsClearedMessage message) {
		EntityID roadID = message.getRoadId();

	}

	protected void processAssignRefugeMessage(AssignRefugeMessage message) {
		EntityID agentID = message.getAgentId();
		EntityID targetRefugeID = message.getRefugeId();

		if (agentID.getValue() == this.getID().getValue()) {
			Refuge refuge = (Refuge) worldmodel.getEntity(targetRefugeID);
			this.targetRefuge = refuge;

		}
	}

	protected void processClearPathMessage(ClearPathIsNeededMessage message) {
		EntityID startId = message.getStartId();
		StandardEntity startRoad = worldmodel.getEntity(startId);
		EntityID finishId = message.getFinishId();
		StandardEntity finish = worldmodel.getEntity(finishId);
		if (finish instanceof Building) {
			Road entrance = getEntranceRoad((Building) finish);
			if (entrance != null) {
				finishId = entrance.getID();
			}
		}

	}

	// 智能体被堵，要及时响应
	@SuppressWarnings("unused")
	protected void processAgentIsStuckMessage(AgentIsStuckMessage message) {
		EntityID startId = message.getLocationId();
		EntityID finishId = message.getLocationId();
		StandardEntity startRoad = worldmodel.getEntity(startId);
		int dis = worldmodel.getDistance(this.getID(), startId);

		
		if (!PFTarget.isEmpty() && PFTarget.contains(finishId.getValue())) {
			return;
		}
		else{
			PFTarget.add(finishId);
			System.out.print("PF-> Received stuck msg.收到"+finishId+"被堵信息\n");
			
		}

	}

	@Override
	protected boolean canRescue() {
		return false;
	}


	public Blockade getClosestBlockade() {
		Human human;
		List<Blockade> blockades;
		MultiBlockDistanceComparator comparator;
		Point position;
		Blockade target;

		human = getMeAsHuman();
		position = PositionLocate.getPosition(human, worldmodel);
		comparator = new MultiBlockDistanceComparator(getModel(), position);
		blockades = getBlockades();

		if (blockades.isEmpty()) {
			return null;
		}
		Collections.sort(blockades, comparator);

		target = blockades.get(0);
		return target;
	}

	public Blockade getClosestBlockadeInCurrentLocation() {
		Human human;
		Area area;
		Road road;
		Blockade target;

		human = getMeAsHuman();
		area = (Area) human.getPosition(getModel());

		if (area instanceof Road) {
			road = (Road) area;
			target = getClosestBlockadeInRoad(road);
		} else {
			target = null;
		}

		return target;
	}

	public Path getPathToClosestBlockedRoad() {
		Path path;
		List<Road> blockedRoads;
		Human human;

		human = getMeAsHuman();
		blockedRoads = getBlockedRoads();
		path = search.getPath(human, blockedRoads, PathType.Shortest);
		return path;
	}

	@Override
	public List<MessageType> getMessagesToListen() {
		List<MessageType> types;
		types = new ArrayList<MessageType>();
		types.add(MessageType.ClearPathIsNeededMessage);
		types.add(MessageType.AgentIsStuckMessage);
		types.add(MessageType.AgentIsBuriedMessage);
		types.add(MessageType.BuildingIsExploredMessage);
		types.add(MessageType.BuildingIsBurningMessage);
		types.add(MessageType.BuildingIsExploredMessage);
		types.add(MessageType.AssignRefugeMessage);
		types.add(MessageType.RoadIsClearedMessage);
		return types;
	}

	public SampleWorldModel getWorldModel() {
		return worldmodel;
	}

	@Override
	public void sendClear(EntityID targetId) {
		Blockade blockade;
		int id = targetId.getValue();
		blockade = worldmodel.getEntity(targetId, Blockade.class);
		if (blockade == null) {
			sendMove(timeStep, getRandomWalk());
			return;

		}
		if (clearedBlockade.containsKey(id)) {
			if (clearedBlockade.get(id) == blockade.getRepairCost()) {
				int num = numOfClear.get(id);
				num++;
				numOfClear.put(id, num);
				if (num > 5) {
					// System.out.println(this.getID().getValue() + "blockade"
					// + id + " is null I'm randmom walk");
					sendMove(timeStep, getRandomWalk());
					return;
				}

			}
		}
		if (!RoadUtilities.doesBlockExists(blockade, worldmodel)) {
			sendMove(timeStep, getRandomWalk());
			return;
		}
		Point point = PositionLocate.getPosition(this.getMeAsHuman(),
				getModel());
		int dis = DistanceUtilities.getDistanceToBlock(blockade, point);
		if (dis > distance) {
			sendMove(timeStep, getRandomWalk());
			return;
		}
		super.sendClear(targetId);
		if (!clearedBlockade.containsKey(targetId.getValue())) {
			clearedBlockade.put(id, blockade.getRepairCost());
			numOfClear.put(id, 1);
		}
		if (clearedBlockade.containsKey(targetId.getValue())) {
			if (clearedBlockade.get(id) != blockade.getRepairCost()) {
				clearedBlockade.put(id, blockade.getRepairCost());
				numOfClear.put(id, 1);
			}
		}
	}

	// //////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	@Override
	protected void act(int time, ChangeSet changed, Collection<Command> heard) {

		
		for (Command next : heard) {
			Logger.debug("Heard " + next);
		}
		Path path = null;
		Blockade target = getTargetBlockade();
		
		if(!PFTarget.isEmpty())
		{
/**/		path =this.getPathTo(PFTarget.get(0),PathType.EmptyAndSafe);
			System.out.print("PF-> Go to msgsender.前往信息发送处  位置："+PFTarget.get(0)+"\n");
			//if(path.size() < 2 && target == null){
			//if(this.getID() == PFTarget.get(0)){
			//	RoadIsClearedMessage message = new RoadIsClearedMessage(PFTarget.get(0));
			//	this.sendMessage(message, MessagePriority.High);
			//	System.out.print("PF -> Tell others [C]target\n");
			//	PFTarget.remove(0);
			//	System.out.print("PF -> [C]target\n");
			//}
			if( this.currentPostion ==this.lastPosition && target!=null)
				sendClear(time, target.getID());
			//lastID = this.getID();
			
			this.sendMove(path);
			System.out.print("PF -> [MV] TargetStuck "  +"\n");
		}
		else{
		
			if (target != null) {
				Logger.info("Clearing blockade " + target);
				sendSpeak(time, 1, ("Clearing " + target).getBytes());
				sendClear(time, target.getID());
				super.work = true;
				return;
			}
			// Plan a path to a blocked area
			List<Road> r1 = new ArrayList<Road>();
			for (EntityID e : getBlockedRoads1()) {
				r1.add((Road) worldmodel.getEntity(e));
			}
			

			path = search.getPath(me(), r1, PathType.EmptyAndSafe);// (me(),
		}
		super.act(time, changed, heard);
		// getBlockedRoads1(),
		// PathType.Shortest);//
		// (me().getPosition(),
		// getBlockedRoads());
		if (path != null) {
			try {
				Logger.info("Moving to target");

				Road r = (Road) model.getEntity(path.getEntities().get(
						path.size() - 1));
				Blockade b = getTargetBlockade1(r, -1);

				sendMove(time, path.getEntities(), b.getX(), b.getY());
				Logger.debug("Path: " + path);
				Logger.debug("Target coordinates: " + b.getX() + ", "
						+ b.getY());
				return;
			} catch (Exception e) {

			}
		}
		Logger.debug("Couldn't plan a path to a blocked road");
		Logger.info("Moving randomly");
		sendMove(time, getRandomWalk());
	}

	private List<EntityID> getBlockedRoads1() {
		Collection<StandardEntity> e = model
				.getEntitiesOfType(StandardEntityURN.ROAD);
		List<EntityID> result = new ArrayList<EntityID>();
		for (StandardEntity next : e) {
			Road r = (Road) next;
			if (r.isBlockadesDefined() && !r.getBlockades().isEmpty()) {
				result.add(r.getID());
			}
		}
		return result;
	}

	private Blockade getTargetBlockade() {
		Logger.debug("Looking for target blockade");
		Area location = (Area) location();
		Logger.debug("Looking in current location");
		Blockade result = getTargetBlockade1(location, distance);
		if (result != null) {
			return result;
		}
		Logger.debug("Looking in neighbouring locations");
		for (EntityID next : location.getNeighbours()) {
			location = (Area) model.getEntity(next);
			result = getTargetBlockade(location, distance);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	private Blockade getTargetBlockade1(Area area, int maxDistance) {
		// Logger.debug("Looking for nearest blockade in " + area);
		if (area == null || !area.isBlockadesDefined()) {
			// Logger.debug("Blockades undefined");
			return null;
		}
		List<EntityID> ids = area.getBlockades();
		// Find the first blockade that is in range.
		int x = me().getX();
		int y = me().getY();
		for (EntityID next : ids) {
			Blockade b = (Blockade) model.getEntity(next);
			double d = findDistanceTo(b, x, y);
			// Logger.debug("Distance to " + b + " = " + d);
			if (maxDistance < 0 || d < maxDistance) {
				// Logger.debug("In range");
				return b;
			}
		}
		// Logger.debug("No blockades in range");
		return null;
	}

	private int findDistanceTo(Blockade b, int x, int y) {
		// Logger.debug("Finding distance to " + b + " from " + x + ", " + y);
		List<Line2D> lines = GeometryTools2D.pointsToLines(GeometryTools2D
				.vertexArrayToPoints(b.getApexes()), true);
		double best = Double.MAX_VALUE;
		Point2D origin = new Point2D(x, y);
		for (Line2D next : lines) {
			Point2D closest = GeometryTools2D.getClosestPointOnSegment(next,
					origin);
			double d = GeometryTools2D.getDistance(origin, closest);
			// Logger.debug("Next line: " + next + ", closest point: " + closest
			// + ", distance: " + d);
			if (d < best) {
				best = d;
				// Logger.debug("New best distance");
			}

		}
		return (int) best;
	}
}