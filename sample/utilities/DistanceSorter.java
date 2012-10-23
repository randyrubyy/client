package sample.utilities;

import java.util.Comparator;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;

/**
 * A comparator that sorts entities by distance to a reference point.
 */
public class DistanceSorter implements Comparator<StandardEntity> {
	private final StandardEntity reference;
	private final StandardWorldModel world;

	/**
	 * Create a DistanceSorter.
	 * 
	 * @param reference
	 *            The reference point to measure distances from.
	 * @param world
	 *            The world model.
	 */
	public DistanceSorter(StandardEntity reference, StandardWorldModel world) {
		this.reference = reference;
		this.world = world;
	}

	@Override
	public int compare(StandardEntity a, StandardEntity b) {
		int d1 = world.getDistance(reference, a);
		int d2 = world.getDistance(reference, b);
		if (d1 == -1)
			d1 = Integer.MAX_VALUE;
		if (d2 == -1)
			d2 = Integer.MAX_VALUE;
		if (d1 == d2)
			return a.getID().getValue() - b.getID().getValue();
		return d1 - d2;
	}
}
