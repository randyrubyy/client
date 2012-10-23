package sample.utilities.Search;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;
import sample.object.SampleWorldModel;

public class Node {

	int id;
	public int hashcode;

	Point position;
	Set<EntityID> connectedAreas;

	public Set<Node> neighbors;

	public Node(Rectangle rectangle) {
		hashcode = hashCode();

		initialize(rectangle);
	}

	Node(Edge edge) {
		hashcode = hashCode();

		Point center;
		int x, y;

		x = (edge.getStartX() + edge.getEndX()) / 2;
		y = (edge.getStartY() + edge.getEndY()) / 2;

		center = new Point(x, y);
		initialize(center);
	}

	Node(int x, int y) {

		hashcode = hashCode();

		Point center;

		center = new Point(x, y);
		initialize(center);
	}

	private void initialize(Rectangle rectangle) {
		Point center;

		center = new Point((int) rectangle.getCenterX(), (int) rectangle
				.getCenterY());
		initialize(center);
	}

	private void initialize(Point point) {
		position = point;
		connectedAreas = new HashSet<EntityID>();
	}

	public int getX() {
		return position.x;
	}

	public int getY() {
		return position.y;
	}

	public void setConnectedAreas(Area... areas) {
		connectedAreas.clear();
		for (Area area : areas) {
			connectedAreas.add(area.getID());
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Node other = (Node) obj;
		if (this.id != other.id) {
			return false;
		}
		if (this.connectedAreas != other.connectedAreas
				&& (this.connectedAreas == null || !this.connectedAreas
						.equals(other.connectedAreas))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 59
				* hash
				+ (this.connectedAreas != null ? this.connectedAreas.hashCode()
						: 0);
		return hash;
	}

	public Set<Node> getNeighbors(SampleWorldModel model) {
		if (neighbors == null) {
			Set<Node> neighbors;

			neighbors = new HashSet<Node>();
			for (Node node : model.getNodes()) {
				if (node.equals(this)) {
					continue;
				}
				for (EntityID id : connectedAreas) {
					if (node.connectedAreas.contains(id)) {
						neighbors.add(node);
						break;
					}
				}
			}
			this.neighbors = neighbors;
		}
		return neighbors;
	}

	public boolean isConnected(EntityID id) {
		boolean connected;

		connected = connectedAreas.contains(id);
		return connected;
	}

	public boolean isConnected(StandardEntity entity) {
		if (entity == null) {
			return false;
		} else {
			EntityID id;

			id = entity.getID();
			return isConnected(id);
		}
	}

	public boolean isConnected(Collection<? extends StandardEntity> entities) {

		for (StandardEntity entity : entities) {
			if (isConnected(entity)) {
				return true;
			}
		}
		return false;
	}

	public boolean isConnectedToAll(
			Collection<? extends StandardEntity> entities) {
		for (StandardEntity entity : entities) {
			if (!isConnected(entity)) {
				return false;
			}
		}
		return true;
	}

	public Collection<EntityID> getDifferentConnections(Node node) {
		Set<EntityID> set;

		set = new HashSet<EntityID>(connectedAreas);
		set.removeAll(node.connectedAreas);
		return set;
	}

	public EntityID getDifferentConnection(Node node) {
		Collection<EntityID> ids;

		ids = getDifferentConnections(node);
		if (ids.isEmpty()) {
			return null;
		} else {
			EntityID id;

			id = ids.iterator().next();
			return id;
		}
	}

	public Collection<EntityID> getCommonConnections(Node node) {
		Set<EntityID> set;

		set = new HashSet<EntityID>();
		for (EntityID id : node.connectedAreas) {
			if (connectedAreas.contains(id)) {
				set.add(id);
			}
		}
		return set;
	}

	public EntityID getCommonConnection(Node node) {
		Collection<EntityID> ids;

		ids = getCommonConnections(node);
		if (ids.isEmpty()) {
			return null;
		} else {
			EntityID id;

			id = ids.iterator().next();
			return id;
		}
	}

	@Override
	public String toString() {
		return "Node(" + id + ")" + " - " + connectedAreas;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isTerminal() {
		boolean terminal;

		terminal = connectedAreas.size() <= 1;
		return terminal;
	}

	public Point getPosition() {
		return position;
	}

	public StandardEntity getConnectedEntity(SampleWorldModel model) {
		if (connectedAreas.size() >= 1) {
			EntityID id;
			StandardEntity entity;

			id = connectedAreas.iterator().next();
			entity = model.getEntity(id);
			return entity;
		} else {
			return null;
		}
	}

	public EntityID getConnectedEntityId() {
		if (connectedAreas.size() >= 1) {
			EntityID id;

			id = connectedAreas.iterator().next();
			return id;
		} else {
			return null;
		}
	}

	public Set<EntityID> getConnectedEntityIds() {
		return connectedAreas;
	}

	// 对于坐标相同的Node判断
	public boolean isNode(int x, int y) {

		return (getX() == x && getY() == y);
	}
}
