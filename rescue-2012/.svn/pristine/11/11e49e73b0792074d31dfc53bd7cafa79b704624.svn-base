package sample.utilities;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.StandardEntity;
import sample.object.SampleWorldModel;

/**
 * A comparator that sorts entities by distance to a reference point.
 */
public class MultiDistanceComparator implements Comparator<StandardEntity> {

	private Collection<Point> references;
	private SampleWorldModel world;

	/**
	 * Create a DistanceSorter.
	 * 
	 * @param reference
	 *            The reference point to measure distances from.
	 * @param world
	 *            The world model.
	 */
	public MultiDistanceComparator(SampleWorldModel world, Point... references) {
		ArrayList<Point> points;

		points = new ArrayList<Point>();
		for (Point p : references) {
			points.add(p);
		}
		this.references = points;
		this.world = world;
	}

	/**
	 * Compares the standard entites according to distance.
	 * 
	 * @param a
	 *            First StandardEntity to compare
	 * @param b
	 *            Second StandardEntity to compare
	 * @return The difference between distances.
	 */

	@Override
	public int compare(StandardEntity a, StandardEntity b) {
		int d1, d2;
		Point p1, p2;
		Pair<Integer, Integer> pair;

		pair = a.getLocation(world);
		p1 = new Point(pair.first(), pair.second());
		pair = b.getLocation(world);
		p2 = new Point(pair.first(), pair.second());

		d1 = 0;
		d2 = 0;
		for (Point reference : references) {
			d1 += world.distance(reference, p1);
			d2 += world.distance(reference, p2);
		}
		return d1 - d2;
	}
}
