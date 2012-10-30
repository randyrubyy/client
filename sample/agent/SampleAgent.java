package sample.agent;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import rescuecore2.Constants;
import rescuecore2.config.Config;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntityConstants.Fieryness;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import sample.agent.Configuration;
import sample.message.MessageController;
import sample.message.MessageMemory;
import sample.message.MessagePriority;
import sample.message.MessageTranslator;
import sample.message.QueueMessage;
import sample.message.Type.AgentIsBuriedMessage;
import sample.message.Type.AgentIsStuckMessage;
import sample.message.Type.BuildingIsBurningMessage;
import sample.message.Type.BuildingIsExploredMessage;
import sample.message.Type.BuildingIsExtinguishedMessage;
import sample.message.Type.CivilianInformationMessage;
import sample.message.Type.CivilianIsSavedOrDeadMessage;
import sample.message.Type.InformTeamMessage;
import sample.message.Type.Message;
import sample.message.Type.MessageCount;
import sample.message.Type.MessageType;
import sample.message.Type.PartitionAssignmentMessage;
import sample.object.Exploration;
import sample.object.SampleWorldModel;
import sample.object.Building.BuildingInfo;
import sample.object.Partition.EntityInPartition;
import sample.object.Partition.GroupingType;
import sample.object.Partition.PartitionMap;
import sample.object.Road.MultiBlockDistanceComparator;
import sample.object.Road.RoadInfo;
import sample.object.Road.RoadUtilities;
import sample.utilities.DistanceComparator;
import sample.utilities.Path;
import sample.utilities.PositionHistory;
import sample.utilities.PositionLocate;
import sample.utilities.Search.AbstractSearch;
import sample.utilities.Search.AStarSearch;
import sample.utilities.Search.PathType;

/**
 * SEU's Top Level Agent
 */
public abstract class SampleAgent<E extends StandardEntity> extends
		StandardAgent<E> {

	public static final double AGENT_RADIUS = 500;
	private static final int RANDOM_WALK_LENGTH = 3;
	private static final int MIN_DAMAGE_GO_TO_REFUGE = 0;
	private static final int DEFAULT_AGENT_THINK_TIME = 1000;
	private static final double AGENT_THINK_TIME_TOLERANCE = 0.8;
	private static final int DEFAULT_VIEW_DISTANCE = 30000;
	private static final int DEFAULT_RANDOM_WALK_PENALTY = 3;
	private static final int IGNORE_COMMANDS_AFTER = 2;
	protected static final int MAX_PATH_LENGTH = 30;

	@SuppressWarnings("unused")
	private static final String SAY_COMMUNICATION_MODEL = "kernel.standard.StandardCommunicationModel";
	private static final String MAX_PLATOON_CHANNELS_KEY = "comms.channels.max.platoon";
	private static final String SPEAK_COMMUNICATION_MODEL = "kernel.standard.ChannelCommunicationModel";
	protected static final String START_TIME_KEY = "kernel.agents.ignoreuntil";
	private static final String AGENT_THINK_TIME_KEY = "kernel.agents.think-time";
	private static final String VIEW_DISTANCE_KEY = "perception.los.max-distance";

	protected int viewDistance;
	// 只有Say通信时边走边说
	protected MessageTranslator sayMessageTransfer;

	protected int timeStep;
	protected int startTime;
	protected int totalSent;
	protected int totalReceived;
	protected int agentThinkTime;
	protected int randomWalkPenalty;
	protected int desiredPath;

	protected SampleWorldModel worldmodel;
	protected Collection<StandardEntity> unDiscoveredBuildings;
	protected Collection<Pair<Building, Fieryness>> informedBurningBuildings;

	protected MessageController messageController;
	protected MessageMemory messageMemory;
	protected ChangeSet changeSet;
	protected Set<EntityID> changedEntities;
	protected List<EntityID> observableBuildings;
	@SuppressWarnings( { "rawtypes" })
	protected StandardEntity locationAtLastCycle;
	protected StandardEntityURN agentType;
	protected List<Building> burningBuildingsInChangeSet;
	protected List<Human> foundAgents;
	protected List<Building> heatingBuildings;
	protected Date decisionStartTime;
	protected List<Civilian> civiliansAround;
	protected Collection<EntityID> unexploredBuildings1;
	protected boolean work=false;
	// 死亡的智能体和Civilain
	private List<EntityID> deadHumans;

	public List<EntityID> getDeadHumans() {
		return deadHumans;
	}

	public void setSavedCivAndAgents(List<EntityID> savedCivAndAgents) {
	}

	// 记录到达的地点和时间
	PositionHistory positionHistory;
	/**
	 * The search algorithm.
	 */
	protected AbstractSearch search;
	/**
	 * Whether to use AKSpeak messages or not.
	 */
	protected boolean useSpeak;

	/**
	 * Construct a SEU Agent.
	 */
	protected SampleAgent() {
	}

	public int getNo() {
		if (worldmodel == null) {
			return 0;
		}
		List<EntityID> team = worldmodel.getTeam();
		if (team.contains(getID())) {
			return team.indexOf(getID());
		} else {
			return 0;
		}
	}

	public void superPostConnect() {
		super.postConnect();
	}

	// 记录上上周期，上一周期，当前周期智能体位置
	protected StandardEntity latestPosition;
	protected StandardEntity lastPosition;
	protected StandardEntity currentPostion;

	// 重复次数
	int sum = 0;

	// 至少保留智能体三周期的位置

	@Override
	protected void postConnect() {

		super.postConnect();

		mergeConfigFiles();

		// 解决indexClass空指针的问题
		this.worldmodel = (SampleWorldModel) super.model;

		search = new AStarSearch(worldmodel);

		useSpeak = config.getValue(Constants.COMMUNICATION_MODEL_KEY).equals(
				SPEAK_COMMUNICATION_MODEL);
		agentThinkTime = config.getIntValue(AGENT_THINK_TIME_KEY,
				DEFAULT_AGENT_THINK_TIME);
		sayMessageTransfer = new MessageTranslator(config);
		startTime = config.getIntValue(START_TIME_KEY);
		viewDistance = config.getIntValue(VIEW_DISTANCE_KEY,
				DEFAULT_VIEW_DISTANCE);

		worldmodel.indexClass(StandardEntityURN.CIVILIAN,
				StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.POLICE_FORCE,
				StandardEntityURN.AMBULANCE_TEAM, StandardEntityURN.REFUGE,
				StandardEntityURN.BUILDING);

		unDiscoveredBuildings = worldmodel
				.getEntitiesOfType(StandardEntityURN.BUILDING);

		messageMemory = new MessageMemory(IGNORE_COMMANDS_AFTER);
		messageController = new MessageController();
		informedBurningBuildings = new ArrayList<Pair<Building, Fieryness>>();
		observableBuildings = new ArrayList<EntityID>();
		locationAtLastCycle = location();
		burningBuildingsInChangeSet = new ArrayList<Building>();
		heatingBuildings = new ArrayList<Building>();
		civiliansAround = new ArrayList<Civilian>();

		worldmodel.initializeMapSearching(getGroupingType());

		// 对这个方法进行修改
		worldmodel.createNodeCaches();

		foundAgents = new ArrayList<Human>();

		// 初始化
		deadHumans = new ArrayList<EntityID>();

		initializePositionHistory();
	}

	public void initializePositionHistory() {
		StandardEntity me;

		me = me();
		if (me instanceof Human) {
			Human human = (Human) me;

			positionHistory = new PositionHistory(human, worldmodel);
		} else {
			positionHistory = new PositionHistory();
		}
	}

	/**
	 * Get the location of the entity controlled by this agent.
	 * 
	 * @return The location of the entity controlled by this agent.
	 */
	@Override
	public StandardEntity location() {
		E me = me();
		if (me instanceof Human) {
			return ((Human) me).getPosition(worldmodel);
		}
		return me;
	}

	@Override
	// @SuppressWarnings("unchecked")
	protected E me() {
		return super.me();
	}

	/**
	 * Construct a random walk starting from this agent's current location.
	 * Buildings will only be entered at the end of the walk.
	 * 
	 * @return A random walk.
	 */
	protected List<EntityID> getRandomWalk() {
		List<EntityID> result = new ArrayList<EntityID>(RANDOM_WALK_LENGTH);
		Set<StandardEntity> seen = new HashSet<StandardEntity>();
		StandardEntity current = location();
		try {
			for (int i = 0; i < RANDOM_WALK_LENGTH; ++i) {
				// if (current != null)
				result.add(current.getID());
				seen.add(current);
				List<StandardEntity> neighbours = new ArrayList<StandardEntity>(
						search.findNeighbours(current));
				
				//List<StandardEntity> neighbours = getAssignedPartitionMap().
				
				Collections.shuffle(neighbours, random);
				boolean found = false;
				for (StandardEntity next : neighbours) {
					if (seen.contains(next)) {
						continue;
					}
					if (next instanceof Building && i < RANDOM_WALK_LENGTH - 1) {
						continue;
					}
					
					
					current = next;
					found = true;
					break;
				}
				if (!found) {
					// We reached a dead-end.
					break;
				}
			}
		} catch (Exception e) {
			System.err.println("time  " + timeStep + "   id  " + getID()
					+ "   randomwalk--err");
		}
		return result;
	}

	/**
	 * Construct a random walk starting from this agent's current location.
	 * Buildings will only be entered at the end of the walk.
	 * 
	 * @param partition
	 *            The region of the agent in the world model.
	 * @return A random walk.
	 */
	protected List<EntityID> getRandomWalk(EntityInPartition partition) {
		List<EntityID> result = new ArrayList<EntityID>(RANDOM_WALK_LENGTH);
		Set<StandardEntity> seen = new HashSet<StandardEntity>();
		StandardEntity current = location();
		for (int i = 0; i < RANDOM_WALK_LENGTH; ++i) {
			result.add(current.getID());
			seen.add(current);
			List<StandardEntity> neighbours = new ArrayList<StandardEntity>(
					search.findNeighbours(current));
			Collections.shuffle(neighbours, random);
			boolean found = false;
			for (StandardEntity next : neighbours) {
				if (seen.contains(next)) {
					continue;
				}
				if (next instanceof Building && i < RANDOM_WALK_LENGTH - 1) {
					continue;
				}
				current = next;
				found = true;
				break;
			}
			if (!found) {
				// We reached a dead-end.
				break;
			}
		}
		return result;
	}

	/**
	 * Get an EnumSet containing requested entity URNs.
	 * 
	 * @return An EnumSet containing requested entity URNs.
	 */
	@Override
	protected abstract EnumSet<StandardEntityURN> getRequestedEntityURNsEnum();

	@Override
	protected StandardWorldModel createWorldModel() {
		return new SampleWorldModel();
	}

	protected boolean shouldProcessCommand(Command command) {
		if (command.getAgentID().equals(getID())) {
			return false;
		}
		if (getTimeStep() - command.getTime() > IGNORE_COMMANDS_AFTER) {
			return false;
		}
		return true;
	}

	protected void processHearing(Collection<Command> heard) {

		messageMemory.clearOldMessages(timeStep);
		for (Command command : heard) {
			if (command instanceof AKSpeak) {
				AKSpeak speakCommand;

				speakCommand = (AKSpeak) command;
				if (isCivilianCommand(speakCommand)) {
					processCivilianCommand(speakCommand);
				}
				if (shouldProcessCommand(command)) {
					processHearing((AKSpeak) command);
				} else {
				}
			} else {
			}
		}
	}

	protected void processObserving(ChangeSet changed) {
		observableBuildings.clear();
		burningBuildingsInChangeSet.clear();
		foundAgents.clear();
		heatingBuildings.clear();
		civiliansAround.clear();
		changedEntities = changed.getChangedEntities();
		for (EntityID next : changedEntities) {
			StandardEntity entity = worldmodel.getEntity(next);
			if (entity instanceof Building) {
				Building building = (Building) entity;
				observableBuildings.add(next);
				if (building.isOnFire()) {
					Pair<Building, Fieryness> pair;
					burningBuildingsInChangeSet.add(building);
					pair = new Pair<Building, Fieryness>(building, building
							.getFierynessEnum());
					if (building.isOnFire()
							&& !informedBurningBuildings.contains(pair)) {
						informedBurningBuildings.add(pair);
						int fieryness = 0;
						int temperature = 0;
						if (building.isFierynessDefined()) {
							fieryness = building.getFieryness();
						}
						if (building.isTemperatureDefined()) {
							temperature = building.getTemperature();
						}
						BuildingIsBurningMessage message = new BuildingIsBurningMessage(
								building.getID(), fieryness, temperature);
						///////////////////////////////////////////////////////////////
						/**
						 * @author Dangyifei
						 * 发送着火
						 */
						System.out.print(this.getMeAsHuman().getStandardURN()
								+ "-> 我在找 发送着火信息  位置 :" +building.getID() + "\n");
						
						sendMessage(message, MessagePriority.High);
					}
				} else {
					for (Fieryness f : Building.BURNING) {
						Pair<Building, Fieryness> pair;

						pair = new Pair<Building, Fieryness>(building, f);
						if (informedBurningBuildings.contains(pair)) {
							informedBurningBuildings.remove(pair);
							BuildingIsExtinguishedMessage message;

							message = new BuildingIsExtinguishedMessage(
									building.getID(), building.getFieryness());
							sendMessage(message, MessagePriority.Medium);
						}
					}

				}
				processBuildingHeat(building);

			} else if (entity instanceof Human) {
				Human human = (Human) entity;
				if ((human.getStandardURN() == StandardEntityURN.FIRE_BRIGADE
						|| human.getStandardURN() == StandardEntityURN.POLICE_FORCE || human
						.getStandardURN() == StandardEntityURN.AMBULANCE_TEAM)
						&& human.getID().getValue() != me().getID().getValue()) {

					// 死亡的智能体
					if (human.isHPDefined())
						if (human.getHP() == 0) {
							if (!deadHumans.contains(human.getID()))
								deadHumans.add(human.getID());
						}

					foundAgents.add(human);

				} else if (human.getStandardURN() == StandardEntityURN.CIVILIAN) {
					Civilian civilian;
					StandardEntity location;

					civilian = (Civilian) entity;
					civiliansAround.add(civilian);
					location = civilian.getPosition(model);
					if (location instanceof Area) {
						Area area = (Area) civilian.getPosition(model);
						if (area instanceof Building) {
							getExploration().addToExplorationHistory(
									(Building) area);
						}
						if (civilian.getHP() == 0) {

							// 死亡的市民
							if (!deadHumans.contains(human.getID()))
								deadHumans.add(human.getID());

							CivilianIsSavedOrDeadMessage civilianIsSavedOrDeadMessage = new CivilianIsSavedOrDeadMessage(
									civilian.getID());
							sendMessage(civilianIsSavedOrDeadMessage,
									MessagePriority.Medium);
						} else {
							CivilianInformationMessage message = new CivilianInformationMessage(
									area.getID().getValue(), civilian.getID()
											.getValue(), timeStep, civilian
											.getHP(), civilian.getBuriedness(),
									civilian.getDamage());
							if (canRescue()) {
								processMessage(message);
							}
							// /////////////////////////////////////////////////////////////
							/**
							 * @author Dangyifei
							 * 发现智能体或者市民被埋
							 */
							System.out.print(this.getMeAsHuman()
									.getStandardURN()
									+ "-> 我在发送市民信息  市民 :"
									+ civilian.getID() + "\n");
							
							sendMessage(message, MessagePriority.High);
						}

					}
				}

			} else if (entity instanceof Blockade) {
				Blockade blockade;
				EntityID roadId;
				RoadInfo roadInfo;

				blockade = (Blockade) entity;
				roadId = blockade.getPosition();
				roadInfo = worldmodel.getRoadInfo(roadId);
				roadInfo.resetBlockadeCache();
			} else if (entity instanceof Road) {
				Property property;
				property = changed.getChangedProperty(entity.getID(),
						StandardPropertyURN.BLOCKADES.toString());
				if (property != null) {
					RoadInfo roadInfo;

					roadInfo = worldmodel.getRoadInfo(entity.getID());
					roadInfo.resetBlockadeCache();
				}
			}
		}
	}

	/**
	 * This function is used to process AKSpeak commands.
	 * 
	 * @param command
	 *            AKSpeak command which comes from other agents.
	 */
	protected void processHearing(AKSpeak command) {
		byte[] bytes = command.getContent();
		try {
			Message message = sayMessageTransfer.receive(bytes);
			message.setSender(command.getAgentID());
			message.setReceivedStep(timeStep);

			if (messageMemory.shouldProcessMessage(message)) {
				totalReceived++;
				if (message instanceof PartitionAssignmentMessage) {
					PartitionAssignmentMessage m = (PartitionAssignmentMessage) message;
					processMessage(m);
				} else if (message instanceof InformTeamMessage) {
					InformTeamMessage m = (InformTeamMessage) message;
					processInformTeamMessage(m);
				} else if (message instanceof MessageCount) {
					MessageCount m = (MessageCount) message;
					processMessage(m);
				} else if (message instanceof BuildingIsBurningMessage) {
					BuildingIsBurningMessage m = (BuildingIsBurningMessage) message;
					processBuildingIsBurningMessage(m);
				} else if (message instanceof BuildingIsExtinguishedMessage) {
					BuildingIsExtinguishedMessage m = (BuildingIsExtinguishedMessage) message;
					processBuildingIsExtinguishedMessage(m);
				} else {
					processMessage(message);
				}
				messageMemory.setMessageAsProcessed(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This function processes the walkie talkie messages.
	 * 
	 * @param message
	 *            Message which comes from walkie talkie
	 */
	protected abstract void processMessage(Message message);

	public void sendMessage(Message message, MessagePriority priority) {
		int channel;

		channel = sayMessageTransfer.getChannel(message.getType());
		messageController.addMessage(channel, message, priority);
	}

	public void sendMessage(Message message, int channel) {
		int id = sayMessageTransfer.getNextMessageId();
		message.setId(id);
		byte[] bytes = sayMessageTransfer.transmit(message);
		int n = sayMessageTransfer.getRepeatCount(channel);

		for (int i = 0; i < n; i++) {
			sendSpeak(getTimeStep(), channel, bytes);
		}

		sayMessageTransfer.incrementMessageId();
		messageController.clearQueue();
	}

	protected void sendMessagesFromQueue() {
		// System.out.println(" Queue size : " + messageController.queueSize());
		Collection<QueueMessage> importantMessages = messageController
				.getMostImportantMessages();
		if (importantMessages == null) {
		} else {
			for (QueueMessage next : importantMessages) {
				Message message = next.message;

				int channel = next.channel;
				try {
					sendMessage(message, channel);
				} catch (Exception e) {
					System.err.println("Send" + message + "error!!!");
				}
			}

		}
	}

	public int getTimeStep() {
		return timeStep;
	}

	public void setTimeStep(int timeStep) {
		this.timeStep = timeStep;
	}

	// @Override
	protected void act(int time, ChangeSet changed, Collection<Command> heard) {

		
		/**
		 * @author Dangyifei
		 * 智能体被堵发送信息
		 */
		System.out.print(this.getMeAsHuman().getStandardURN()+""+this.location()+"位置 :"+ this.lastPosition + "\n");
		if (!work&&this.isStuck(getMeAsHuman(), true)
		&&this.location()==this.lastPosition )// this.location().equals(this.positionHistory))
		{
			System.out.print(this.getMeAsHuman().getStandardURN()
					+ "-> 我在发送被堵信息  位置 :" + this.getID() + "\n");
			// System.out.print("AT -> [SEND]i'M sTUCK~!!! 发送被堵信息\n");
			AgentIsStuckMessage message = new AgentIsStuckMessage(this
					.location().getID());
			this.sendMessage(message, MessagePriority.Medium);
		}
		//this.lastPosition = this.location();
		
		Area testArea = (Area)getMeAsHuman().getPosition(getModel());
		
		System.out.println(testArea);
		
	if(this.getMeAsHuman().getBuriedness()>0){
		System.out.print(this.getMeAsHuman().getStandardURN()
				+ "-> 我在发送自己被埋信息  位置 :" + this.getID() + "\n");
		// System.out.print("AT -> [SEND]i'M sTUCK~!!! 发送被堵信息\n");
		AgentIsBuriedMessage message = new AgentIsBuriedMessage(this
				.location().getID().getValue(),this.getID().getValue(), this.getTimeStep(),
				this.getMeAsHuman().getHP(),this.getMeAsHuman().getBuriedness(), this.getMeAsHuman().getDamage());
			this.sendMessage(message, MessagePriority.High);

		//TODO: 发现被埋，房屋着火，卡住了
		

	}
		
	}

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard) {
		Date decisionEndTime;

		decisionStartTime = new Date();

		setTimeStep(time);

		positionHistory.process(timeStep);
		if (time == 1) {

		}
		if (time == startTime) {
			int[] channels;
			addTeamMembers();
			assignQuartations();
			setCommunicationCondition();
			//
			channels = getChannelsToSubscribe();
			sendSubscribe(time, channels);
		}
		if (time >= startTime) {
			changeSet = changed;
			processObserving(changeSet);

			processHearing(heard);
			if (timeStep == 3) {
				HashSet<EntityID> e = new HashSet<EntityID>();
				for (StandardEntity s : worldmodel
						.getEntitiesOfType(StandardEntityURN.BUILDING))
					e.add(s.getID());
				unexploredBuildings1 = new HashSet<EntityID>(e);
			}

			act(time, changed, heard);

			sendMessagesFromQueue();

		}
		decisionEndTime = new Date();

		double dt = decisionEndTime.getTime() - decisionStartTime.getTime();

		// System.out.println(dt);

		if (dt > agentThinkTime / 2) {
			System.err.println(me() + " Decision Time Needs " + dt + " In "
					+ time);
		}
	}

	protected void addTeamMembers() {
		agentType = me().getStandardURN();
		worldmodel.addTeamMembers(agentType);
	}

	public void assignQuartations() {
		worldmodel.getPartitionModel().assignPartitionsToTeam();
		List<EntityInPartition> regions = getAssignedPartitions();
		for (EntityInPartition region : regions) {
			getExploration().addEntitiesToExplore(region.getBuildings());
		}
	}

	protected void processInformTeamMessage(InformTeamMessage message) {
		EntityID agentId = message.getSender();
		worldmodel.addToTeam(agentId);
	}

	protected abstract void thinkAndAct();

	public List<EntityInPartition> getAssignedPartitions() {
		if (worldmodel == null) {
			System.err.println("Empty worldmodel");
			return null;
		} else {
			return getAssignedPartitionMap().getPartitions();
		}
	}

	public PartitionMap getAssignedPartitionMap() {
		if (worldmodel == null) {
			System.err.println("Empty worldmodel");
			return null;
		} else {
			return worldmodel.getPartitionModel().getAssignedPartitions(getID());
		}
	}

	public List<Area> getPartitionEntities() {
		List<Area> entities;
		List<EntityInPartition> regions;

		entities = new ArrayList<Area>();
		regions = getAssignedPartitions();
		if (regions == null) {
			return null;
		}
		for (EntityInPartition region : regions) {
			entities.addAll(region.getBuildings());
			entities.addAll(region.getRoads());
		}
		return entities;
	}

	public List<Road> getRoadsInPartition() {
		List<Road> entities;
		List<EntityInPartition> partitions;

		entities = new ArrayList<Road>();
		partitions = getAssignedPartitions();
		if (partitions == null) {
			return null;
		}
		for (EntityInPartition region : partitions) {
			entities.addAll(region.getRoads());
		}
		return entities;
	}

	/**
	 * 从当前位置寻找一条到Refuge的道路
	 * 
	 * @return The path from current location to nearest refuge.
	 */
	public Path getPathToRefuge(PathType pathType) {
		// TODO: 如果该路径不存在，如何处理
		/**
		 * @author iorange
		 * modified to cope with some weird issues...
		 */
		Path path = search.getPath(getMeAsHuman(), getAllRefuges(), pathType);
		if( (path == null)
				|| (path.isEmpty())
				|| (!path.isPassable())
				)
		{
			path = search.getPath(getMeAsHuman(), getAllRefuges(), PathType.LowBlockRepair);
			System.err.println("找不到到Refuge的路，重试一下。。。");
		}
			
		return path;
	}

	public Path getPathToRefuge() {
		return getPathToRefuge(PathType.EmptyAndSafe);
	}

	public Path getPathToPartition() {
		Path path;
		List<Area> entities;

		entities = getPartitionEntities();
		path = search.getPath(getMeAsHuman(), entities, PathType.EmptyAndSafe);
		return path;
	}

	/**
	 * Determines the refuge objects in the world model.
	 * 
	 * @return the lise of refuges in the map.
	 */
	public List<Refuge> getAllRefuges() {
		List<Refuge> result = worldmodel.getEntitiesOfType(Refuge.class,
				StandardEntityURN.REFUGE);
		return result;
	}

	protected void updateUnexploredBuildings(Collection<EntityID> changed) {
		for (EntityID next : changed) {
			StandardEntity e = worldmodel.getEntity(next);
			if (e != null) {
				unDiscoveredBuildings.remove(e);
			}
		}
	}

	public Exploration<Building> getExploration() {
		return worldmodel.getBuildingExploration();
	}

	protected void processExplorationMessage(BuildingIsExploredMessage message) {
		Exploration<Building> exploration = getExploration();
		EntityID buildingId = message.getBuildingId();

		// 解决类的强制转换问题，伊斯坦布尔地图出现该类问题
		try {
			Building building = (Building) worldmodel.getEntity(buildingId);

			if (exploration.shouldExplore(building)) {
				exploration.addToExplorationHistory(building);
			}
		} catch (Exception e) {
			System.err.println("There is No Such Building");
		}
	}

	// 没有被调用
	protected boolean exploreRandomly() {
		List<EntityID> path = null;
		List<EntityInPartition> regions = getAssignedPartitions();
		if (regions.size() == 0) {
			path = getRandomWalk();
		} else {
			path = getRandomWalk(regions.get(0));
		}
		if (path == null || path.isEmpty()) {
			return false;
		} else {
			sendMove(getTimeStep(), path);
			return true;
		}
	}

	protected Path getPathTo(EntityID id, PathType type) {
		StandardEntity entity = worldmodel.getEntity(id);
		if (entity instanceof Area) {
			Path path = search.getPath(getMeAsHuman(), (Area) entity, type);
			return path;
		} else {
			return null;
		}

	}

	protected abstract boolean canRescue();

	public boolean isInMyPartition(StandardEntity entity) {
		return getAssignedPartitionMap().contains(entity);
	}

	public boolean isInNeighborPartition(StandardEntity entity) {
		Set<PartitionMap> neighbors;

		neighbors = worldmodel.getPartitionModel().getNeighbors(
				getAssignedPartitionMap(), 1);
		for (PartitionMap regionGroup : neighbors) {
			if (regionGroup.contains(entity)) {
				return true;
			}
		}
		return false;
	}

	private void mergeConfigFiles() {
		Configuration merger;
		Config simulatorConfig, mergedConfig;

		simulatorConfig = this.config;
		merger = new Configuration();
		mergedConfig = merger.getMergedConfig(simulatorConfig);
		this.config = mergedConfig;
	}

	public boolean shouldGoToRefuge() {
		Human me = getMeAsHuman();
		if (me.isDamageDefined()) {
			return me.getDamage() > MIN_DAMAGE_GO_TO_REFUGE;
		}
		return false;
	}

	public Human getMeAsHuman() {
		StandardEntity entiy = worldmodel.getEntity(me().getID());
		Human me = (Human) entiy;
		return me;
	}

	protected Path getPathToArea(Building center, int distance) {
		// Try to get to anything within maxDistance of the target
		Collection<StandardEntity> targets = worldmodel.getObjectsInRange(center,
				distance);
		Collection<Area> temp;

		temp = new ArrayList<Area>();
		for (StandardEntity entity : targets) {
			if (entity instanceof Area) {
				temp.add((Area) entity);
			}
		}
		if (temp.isEmpty()) {
			return null;
		}

		Path path = search.getPath(getMeAsHuman(), temp, PathType.EmptyAndSafe);
		if (path == null || path.isEmpty()) {
		}
		return path;
	}

	protected void setCommunicationCondition() {
		messageController.setCommunicationCondition(worldmodel);
	}

	public Random getRandomSource() {
		return random;
	}

	public AbstractSearch getSearch() {
		return search;
	}

	public void sendMove(Path path) {
		Point target;
		if (path == null) {
			return;
		} else if (!path.isPassable()) {
			return;
		}
		path = fixBuildingExitPath(path);

		target = path.getTarget();
		if (target == null) {
			sendMove(timeStep, path.getEntities());
		} else {
			sendMove(timeStep, path.getEntities(), target.x, target.y);
		}
		positionHistory.setDesiredLocation(path.getDestination());
		positionHistory.setDesiredPath(path);
	}

	public void sendClear(EntityID targetId) {
		Blockade blockade;

		blockade = worldmodel.getEntity(targetId, Blockade.class);
		if (blockade == null) {
		}
		if (!RoadUtilities.doesBlockExists(blockade, worldmodel)) {
		}
		super.sendClear(timeStep, targetId);
	}

	public void sendRescue(Human human) {
		sendRescue(timeStep, human.getID());
	}

	public Path fixBuildingExitPath(Path path) {
		Path fixedPath;
		StandardEntity entity;

		entity = path.getStart(worldmodel);
		if (entity instanceof Building) {
			if (path.size() > 1) {
				Path p;
				Edge e;
				Area a2, a3;

				p = new Path();
				p.add(path.getStart());
				p.add(path.getEntities().get(1));
				if (path.size() > 2) {
					Point target;

					a2 = worldmodel.getEntity(path.getEntities().get(1), Area.class);
					a3 = worldmodel.getEntity(path.getEntities().get(2), Area.class);
					e = a2.getEdgeTo(a3.getID());
					target = PositionLocate.getCenter(e);
					p.setTarget(target);
				}

				fixedPath = p;
			} else {
				fixedPath = path;
			}
		} else {
			fixedPath = path;
		}
		return fixedPath;
	}

	// 处理任务
	@SuppressWarnings("rawtypes")
	public void handleTasks() {

	}

	public Road getStuckRoad(Human human, boolean isArea) {
		StandardEntity location = human.getPosition(worldmodel);
		if (location instanceof Road) {
			Road road = (Road) location;
			if (road.isBlockadesDefined()) {
				List<EntityID> blockades = road.getBlockades();
				for (EntityID blockadeID : blockades) {
					Blockade blockade = (Blockade) worldmodel.getEntity(blockadeID);
					if (isArea) {
						if (blockade.getShape().intersects(
								PositionLocate.getBounds(human, worldmodel))) {
							return road;
						}
					} else {
						if (blockade.getShape().contains(
								PositionLocate
										.getPosition(human.getID(), worldmodel))) {
							return road;
						}
					}
				}
			}
		}
		return null;
	}

	public Blockade getStuckBlockade(Human human, boolean isArea) {
		StandardEntity location = human.getPosition(worldmodel);
		if (location instanceof Road) {
			Road road = (Road) location;
			if (road.isBlockadesDefined()) {
				List<EntityID> blockades = road.getBlockades();
				for (EntityID blockadeID : blockades) {
					Blockade blockade = (Blockade) worldmodel.getEntity(blockadeID);
					if (isArea) {
						if (blockade.getShape().intersects(
								PositionLocate.getBounds(human, worldmodel))) {
							return blockade;
						}
					} else {
						if (blockade.getShape().contains(
								PositionLocate
										.getPosition(human.getID(), worldmodel))) {
							return blockade;
						}
					}
				}
			}
		}
		return null;
	}

	public boolean isStuck(Human human, boolean isArea) {
		Blockade blocakde = getStuckBlockade(human, isArea);
		if (blocakde == null) {
			return false;
		} else {
			return true;
		}
	}

	public boolean isBuried() {
		Human me = getMeAsHuman();
		if (me.isBuriednessDefined()) {
			if (me.getBuriedness() > 0) {
				return true;
			}
		}
		return false;

	}

	protected boolean isBuriedCallHelp() {
		Human me = getMeAsHuman();
		if (me.isBuriednessDefined()) {
			if (me.getBuriedness() > 0) {
				AgentIsBuriedMessage agentIsBuriedMessage = new AgentIsBuriedMessage(
						location().getID().getValue(), me.getID().getValue(),
						timeStep, me.getHP(), me.getBuriedness(), me
								.getDamage());
				sendMessage(agentIsBuriedMessage, MessagePriority.High);
				// System.out.println(me() + " I AM BURIEDDD !!!!!!!!!!!!!!!!");
				return true;
			}
		}
		return false;

	}

	public PositionHistory getTravelMemory() {
		return positionHistory;
	}

	public boolean isInRefuge() {
		boolean inRefuge;
		StandardEntity location;

		location = location();
		inRefuge = location instanceof Refuge;
		return inRefuge;
	}

	public SampleWorldModel getModel() {
		return worldmodel;
	}

	public void sendLoad(Human human) {
		sendLoad(timeStep, human.getID());
	}

	public void sendUnload() {
		sendUnload(timeStep);
	}

	public void sendRest() {
		sendRest(timeStep);
	}

	public boolean canSee(EntityID id) {
		Set<EntityID> ids;
		boolean canSee;

		ids = changeSet.getChangedEntities();
		canSee = ids.contains(id);
		return canSee;
	}

	@Override
	public String toString() {
		return me().toString();
	}

	@SuppressWarnings("unchecked")
	public List<E> getNearbyTeamAgents() {
		Set<EntityID> nearEntities;
		List<EntityID> team;
		List<E> teamAgents;

		team = worldmodel.getTeam();
		nearEntities = changeSet.getChangedEntities();
		teamAgents = new ArrayList<E>();
		for (EntityID id : team) {
			if (nearEntities.contains(id)) {
				E agent;
				agent = (E) worldmodel.getEntity(id);
				teamAgents.add(agent);
			}
		}
		return teamAgents;
	}

	public void sendGetTaskAssigmentMessage() {

	}

	public Path getPathToBlock(Blockade blockade) {
		Path path;
		EntityID id;
		Area destination;
		Point p, humanPoint;
		Human human;

		human = getMeAsHuman();
		id = blockade.getPosition();
		destination = getModel().getEntity(id, Area.class);
		humanPoint = PositionLocate.getPosition(human, getModel());
		p = RoadUtilities.closestPointToBlock(blockade, humanPoint.x,
				humanPoint.y);
		path = getSearch().getPath(human, destination, p, PathType.Shortest);
		return path;
	}

	public GroupingType getGroupingType() {
		return GroupingType.getGroupingType(me().getStandardURN());
	}

	public abstract List<MessageType> getMessagesToListen();

	public int[] getChannelsToSubscribe() {
		List<Integer> channelList;
		int[] channels;
		int max;

		max = config.getIntValue(MAX_PLATOON_CHANNELS_KEY);
		channelList = new ArrayList<Integer>();
		for (MessageType type : getMessagesToListen()) {
			if (channelList.size() >= max) {
				break;
			}
			int channel;
			channel = sayMessageTransfer.getChannel(type);
			if (!channelList.contains(channel)) {
				channelList.add(channel);
			}
		}
		channels = new int[channelList.size()];
		for (int i = 0; i < channels.length; i++) {
			channels[i] = channelList.get(i);
		}
		return channels;
	}

	public List<Building> getBurningBuildingsFromChangeSet() {
		return getBurningBuildingsFromChangeSet(true, false);
	}

	public List<Building> getBurningBuildingsFromChangeSet(boolean sort,
			boolean noInfono) {
		List<Building> buildings;

		buildings = burningBuildingsInChangeSet;
		if (noInfono) {
			buildings = removeInfono(buildings);
		}
		if (sort) {
			Collections.sort(buildings, new DistanceComparator(location(),
					worldmodel));
		}
		return buildings;
	}

	// 移出没有着火的房屋
	public List<Building> removeInfono(List<Building> buildings) {
		List<Building> list;

		list = new ArrayList<Building>();
		for (Building building : buildings) {
			if (building.isFierynessDefined()) {
				list.add(building);
			} else {
				if (building.getFierynessEnum() != Fieryness.INFERNO) {
					list.add(building);
				}
			}
		}
		return list;
	}

	private boolean isCivilianCommand(AKSpeak command) {
		int channel;
		EntityID id;

		channel = command.getChannel();
		id = command.getAgentID();
		if (channel == 0) {
			StandardEntity entity;

			entity = model.getEntity(id);
			if (entity == null || entity instanceof Civilian) {
				return true;
			}
		}
		return false;
	}

	private void processCivilianCommand(AKSpeak speakCommand) {
	}

	protected void processBuildingIsBurningMessage(
			BuildingIsBurningMessage message) {
		EntityID buildingId = message.getBuildingId();

		try {
			Building building = (Building) worldmodel.getEntity(buildingId);
			if (building != null) {
				building.setFieryness(message.getFieryness());
				building.setTemperature(message.getTemperature());
			}
		} catch (Exception e) {
			System.err.println("There is No Such Building");
		}

	}

	protected void processBuildingIsExtinguishedMessage(
			BuildingIsExtinguishedMessage message) {
		EntityID buildingId = message.getBuildingId();

		Building building = null;

		try {
			building = (Building) worldmodel.getEntity(buildingId);
		} catch (Exception e) {
			System.err.println("There Is No Such Building");
		}

		if (building != null) {
			building.setFieryness(message.getFieryness());
		}
	}

	public List<Building> getObserveBuilding() {
		List<Building> building = new ArrayList<Building>();
		for (EntityID e : observableBuildings) {
			StandardEntity s = worldmodel.getEntity(e);
			building.add((Building) s);
		}
		return building;
	}

	public List<Human> getFoundAgents() {
		return foundAgents;
	}

	private void processBuildingHeat(Building building) {

		BuildingInfo buildingInfo;

		buildingInfo = worldmodel.getBuildingInfo(building.getID());
		if (buildingInfo.isHeating()) {
			if (!(building instanceof Refuge)) {
				heatingBuildings.add(building);
			}
		}
		buildingInfo.updateTemperature(timeStep);
	}

	public List<Building> getHeatingBuildings() {
		return heatingBuildings;
	}

	public List<Civilian> getCiviliansAround() {
		return civiliansAround;
	}

	public Road getEntranceRoad(Building building) {
		for (EntityID next : building.getNeighbours()) {
			StandardEntity entity = worldmodel.getEntity(next);
			if (entity instanceof Road) {
				return (Road) entity;
			}
		}
		return null;
	}

	public Blockade getClosestBlockadeInRoad(Road road) {
		Human human;
		List<Blockade> blockades;
		MultiBlockDistanceComparator comparator;
		Point position;
		Blockade target;

		human = getMeAsHuman();
		position = PositionLocate.getPosition(human, worldmodel);
		comparator = new MultiBlockDistanceComparator(getModel(), position);

		if (!road.isBlockadesDefined()) {
			return null;
		}
		blockades = getModel().getBlockades(road);
		if (blockades.isEmpty()) {
			return null;
		}
		Collections.sort(blockades, comparator);
		target = blockades.get(0);
		return target;
	}

	
}
