package sample.object.Road;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rescuecore2.misc.Pair;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;
import sample.agent.SampleAgent;
import sample.object.SampleWorldModel;
import sample.utilities.Search.Node;

public class RoadUtilities {

	public static final double RADIUS_GROW_RATIO = 2.0;

	// assumes a symmetrical road
	public static int getAvailableLaneCount(Road road) {
		if (road.isBlockadesDefined() && road.getBlockades().size() > 0) {
			return 0;
		} else {
			return 1;
		}
	}

	// 判断路是否被堵
	public static boolean isBlocked(Road road) {
		int availableLaneCount;
		boolean blocked;
		availableLaneCount = getAvailableLaneCount(road);
		blocked = availableLaneCount > 0;
		return blocked;
	}

	public static boolean isBlocked(rescuecore2.standard.entities.Area area,
			Point p1, Point p2, SampleWorldModel model) {
		Road road;
		RoadInfo info;
		boolean blocked;

		if (area instanceof Road) {
			road = (Road) area;
		} else {
			return false;
		}

		info = model.getRoadInfo(road.getID());
		blocked = info.getBlocked(p1, p2, model);
		return blocked;
	}

	public static boolean isBlocked(rescuecore2.standard.entities.Area area,
			Point p1, Point p2, int tolerance, SampleWorldModel model) {
		Road road;
		RoadInfo info;
		boolean blocked;

		if (area instanceof Road) {
			road = (Road) area;
		} else {
			return false;
		}

		info = model.getRoadInfo(road.getID());
		blocked = info.calculateBlocked(p1, p2, tolerance, model);
		return blocked;
	}

	public static PathBlockState getPathBlockState(Human human,
			rescuecore2.standard.entities.Area target, SampleWorldModel model) {
		rescuecore2.standard.entities.Area start;
		Pair<Integer, Integer> pair;
		Point p1, p2;
		Collection<Node> nodes;
		Collection<StandardEntity> entities;
		Node node;

		start = (rescuecore2.standard.entities.Area) human.getPosition(model);
		pair = human.getLocation(model);
		p1 = new Point(pair.first(), pair.second());
		entities = new ArrayList<StandardEntity>();
		entities.add(start);
		entities.add(target);
		nodes = model.getNodesConnectedToAll(entities);
		if (!nodes.isEmpty()) {
			node = nodes.iterator().next();
			p2 = node.getPosition();
			if (isBlocked(start, p1, p2, model)) {
				return PathBlockState.FirstPartIsBlocked;
			} else {
				if (target instanceof Road) {
					Road targetRoad;

					targetRoad = (Road) target;
					// pair = targetRoad.getLocation(model);
					// p1 = new Point(pair.first(), pair.second());

					if (isBlocked(targetRoad, p2, p2, model)) {
						return PathBlockState.SecondPartIsBlocked;
					} else {
						return PathBlockState.NotBlocked;
					}

				} else {
					return PathBlockState.NotBlocked;
				}
			}
		} else {
			// LOG.warn("Invalid arguments are sent to isBlocked function");
			return null;
		}
	}

	public static Rectangle boundingRectangle(Point p1, Point p2) {
		int x, y, xm, ym;
		Rectangle rect;

		x = Math.min(p1.x, p2.x);
		xm = Math.max(p1.x, p2.x);
		y = Math.min(p1.y, p2.y);
		ym = Math.max(p1.y, p2.y);
		rect = new Rectangle(x, xm, y, ym);
		return rect;
	}

	public static Shape expandedBlockShape(Blockade block) {
		double cx, cy, radius;
		int[] apexes;
		Blockade temp;
		Shape shape;

		radius = SampleAgent.AGENT_RADIUS * RADIUS_GROW_RATIO;
		apexes = block.getApexes();
		cx = block.getX();
		cy = block.getY();

		// Move each apex away from the centre
		for (int i = 0; i < apexes.length; i += 2) {
			double x, y, dx, dy, newX, newY, d, r;

			x = apexes[i];
			y = apexes[i + 1];
			dx = x - cx;
			dy = y - cy;
			d = Math.pow(dx * dx + dy * dy, 0.5);
			r = (d + radius) / d;

			// Shift both x and y so they are now d * dx from the centre
			newX = cx + (dx * r);
			newY = cy + (dy * r);
			apexes[i] = (int) newX;
			apexes[i + 1] = (int) newY;
		}

		temp = new Blockade(new EntityID(0));
		temp.setApexes(apexes);
		shape = temp.getShape();
		return shape;
	}

	public static List<Area> divide(Area area) {
		PathIterator pi;
		List<Area> areas;
		Path2D path;

		// LOG.trace("Dividing area: " + area);
		path = null;
		areas = new ArrayList<Area>();
		pi = area.getPathIterator(null);
		for (; !pi.isDone(); pi.next()) {
			int type;
			double[] v;

			v = new double[6];
			type = pi.currentSegment(v);
			switch (type) {
			case PathIterator.SEG_CLOSE:
				path.closePath();
				area = new Area(path);
				areas.add(area);
				break;
			case PathIterator.SEG_CUBICTO:
				path.curveTo(v[0], v[1], v[2], v[3], v[4], v[5]);
				break;
			case PathIterator.SEG_LINETO:
				path.lineTo(v[0], v[1]);
				break;
			case PathIterator.SEG_MOVETO:
				path = new Path2D.Double();
				path.moveTo(v[0], v[1]);
				break;
			case PathIterator.SEG_QUADTO:
				path.quadTo(v[0], v[1], v[2], v[3]);
				break;
			}
		}
		return areas;
	}

	public static List<Point> getNextPoints(Point p, int d) {
		List<Point> points;

		points = new ArrayList<Point>();
		points.add(new Point(p.x + d, p.y));
		points.add(new Point(p.x - d, p.y));
		points.add(new Point(p.x, p.y + d));
		points.add(new Point(p.x, p.y - d));
		points.add(new Point(p.x + d, p.y + d));
		points.add(new Point(p.x - d, p.y + d));
		points.add(new Point(p.x + d, p.y - d));
		points.add(new Point(p.x - d, p.y - d));
		return points;
	}

	public static Point getPointInside(Point point, Shape shape) {
		List<Point> nexts;

		nexts = getNextPoints(point, 1);
		// nexts.addAll(getNextPoints(point, 3000));
		if (shape.contains(point)) {
			return point;
		}
		for (Point p : nexts) {
			if (shape.contains(p)) {
				// LOG.warn("Fixed");
				return p;
			}
		}
		// LOG.fatal("Couldn't fix");
		return point;
	}

	public static Point closestPointToBlock(Blockade b, int x, int y) {
		// LOG.debug("Finding distance to " + b + " from " + x + ", " + y);
		List<Line2D> lines = GeometryTools2D.pointsToLines(GeometryTools2D
				.vertexArrayToPoints(b.getApexes()), true);
		double best = Double.MAX_VALUE;
		Point2D origin = new Point2D(x, y);
		Point2D bestPoint;
		Point p;

		bestPoint = null;
		for (Line2D next : lines) {
			Point2D closest = GeometryTools2D.getClosestPointOnSegment(next,
					origin);
			double d = GeometryTools2D.getDistance(origin, closest);
			// LOG.debug("Next line: " + next + ", closest point: " + closest +
			// ", distance: " + d);
			if (d < best) {
				best = d;
				bestPoint = closest;
				// LOG.debug("New best distance");
			}

		}
		p = new Point((int) bestPoint.getX(), (int) bestPoint.getY());
		return p;
	}

	public static boolean doesBlockExists(Blockade blockade, SampleWorldModel model) {
		Road road;
		EntityID roadId;

		if (blockade.isPositionDefined()) {
			boolean exists;
			roadId = blockade.getPosition();
			road = model.getEntity(roadId, Road.class);

			if (road.isBlockadesDefined()) {
				exists = road.getBlockades().contains(blockade.getID());
				return exists;
			} else {
				return false;
			}
		} else {
			return false;
		}

	}

}
