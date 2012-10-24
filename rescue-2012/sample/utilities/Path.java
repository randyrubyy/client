package sample.utilities;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;
import sample.object.SampleWorldModel;

public class Path {

	private List<EntityID> entities;
	private double cost;
	protected Point target;
	boolean willHaveATarget;
	boolean limitExceeded;

	/**
	 * Contruct a Path object
	 * 
	 * @param entities
	 *            takes list of entity ids.
	 */
	public Path(List<EntityID> entities) {
		this.entities = entities;
	}

	public Path(EntityID entity) {
		entities = new ArrayList<EntityID>();
		entities.add(entity);
	}

	public Path() {
		this.entities = new ArrayList<EntityID>();
	}

	public void removeLastEntity() {
		if (entities == null) {
			return;
		}
		if (!entities.isEmpty()) {
			entities.remove(entities.size() - 1);
		}
	}

	public boolean isEmpty() {
		return entities == null || entities.isEmpty();
	}

	public boolean contains(EntityID id) {
		return entities.contains(id);
	}

	public boolean contains(StandardEntity entity) {
		return contains(entity.getID());
	}

	public EntityID next(EntityID id) {
		int i;

		i = entities.indexOf(id);
		if (i >= 0 && i < entities.size() - 1) {
			EntityID next;

			next = entities.get(i + 1);
			return next;
		}
		return null;
	}

	public EntityID previous(EntityID id) {
		int i;

		i = entities.indexOf(id);
		if (i > 0) {
			EntityID next;

			next = entities.get(i - 1);
			return next;
		}
		return null;
	}

	public boolean isUsedHeadToTail(Road road) {
		return true;
	}

	public void add(EntityID id) {
		entities.add(id);
	}

	public void add(int id) {
		add(new EntityID(id));
	}

	public int size() {
		return entities.size();
	}

	public EntityID getDestination() {
		if (isEmpty()) {
			return null;
		}
		EntityID id;

		id = entities.get(size() - 1);
		return id;
	}

	public Area getDestination(SampleWorldModel model) {
		EntityID id;

		id = getDestination();
		if (id == null) {
			return null;
		} else {
			return (Area) model.getEntity(id);
		}
	}

	public EntityID getStart() {
		EntityID id;
		id = entities.get(0);
		return id;
	}

	public List<EntityID> getEntities() {
		return entities;
	}

	@Override
	public String toString() {
		return entities.toString();
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public boolean hasInfiniteCost() {
		boolean infinite;

		infinite = Double.isInfinite(cost);
		return infinite;
	}

	public boolean isPassable() {
		boolean passable;

		passable = !entities.isEmpty();
		passable = passable && !hasInfiniteCost();
		passable = passable
				&& (entities.size() > 1 || target != null || willHaveATarget);
		return passable;
	}

	public Point getTarget() {
		return target;
	}

	public void setTarget(Point tar) {
		this.target = tar;
	}

	public void setWillHaveATarget(boolean willHaveATarget) {
		this.willHaveATarget = willHaveATarget;
	}

	public void setTargetToCenter(SampleWorldModel model) {
		StandardEntity destination;

		destination = getDestination(model);
		if (destination != null) {
			if (destination instanceof Area) {
				Area area;
				Point point;

				area = (Area) destination;
				point = new Point(area.getX(), area.getY());
				setTarget(point);
			}
		}
	}

	public Area getStart(SampleWorldModel model) {
		EntityID id;

		id = getStart();
		if (id == null) {
			return null;
		} else {
			return (Area) model.getEntity(id);
		}
	}

	public void setLimitExceeded(boolean exceeded) {
		limitExceeded = exceeded;
	}

	public boolean getLimitExceeded() {
		return limitExceeded;
	}
}
