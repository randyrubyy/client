package sample.object.Road;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Road;
import rescuecore2.worldmodel.EntityID;
import sample.agent.SampleAgent;
import sample.object.SampleWorldModel;
import sample.utilities.DistanceUtilities;
import sample.utilities.PositionLocate;

public class RoadInfo {

	protected int length;
	protected int width;
	protected Road road;
	protected List<Edge> passableEdges;
	protected int area;
	protected List<Edge> heads;
	protected List<Area> subareas;
	protected Map<Pair<Point, Point>, Boolean> blockedMap;

	public RoadInfo(Road road) {
		this.road = road;
		blockedMap = new HashMap<Pair<Point, Point>, Boolean>();
	}

	public List<Edge> calculatePassableEdges() {
		List<Edge> edges, passableEdges;

		if (road == null) {
			// LOG.warn("Road is not initialized");
			return null;
		}
		edges = road.getEdges();
		passableEdges = new ArrayList<Edge>();
		for (Edge edge : edges) {
			if (edge.isPassable()) {
				passableEdges.add(edge);
			}
		}
		return passableEdges;
	}

	public List<Edge> getPassableEdges() {
		if (passableEdges == null) {
			passableEdges = calculatePassableEdges();
		}
		return passableEdges;
	}

	public void calculateHeads() {
		int max, n;
		List<Edge> heads, passableEdges;
		Edge head, tail;

		if (road == null) {
			// LOG.warn("Road is not initialized");
			return;
		}
		passableEdges = getPassableEdges();
		n = passableEdges.size();
		if (n < 2) {
			// LOG.warn("Road has only " + n + " passable edges.");
			return;
		}
		max = 0;
		head = null;
		tail = null;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				Edge e1, e2;
				int d;

				e1 = passableEdges.get(i);
				e2 = passableEdges.get(j);
				d = DistanceUtilities.getCenterDistance(e1, e2);
				if (d > max) {
					head = e1;
					tail = e2;
					max = d;
				}
			}
		}
		heads = new ArrayList<Edge>();
		heads.add(head);
		heads.add(tail);
		this.heads = heads;
		length = max;
	}

	public int getLength() {
		if (length == 0) {
			calculateHeads();
		}
		return length;
	}

	public Edge getHead() {
		if (heads == null) {
			calculateHeads();
		}
		if (heads.size() > 0) {
			return heads.get(0);
		} else {
			// LOG.warn("Invalid road ");
			return null;
		}
	}

	public Edge getTail() {
		if (heads == null) {
			calculateHeads();
		}
		if (heads.size() > 1) {
			return heads.get(1);
		} else {
			// LOG.warn("Invalid road ");
			return null;
		}
	}

	public List<Edge> getHeads() {
		if (heads == null) {
			calculateHeads();
		}

		return heads;
	}

	public int calculateWidth() {
		int area, width, length;

		area = getArea();
		length = getLength();
		if (length == 0) {
			width = 0;
		} else {
			width = area / length;
		}
		return width;
	}

	public int getWidth() {
		if (width == 0) {
			width = calculateWidth();
		}
		return width;
	}

	public int getArea() {
		if (area == 0) {
			area = calculateArea();
		}
		return area;
	}

	public int calculateArea() {
		if (road == null) {
			// LOG.warn("Road is not initialized");
			return 0;
		}
		Point point;
		int area;

		point = null;
		area = 0;
		for (Edge edge : road.getEdges()) {
			if (point == null) {
				point = new Point(edge.getStartX(), edge.getStartY());
			} else {
				Line2D line;
				int d, length;

				line = PositionLocate.getLine(edge);
				d = (int) line.ptLineDist(point);
				length = DistanceUtilities.getLength(line);
				area += d * length / 2;
			}
		}
		return area;
	}

	public List<Area> calculateSubareas(SampleWorldModel model) {
		List<Area> areas;
		Area roadArea;

		roadArea = new Area(road.getShape());
		if (road.isBlockadesDefined()) {
			for (EntityID id : road.getBlockades()) {
				Blockade block;
				Shape shape;
				Area a;

				block = (Blockade) model.getEntity(id);
				shape = RoadUtilities.expandedBlockShape(block);
				a = new Area(shape);
				roadArea.subtract(a);
			}
		}
		areas = RoadUtilities.divide(roadArea);
		return areas;
	}

	public List<Area> getSubareas(SampleWorldModel model) {
		if (subareas == null) {
			subareas = calculateSubareas(model);
		}
		return subareas;
	}

	public void resetBlockadeCache() {
		subareas = null;
		blockedMap.clear();
	}

	public boolean calculateBlocked(Point p1, Point p2, SampleWorldModel model) {
		return calculateBlocked(p1, p2, (int) SampleAgent.AGENT_RADIUS, model);
	}

	public boolean calculateBlocked(Point p1, Point p2, int tolerance,
			SampleWorldModel model) {
		List<Area> roadParts;
		double s;
		Rectangle2D r1, r2;

		s = tolerance;
		r1 = new Rectangle2D.Double(p1.x - s, p1.y - s, 2 * s, 2 * s);
		r2 = new Rectangle2D.Double(p2.x - s, p2.y - s, 2 * s, 2 * s);

		roadParts = getSubareas(model);
		for (Area part : roadParts) {
			if (part.intersects(r1)) {
				if (part.intersects(r2)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean getBlocked(Point p1, Point p2, SampleWorldModel model) {
		Pair<Point, Point> pair1, pair2;
		boolean blocked;

		pair1 = new Pair<Point, Point>(p1, p2);
		if (blockedMap.containsKey(pair1)) {
			return blockedMap.get(pair1);
		}
		pair2 = new Pair<Point, Point>(p2, p1);
		if (blockedMap.containsKey(pair2)) {
			return blockedMap.get(pair2);
		}
		blocked = calculateBlocked(p1, p2, model);
		blockedMap.put(pair1, blocked);
		blockedMap.put(pair2, blocked);
		return blocked;
	}
}
