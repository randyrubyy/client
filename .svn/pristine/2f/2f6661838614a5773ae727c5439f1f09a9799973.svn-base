package sample.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rescuecore2.standard.entities.StandardEntity;
import sample.utilities.DistanceComparator;

public class Exploration<T extends StandardEntity> {
	private Set<T> explored;
	private List<T> notExplored;
	private SampleWorldModel model;

	/**
	 * Construct a explorationhistoy object.
	 * 
	 * @param model
	 */
	public Exploration(SampleWorldModel model) {
		this.model = model;
		explored = new HashSet<T>();
		notExplored = new ArrayList<T>();
	}

	public void addEntitiesToExplore(Collection<T> entities) {
		explored.removeAll(entities);
		notExplored.addAll(entities);
	}

	public void addEntitiesToExploreAndSort(Collection<T> entities,
			StandardEntity position) {
		explored.removeAll(entities);
		notExplored.addAll(entities);
		DistanceComparator sorter = new DistanceComparator(position, model);
		Collections.sort(notExplored, sorter);
	}

	/**
	 * Adds given building to explored list.
	 * 
	 * @param entity
	 *            Given enitty.
	 */
	public void addToExplorationHistory(T entity) {
		if (!explored.contains(entity)) {
			explored.add(entity);
		}
		if (notExplored.contains(entity)) {
			notExplored.remove(entity);
		}
	}

	public void removeFromUnexplored(Collection<T> entities) {
		notExplored.removeAll(entities);
	}

	public boolean isExploredBefore(T entity) {
		return explored.contains(entity);
	}

	public T getNextUnexploredEntity() {
		if (notExplored.isEmpty()) {
			return null;
		} else {
			return notExplored.get(0);
		}
	}

	public List<T> getUnexploredEntities() {
		return notExplored;
	}

	public boolean isExplorationCompeted() {
		return notExplored.isEmpty();
	}

	public boolean shouldExplore(T entity) {
		return notExplored.contains(entity);
	}
}
