package sample.utilities;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;

import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Edge;

public class DistanceUtilities {
	public static int getCenterDistance(Edge e1, Edge e2) {
		Point p1, p2;
		int d;

		p1 = PositionLocate.getCenter(e1);
		p2 = PositionLocate.getCenter(e2);
		d = getDistance(p1, p2);
		return d;
	}

	public static int getDistance(Edge e1, Edge e2) {
		double d1, d2, d3, d4;
		int d;
		Line2D l1, l2;

		l1 = PositionLocate.getLine(e1);
		l2 = PositionLocate.getLine(e2);
		d1 = l1.ptSegDist(l2.getP1());
		d2 = l1.ptSegDist(l2.getP2());
		d3 = l2.ptSegDist(l1.getP1());
		d4 = l2.ptSegDist(l1.getP2());
		d = (int) Math.min(Math.min(d3, d4), Math.min(d1, d2));
		return d;
	}

	public static int getDistance(Point2D p1, Point2D p2) {
		double dx, dy;
		int d;

		dx = p1.getX() - p2.getX();
		dy = p1.getY() - p2.getY();
		d = (int) Math.hypot(dx, dy);
		return d;
	}

	public static int getDistanceToBlock(Blockade b, int x, int y) {
		List<rescuecore2.misc.geometry.Line2D> lines = GeometryTools2D
				.pointsToLines(
						GeometryTools2D.vertexArrayToPoints(b.getApexes()),
						true);
		double best = Double.MAX_VALUE;
		rescuecore2.misc.geometry.Point2D origin = new rescuecore2.misc.geometry.Point2D(
				x, y);
		for (rescuecore2.misc.geometry.Line2D next : lines) {
			rescuecore2.misc.geometry.Point2D closest = GeometryTools2D
					.getClosestPointOnSegment(next, origin);
			double d = GeometryTools2D.getDistance(origin, closest);
			// LOG.debug("Next line: " + next + ", closest point: " + closest +
			// ", distance: " + d);
			if (d < best) {
				best = d;
				// LOG.debug("New best distance");
			}

		}
		return (int) best;
	}

	public static int getDistanceToBlock(Blockade block, Point point) {
		int x, y, d;

		x = point.x;
		y = point.y;
		d = getDistanceToBlock(block, x, y);
		return d;
	}

	public static int getLength(Line2D line) {
		Point2D p1, p2;
		int d;

		p1 = line.getP1();
		p2 = line.getP2();
		d = getDistance(p1, p2);
		return d;
	}

	public static int getDistance(Building b1, Building b2) {
		int min;
		boolean first;

		first = true;
		min = 0;
		for (Edge edge : b1.getEdges()) {
			int d;

			d = getDistance(b2, edge);
			if (first || d < min) {
				min = d;
				first = false;
			}
		}
		return min;
	}

	public static int getDistance(Building building, Edge edge) {
		int min;
		boolean first;

		first = true;
		min = 0;
		for (Edge e : building.getEdges()) {
			int d;

			d = getDistance(e, edge);
			if (first || d < min) {
				min = d;
				first = false;
			}
		}
		return min;
	}

	public static int getDistanceToEdges(Point point, Building building) {
		int min;
		boolean first;

		first = true;
		min = 0;
		for (Edge e : building.getEdges()) {
			int d;

			d = getDistance(point, e);
			if (first || d < min) {
				min = d;
				first = false;
			}
		}
		return min;
	}

	public static int getDistance(Point point, Edge edge) {
		int d;
		Line2D l;

		l = PositionLocate.getLine(edge);
		d = (int) l.ptSegDist(point);
		return d;
	}
}
