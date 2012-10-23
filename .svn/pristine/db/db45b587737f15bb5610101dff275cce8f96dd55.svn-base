package sample.utilities.Search;

import java.util.Comparator;

public class AStarSearchNodeComparator implements Comparator<AStarSearchNode> {
	// public static final Log LOG = LogFactory
	// .getLog(AStarSearchNodeComparator.class);

	public static final int SCALE = 1000;
	public static final int HEURISTIC_WEIGHT = 5;

	public int compare(AStarSearchNode node1, AStarSearchNode node2) {
		double cost1, cost2;
		int difference;

		if (node1 == null || node2 == null) {
			// LOG.warn("Something is wrong");
		}

		cost1 = node1.getPathCost() + HEURISTIC_WEIGHT * node1.getHeuristic();
		cost2 = node2.getPathCost() + HEURISTIC_WEIGHT * node2.getHeuristic();
		difference = (int) ((cost1 - cost2) * SCALE);
		return difference;
	}
}
