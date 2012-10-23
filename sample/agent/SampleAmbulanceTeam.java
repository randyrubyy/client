package sample.agent;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import sample.agent.SampleAgent;
import sample.message.MessagePriority;
import sample.message.Type.AgentIsBuriedMessage;
import sample.message.Type.BuildingIsExploredMessage;
import sample.message.Type.CivilianInformationMessage;
import sample.message.Type.CivilianIsSavedOrDeadMessage;
import sample.message.Type.Message;
import sample.message.Type.MessageCount;
import sample.message.Type.MessageType;
import sample.utilities.DistanceSorter;
import sample.utilities.Path;
import sample.utilities.Search.PathType;

/**
 * SEU's Ambulance Team Agent.
 */
public class SampleAmbulanceTeam extends SampleAgent<AmbulanceTeam> {
    private EntityID searchTarget=null;
	public SampleAmbulanceTeam() {
		super();
	}

	@Override
	public String toString() {
		return "SEU Ambulance Team #" + getNo();
	}

	@Override
	protected void postConnect() {
		super.postConnect();
		new HashSet<EntityID>();

	}

	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.AMBULANCE_TEAM);
	}

	protected void processMessage(MessageCount message) {
		int counter = message.getCounter();
		MessageCount reply = new MessageCount(counter + 1);
		sendMessage(reply, MessagePriority.High);
	}

	@Override
	protected void thinkAndAct() {
	}

	protected void printKnownCivilians() {
		for (StandardEntity next : worldmodel.getHumans()) {
			if (next instanceof Civilian) {
				Civilian civilian = (Civilian) next;
				EntityID id = civilian.getPosition();
				StandardEntity position = worldmodel.getEntity(id);
				if (position instanceof Building) {
				}
			}
		}
	}

	@Override
	protected void processMessage(Message message) {
		if (message instanceof BuildingIsExploredMessage) {
			BuildingIsExploredMessage explorationMessage = (BuildingIsExploredMessage) message;
			processExplorationMessage(explorationMessage);
		}
		if (message instanceof CivilianInformationMessage) {
			CivilianInformationMessage civilianInformationMessage = (CivilianInformationMessage) message;
			processCivilianInformationMessage(civilianInformationMessage);
		}
		if (message instanceof CivilianIsSavedOrDeadMessage) {
			CivilianIsSavedOrDeadMessage civilianIsSavedMessage = (CivilianIsSavedOrDeadMessage) message;
			processCivilianIsSavedMessage(civilianIsSavedMessage);
		}
		if (message instanceof AgentIsBuriedMessage) {
			AgentIsBuriedMessage agentIsBuriedMessage = (AgentIsBuriedMessage) message;
			processAgentIsBuriedMessage(agentIsBuriedMessage);
		}
	}

	protected void processCivilianInformationMessage(
			CivilianInformationMessage message) {
		EntityID location = new EntityID(message.getLocationId());
		StandardEntity civilianLocation = worldmodel.getEntity(location);
		Civilian civilian = new Civilian(new EntityID(message.getCivilianId()));
		civilian.setHP(message.getHp());
		civilian.setDamage(message.getDamage());
		civilian.setBuriedness(message.getBuriedness());

	//	int deathTime = getTimeStep()
	//			+ civilian.getHP()/civilian.getDamage();

	}

	protected void processAgentIsBuriedMessage(AgentIsBuriedMessage message) {
		EntityID location = new EntityID(message.getLocationId());
		StandardEntity agentLocation = worldmodel.getEntity(location);
		Human agent;

		agent = worldmodel
				.getEntity(new EntityID(message.getAgentId()), Human.class);
		if (agent != null) {
			agent.setHP(message.getHp());
			agent.setDamage(message.getDamage());
			agent.setBuriedness(message.getBuriedness());
		//	int deathTime = getTimeStep()
		//			+ agent.getHP()/agent.getDamage();

			int dis = worldmodel.getDistance(me().getID(), location);

		}
	}

	public void processCivilianIsSavedMessage(
			CivilianIsSavedOrDeadMessage message) {

	}

	@Override
	protected boolean canRescue() {
		return true;
	}

	@Override
	public List<MessageType> getMessagesToListen() {
		List<MessageType> types;

		types = new ArrayList<MessageType>();
		types.add(MessageType.AgentIsBuriedMessage);
		types.add(MessageType.CivilianInformationMessage);
		types.add(MessageType.CivilianIsSavedOrDeadMessage);
		types.add(MessageType.BuildingIsExploredMessage);
		types.add(MessageType.BuildingIsBurningMessage);
		types.add(MessageType.BuildingIsExtinguishedMessage);
		types.add(MessageType.CivilianIsSavedOrDeadMessage);
		types.add(MessageType.CivilianIsSavedOrDeadMessage);
		return types;
	}

	// ///////////////////////////////////////////////////////////////////

	@Override
	protected void act(int time, ChangeSet changed, Collection<Command> heard) {

		updateUnexploredBuildings(changed);
		// Am I transporting a civilian to a refuge?
		if (someoneOnBoard()) {
			// Am I at a refuge?
			if (location() instanceof Refuge) {
				// Unload!
				Logger.info("Unloading");
				sendUnload(time);
				return;
			} else {
				// Move to a refuge
				Path path = getPathToRefuge();
				// search.breadthFirstSearch(me().getPosition(), refugeIDs);
				if (path != null) {
					Logger.info("Moving to refuge");
					sendMove(path);
					return;
				}
				// What do I do now? Might as well carry on and see if we can
				// dig someone else out.
				Logger.debug("Failed to plan path to refuge");
			}
		}

		for (Human next : getTargets()) {
			if (next.getPosition().equals(location().getID())) {
				// Targets in the same place might need rescueing or loading
				if ((next instanceof Civilian) && next.getBuriedness() == 0
						&& !(location() instanceof Refuge)&&!(location() instanceof Road)) {
					// Load
					Logger.info("Loading " + next);
					sendLoad(time, next.getID());
					return;
				}
				if (next.getBuriedness() > 0) {
					// Rescue
					Logger.info("RgetTargetsescueing " + next);
					sendRescue(time, next.getID());
					return;
				}
			}

			else {
		
				// Try to move to the target
				try {
					if (!next.isPositionDefined())
						continue;
					if (next.getPosition() == null)
						continue;
					if (worldmodel.getEntity(next.getPosition()) instanceof Refuge)
						continue;
					if (next.isBuriednessDefined())
						if (next.getBuriedness() == 0)
							continue;
					Path path = search.getPath(me(), next,
							PathType.LowBlockRepair);
					// search.breadthFirstSearch(me().getPosition(),
					// next.getPosition());
					if (path != null && path.isPassable()) {
						Logger.info("Moving to target");
						sendMove(path);
					
						return;
					}
				} catch (Exception e) {
				}
			}
		}
		// List<Building> buildingwithhuman = getbuildingwithHuman();
		// Path path1 = null;
		// if (!buildingwithhuman.isEmpty()) {
		// path1 = getSearch().getPath(me(), buildingwithhuman,
		// PathType.EmptyAndSafe);
		// if (path1 != null) {
		// if (path1.isPassable()) {
		//
		// sendMove(path1);
		// return;
		// }
		// }
		// }

		// Nothing to do
//		if(searchTarget!=null&&this.location().getID().getValue()==searchTarget.getValue()){
//			searchTarget=null;
//		}
//		if(searchTarget==null){
//			List<Building> b1 = new ArrayList<Building>();
//			// =new ArrayList<Building>;
//			for (EntityID e : unexploredBuildings1) {
//				StandardEntity s = world.getEntity(e);
//				b1.add((Building) s);
//			}
//			Path path = search.getPath(me(), b1, PathType.LowBlockRepair);
//			searchTarget=path.getDestination();
//			// (me().getPosition(),unexploredBuildings);
//			if (path != null) {
//				Logger.info("Searching buildings");
//				System.out.println("time"+time+"  "+me().getID()+"AT"+"Search newbuildings");
//				sendMove(path);
//				return;
//			}
//		}else{
//			StandardEntity e=this.world.getEntity(searchTarget);
//		    if(e instanceof Building){
//		    	Building b=(Building)e;
//		    	Path path=search.getPath(me(), b, PathType.LowBlockRepair);
//			    if (path != null) {
//				Logger.info("Searching buildings");
//				System.out.println("time"+time+"  "+me().getID()+"AT"+"Search oldbuildings");
//				sendMove(path);
//				return;
//			}
//		    }
//			
//		}
		// System.out.println("   time   "+timeStep+"    ");
		Logger.info("Moving randomly");
		List<EntityID> e=getRandomWalk();
		sendMove(time, e);
	}

	// @Override
	// protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
	// return EnumSet.of(StandardEntityURN.AMBULANCE_TEAM);
	// }

	private boolean someoneOnBoard() {
		for (StandardEntity next : model
				.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
			if (((Human) next).getPosition().equals(getID())) {
				Logger.debug(next + " is on board");
				return true;
			}
		}
		return false;
	}

	private List<Human> getTargets() {
		List<Human> targets = new ArrayList<Human>();
		for (StandardEntity next : model.getEntitiesOfType(
				StandardEntityURN.CIVILIAN, StandardEntityURN.FIRE_BRIGADE,
				StandardEntityURN.POLICE_FORCE,
				StandardEntityURN.AMBULANCE_TEAM)) {
			Human h = (Human) next;
			if (h == me()) {
				continue;
			}
			if (h.isHPDefined() && h.isBuriednessDefined()
					&& h.isDamageDefined() && h.isPositionDefined()
					&& h.getHP() > 0
					&& (h.getBuriedness() >= 0 || h.getDamage() > 0)) {

				targets.add(h);
			}
		}
		Collections.sort(targets, new DistanceSorter(location(), model));

		return targets;
	}

	private List<Building> getbuildingwithHuman() {
		List<Building> b1 = new ArrayList<Building>();
		for (Human h : getTargets()) {
			StandardEntity position = null;
			position = h.getPosition(getModel());
			if (position instanceof Building && (!(position instanceof Refuge))) {
				Building b = (Building) position;
				b1.add(b);

			}
		}
		return b1;
	}

	private void updateUnexploredBuildings(ChangeSet changed) {
		for (EntityID next : changed.getChangedEntities()) {
			unexploredBuildings1.remove(next);
		}
	}
}