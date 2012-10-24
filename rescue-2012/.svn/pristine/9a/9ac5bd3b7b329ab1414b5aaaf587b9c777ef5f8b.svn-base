package sample.utilities.Search;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import sample.object.SampleWorldModel;
import sample.utilities.Path;
import sample.utilities.Search.CostFunction;
import sample.utilities.Search.CostFunctionCreator;
import sample.utilities.Search.PathType;
import sample.utilities.Search.Node;

/**
 * 
 * @author Knight
 */
public abstract class AbstractSearch {
	protected CostFunction<Node> costFunction;
	protected SampleWorldModel model;

	protected Path getPath(Area start, Point startPoint,
			Collection<? extends Area> goals, Point toPoint) {
		return getPath(start, startPoint, goals, toPoint, 0);
	}

	protected abstract Path getPath(Area start, Point startPoint,
			Collection<? extends Area> goals, Point toPoint, int limit);

	public abstract Collection<StandardEntity> findNeighbours(StandardEntity e);

	public Path getPath(Area start, Point startPoint,
			Collection<? extends Area> goals, PathType pathType) {
		return getPath(start, startPoint, goals, null, pathType);
	}

	public Path getPath(Area start, Point startPoint,
			Collection<? extends Area> goals, Point goalPoint, PathType pathType) {
		return getPath(start, startPoint, goals, goalPoint, pathType, 0);
	}

	public Path getPath(Area start, Point startPoint,
			Collection<? extends Area> goals, Point goalPoint,
			PathType pathType, int limit) {
		CostFunction<Node> costFunction;
		Path path;

		costFunction = CostFunctionCreator.getCostFunction(pathType, model);
		this.costFunction = costFunction;
		path = getPath(start, startPoint, goals, goalPoint);
		return path;
	}

	public Path getPath(Area start, Point startPoint, Area goal,
			Point goalPoint, PathType pathType) {
		Collection<Area> goals;
		Path path;

		goals = new ArrayList<Area>();
		goals.add(goal);
		path = getPath(start, startPoint, goals, goalPoint, pathType);
		return path;
	}

	public Path getPath(Area start, Point startPoint, Area goal,
			PathType pathType) {
		Path path;

		path = getPath(start, startPoint, goal, null, pathType);
		return path;
	}

	public Path getPath(Area start, Point startPoint, Human target,
			PathType pathType) {
		return getPath(start, startPoint, target, pathType, 0);
	}

	public Path getPath(Area start, Point startPoint, Human target,
			PathType pathType, int limit) {
		Collection<Area> goals;
		Area goal;
		Path path;
		Point endPoint;
		Pair<Integer, Integer> pair;

		goal = (Area) target.getPosition(model);
		pair = target.getLocation(model);
		if (pair == null) {
			endPoint = null;
		} else {
			endPoint = new Point(pair.first(), pair.second());
		}
		goals = new ArrayList<Area>();
		goals.add(goal);
		path = getPath(start, startPoint, goals, endPoint, pathType, limit);
		return path;
	}

	public Path getPath(Human agent, Area goal, PathType pathType) {
		Path path;

		path = getPath(agent, goal, null, pathType);
		return path;
	}

	public Path getPath(Human agent, Area goal, Point goalPoint,
			PathType pathType) {
		Path path;
		Area start;
		Point startPoint;
		Pair<Integer, Integer> pair;

		start = (Area) agent.getPosition(model);
		pair = agent.getLocation(model);
		startPoint = new Point(pair.first(), pair.second());
		path = getPath(start, startPoint, goal, goalPoint, pathType);
		return path;
	}

	public Path getPathFromHuman(Human agent, Human target, PathType pathType,
			int limit) {
		Path path;
		Area start;
		Point startPoint;
		Pair<Integer, Integer> pair;

		start = (Area) agent.getPosition(model);
		pair = agent.getLocation(model);
		startPoint = new Point(pair.first(), pair.second());
		path = getPath(start, startPoint, target, pathType);
		return path;
	}

	public Path getPath(Human agent, Human target, PathType pathType) {
		Path path;
		Area start;
		Point startPoint;
		Pair<Integer, Integer> pair;

		start = (Area) agent.getPosition(model);
		pair = agent.getLocation(model);
		startPoint = new Point(pair.first(), pair.second());
		path = getPath(start, startPoint, target, pathType);
		return path;
	}

	public Path getPath(Human agent, Collection<? extends Area> goals,
			PathType pathType) {
		Path path;
		Area start;
		Point startPoint;
		Pair<Integer, Integer> pair;

		start = (Area) agent.getPosition(model);
		pair = agent.getLocation(model);
		startPoint = new Point(pair.first(), pair.second());
		path = getPath(start, startPoint, goals, pathType);
		return path;
	}

}
