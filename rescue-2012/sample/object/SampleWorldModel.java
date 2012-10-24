package sample.object;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;
import sample.object.Building.BuildingInfo;
import sample.object.Partition.GroupingType;
import sample.object.Partition.PartitionModel;
import sample.object.Road.AreaInfo;
import sample.object.Road.RoadInfo;
import sample.object.Road.RoadModel;
import sample.utilities.EntityIdComparator;
import sample.utilities.DistanceUtilities;
import sample.utilities.Search.*;

public class SampleWorldModel extends StandardWorldModel {
	int avarageRoadWidth;

	RoadModel roadModel;
	// 分区
	PartitionModel partitionModel;
	Exploration<Building> buildingExploration;

	List<EntityID> team;
	List<Node> nodes;

	Map<EntityID, RoadInfo> roadInfoMap;
	Map<EntityID, AreaInfo> areaInfoMap;

	// 每一个builiding的ID和该房屋的信息相对应
	Map<EntityID, BuildingInfo> buildingInfoMap;

	// 计算地图的哈希值
	private Long hash = null;

	/**
	 * @return the hashValue
	 */
	public Long getHash() {
		if (null == hash)
			return hash();
		return hash;
	}

	// 根据Node产生哈希值
	private Long hash() {
		if (hash == null) {
			long sum = 0;
			for (Iterator<Node> i = nodes.iterator(); i.hasNext();) {
				Node node = i.next();
				if (Long.MAX_VALUE - sum <= node.getX()) {
					sum = 0;
				}
				sum += node.getX();

				if (Long.MAX_VALUE - sum <= node.getY()) {
					sum = 0;
				}
				sum += node.getY();
			}
			hash = new Long(sum);
		}

		// System.err.println("Hash of Map is = " + hash.longValue());

		return hash.longValue();
	}

	public SampleWorldModel() {
		super();

		team = new ArrayList<EntityID>();
		roadInfoMap = new HashMap<EntityID, RoadInfo>();
		areaInfoMap = new HashMap<EntityID, AreaInfo>();
		buildingInfoMap = new HashMap<EntityID, BuildingInfo>();
	}

	// 根据坐标得到NODE
	public Node getNode(int x, int y) {
		for (Iterator<Node> i = nodes.iterator(); i.hasNext();) {
			Node n = i.next();
			if (n.isNode(x, y)) {
				return n;
			}
		}
		throw new NullPointerException();
	}

	// 这里的计算会花费较长时间
	public void createNodeCaches() {
		// 初始化地图的Hash值
		hash = hash();

		// System.out.println("Nodes Size : " + nodes.size());

		// 相邻节点是否缓存
		boolean loaded = false;

		String str = "nodecache/";

		String fname = str + hash + ".nc";

		try {
			File f = new File(fname);

			BufferedReader br = new BufferedReader(new FileReader(f));

			String nl;

			while (null != (nl = br.readLine())) {

				int quantity = Integer.parseInt(nl);

				int x = Integer.parseInt(br.readLine());
				int y = Integer.parseInt(br.readLine());

				Node n = getNode(x, y);

				// System.out.println("N : " + n);

				Set<Node> neighbors;
				neighbors = new HashSet<Node>();

				for (int i = 0; i < quantity; i++) {
					int ox = Integer.parseInt(br.readLine());
					int oy = Integer.parseInt(br.readLine());

					neighbors.add(getNode(ox, oy));
				}

				// 一定要按照顺序读完文件，所以要放在上面
				if (null != n && null == n.neighbors) {

					n.neighbors = neighbors;

				}

			}

			loaded = true;

		} catch (Exception e) {

			// 文件不一致或者不存在都需要调用原方法进行计算
			// e.printStackTrace();

			for (Node node : nodes) {
				// 缓存相邻的节点
				node.getNeighbors(this);
			}
		}

		try {
			if (!loaded) {
				// 记录节点数据
				File f = new File(fname);
				f.createNewFile();

				BufferedWriter bw = new BufferedWriter(new FileWriter(f));

				for (Iterator<Node> i = nodes.iterator(); i.hasNext();) {
					Node n = i.next();

					bw.write(n.neighbors.size() + "\n");

					bw.write(n.getX() + "\n");
					bw.write(n.getY() + "\n");

					for (Iterator<Node> x = n.neighbors.iterator(); x.hasNext();) {
						Node y = x.next();

						bw.write(y.getX() + "\n");
						bw.write(y.getY() + "\n");
					}
				}
				bw.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 返回分区模型
	public PartitionModel getPartitionModel() {
		return partitionModel;
	}

	public void setRegionModel(PartitionModel pModel) {
		this.partitionModel = pModel;
	}

	public RoadModel getRoadModel() {
		return roadModel;
	}

	public Exploration<Building> getBuildingExploration() {
		return buildingExploration;
	}

	public void setRoadModel(RoadModel roadModel) {
		this.roadModel = roadModel;
	}

	public void initializeMapSearching(GroupingType type) {
		this.roadModel = new RoadModel(this);
		this.partitionModel = new PartitionModel(this, type);
		buildingExploration = new Exploration<Building>(this);
		initializeNodes();
	}

	private void initializeNodes() {
		NodeCreator factory;

		factory = new NodeCreator(this);
		factory.createNodes();
		nodes = factory.getNodes();
	}

	/**
	 * 返回指定类型的Standard Entity
	 * 
	 * @param <T>
	 * @param c
	 * @param urn
	 * @return 指定类型的Standard Entity
	 */
	public <T extends StandardEntity> List<T> getEntitiesOfType(Class<T> c,
			StandardEntityURN urn) {
		Collection<StandardEntity> entities = getEntitiesOfType(urn);
		List<T> list = new ArrayList<T>();
		for (StandardEntity entity : entities) {
			if (c.isInstance(entity)) {
				list.add(c.cast(entity));
			}
		}
		return list;
	}

	/**
	 * Returns the entrances of a given building.
	 * 
	 * @param building
	 * @return Entrance nodes.
	 */
	public List<Area> getEnterences(Building building) {
		List<EntityID> ids = building.getNeighbours();
		List<Area> enterences = new ArrayList<Area>();
		for (EntityID id : ids) {
			StandardEntity entity = getEntity(id);
			if (entity instanceof Area) {
				enterences.add((Area) entity);
			}
		}
		return enterences;
	}

	/**
	 * Returns the buildings of a given node.
	 * 
	 * @param node
	 * @return List of buildings.
	 */
	public List<Building> getBuildings(Area node) {
		List<EntityID> ids = node.getNeighbours();
		List<Building> buildings = new ArrayList<Building>();
		for (EntityID id : ids) {
			StandardEntity entity = getEntity(id);
			if (entity instanceof Building) {
				Building building = (Building) entity;
				buildings.add(building);
			}
		}
		return buildings;
	}

	/**
	 * Returns the nodes for a given road.
	 * 
	 * @param road
	 * @return List of nodes.
	 */
	public List<Area> getNodes(Road road) {
		List<EntityID> ids;

		ids = road.getNeighbours();
		List<Area> nodes = new ArrayList<Area>();
		for (EntityID id : ids) {
			StandardEntity entity = getEntity(id);
			if (entity instanceof Area) {
				nodes.add((Area) entity);
			}
		}
		return nodes;
	}

	/**
	 * Returns the buildings of a given road.
	 * 
	 * @param node
	 * @return List of buildings.
	 */
	public List<Building> getBuildings(Road road) {
		List<Area> nodes = getNodes(road);
		List<Building> buildings = new ArrayList<Building>();
		for (Area node : nodes) {

			buildings.addAll(getBuildings(node));
		}
		return buildings;
	}

	/**
	 * Returns the line object for given two node.
	 * 
	 * @param node1
	 * @param node2
	 * @return
	 */
	public Line getLine(Area node1, Area node2) {
		int x1 = node1.getX();
		int y1 = node1.getY();
		int x2 = node2.getX();
		int y2 = node2.getY();
		return new Line(x1, y1, x2, y2);
	}

	/**
	 * Adds given building to exploration history.
	 * 
	 * @param entity
	 *            Building entity.
	 */
	public void addToExplorationHistory(Building entity) {
		buildingExploration.addToExplorationHistory(entity);
	}

	public void addToTeam(EntityID entity) {
		if (!team.contains(entity)) {
			team.add(entity);
			EntityIdComparator comparator = new EntityIdComparator();
			Collections.sort(team, comparator);
		}
	}

	public List<EntityID> getTeam() {
		return team;
	}

	public Collection<StandardEntity> getHumans() {
		return getEntitiesOfType(StandardEntityURN.CIVILIAN,
				StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.POLICE_FORCE,
				StandardEntityURN.AMBULANCE_TEAM);
	}

	public List<Building> findNearBuildings(Building centerbuilding,
			int distance) {
		List<Building> result;
		Collection<StandardEntity> allObjects;
		int radius;

		Rectangle rect = centerbuilding.getShape().getBounds();
		radius = (int) (distance + rect.getWidth() + rect.getHeight());

		allObjects = getObjectsInRange(centerbuilding, radius);
		result = new ArrayList<Building>();
		for (StandardEntity next : allObjects) {
			if (next instanceof Building) {
				Building building;

				building = (Building) next;
				if (!building.equals(centerbuilding)) {
					if (DistanceUtilities.getDistance(centerbuilding, building) < distance) {
						result.add(building);
					}
				}
			}
		}
		return result;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public List<Node> getConnectedTerminalNodes(Area area) {
		AreaInfo info = null;

		try {
			info = getAreaInfo(area.getID());
		} catch (Exception e) {
			System.err.println("No Such Area");
		}
		return info.getTerminalNodes();
	}

	public List<Node> getNodesConnectedToAll(
			Collection<? extends StandardEntity> entities) {
		List<Node> nodes;

		nodes = new ArrayList<Node>();

		for (Node node : this.nodes) {
			if (node.isConnectedToAll(entities)) {
				nodes.add(node);
			}
		}
		return nodes;
	}

	public Set<Node> getConnectedTerminalNodes(
			Collection<? extends Area> entities) {
		Set<Node> nodes;

		nodes = new HashSet<Node>();
		for (Area area : entities) {
			nodes.addAll(getConnectedTerminalNodes(area));
		}
		return nodes;
	}

	public int distance(Pair<Integer, Integer> a, Pair<Integer, Integer> b) {
		return distance(a.first(), a.second(), b.first(), b.second());
	}

	public int distance(java.awt.Point p1, java.awt.Point p2) {
		return distance(p1.x, p1.y, p2.x, p2.y);
	}

	public int distance(int x1, int y1, int x2, int y2) {
		double dx = x1 - x2;
		double dy = y1 - y2;
		return (int) Math.hypot(dx, dy);
	}

	public List<Blockade> getBlockades(Road road) {
		List<EntityID> blockadeIds;
		List<Blockade> blockades;

		if (road.isBlockadesDefined()) {
			blockadeIds = road.getBlockades();
			blockades = new ArrayList<Blockade>();
			for (EntityID id : blockadeIds) {
				Blockade blockade;

				blockade = getEntity(id, Blockade.class);
				blockades.add(blockade);
			}
			return blockades;
		} else {
			return null;
		}
	}

	public <T extends StandardEntity> T getEntity(EntityID id, Class<T> c) {
		StandardEntity entity;

		entity = getEntity(id);
		if (c.isInstance(entity)) {
			T castedEntity;

			castedEntity = c.cast(entity);
			return castedEntity;
		} else {
			return null;
		}
	}

	public RoadInfo getRoadInfo(EntityID id) {
		RoadInfo info;

		info = roadInfoMap.get(id);
		if (info == null) {
			Road road;

			road = getEntity(id, Road.class);
			if (road != null) {
				info = new RoadInfo(road);
				roadInfoMap.put(id, info);
			}
		}
		return info;
	}

	public int getAverageRoadWidth() {
		List<Road> roads;
		int totalWidth, n;

		if (avarageRoadWidth == 0) {
			roads = getEntitiesOfType(Road.class, StandardEntityURN.ROAD);
			totalWidth = 0;
			n = 0;
			for (Road road : roads) {
				int width;
				RoadInfo info;

				info = getRoadInfo(road.getID());
				width = info.getWidth();
				if (width > 0) {
					totalWidth += width;
					n += 1;
				}
			}
			avarageRoadWidth = totalWidth / n;
		}
		return avarageRoadWidth;
	}

	public int getMinimumMainRoadWidth() {
		int width, average;
		double r;

		r = 1.5;
		average = getAverageRoadWidth();
		width = (int) (average * r);
		return width;
	}

	// 可变参数，任意数量的 StandareEntityURN 类型均可以被接受
	@SuppressWarnings("unused")
	public void addTeamMembers(StandardEntityURN... urn) {
		Collection<StandardEntity> teamMembers = getEntitiesOfType(urn);
		for (StandardEntity next : teamMembers) {
			Human agent = (Human) next;
			EntityID member = next.getID();
			addToTeam(member);
			// System.out.println(member.getValue() + " " +
			// agent.getPosition().getValue());
		}

		// System.out.println( " Team member size " + teamMembers.size());
	}

	public int getTotalBurningArea(Building building) {
		int totalBurningArea = building.getTotalArea();
		BuildingInfo buildingInfo = new BuildingInfo(building);
		List<Building> neighbours = buildingInfo.getNeighbours(this);
		for (Building next : neighbours) {
			if (next.isOnFire()) {
				totalBurningArea += next.getTotalArea();

			}
		}

		return totalBurningArea;
	}

	public AreaInfo getAreaInfo(EntityID id) {
		AreaInfo info;

		info = areaInfoMap.get(id);
		if (info == null) {
			Area area;

			area = getEntity(id, Area.class);
			if (area != null) {
				info = new AreaInfo(area);
				areaInfoMap.put(id, info);
			}
		}
		return info;
	}

	public BuildingInfo getBuildingInfo(EntityID id) {
		BuildingInfo info;

		info = buildingInfoMap.get(id);
		if (info == null) {
			Building building;

			building = getEntity(id, Building.class);
			if (building != null) {
				info = new BuildingInfo(building);
				buildingInfoMap.put(id, info);
			}
		}
		return info;
	}
}
