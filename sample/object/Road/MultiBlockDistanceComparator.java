package sample.object.Road;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import rescuecore2.standard.entities.Blockade;
import sample.object.SampleWorldModel;
import sample.utilities.DistanceUtilities;

/**
 * A comparator that sorts entities by distance to a reference point.
 */
public class MultiBlockDistanceComparator implements Comparator<Blockade> {

	private Collection<Point> references;

	/**
	 * Create a DistanceSorter.
	 * 
	 * @param reference
	 *            The reference point to measure distances from.
	 * @param world
	 *            The world model.
	 */
	public MultiBlockDistanceComparator(SampleWorldModel world,
			Point... references) {
		ArrayList<Point> points;

		points = new ArrayList<Point>();
		for (Point p : references) {
			points.add(p);
		}
		this.references = points;
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
	public int compare(Blockade a, Blockade b) {
		int d1, d2;

		d1 = 0;
		d2 = 0;
		for (Point reference : references) {
			d1 += DistanceUtilities.getDistanceToBlock(a, reference);
			d2 += DistanceUtilities.getDistanceToBlock(b, reference);
		}
		return d1 - d2;
	}
}
