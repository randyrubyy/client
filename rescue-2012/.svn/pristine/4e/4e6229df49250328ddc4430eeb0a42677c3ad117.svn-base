package sample.utilities.Search;


public abstract class CostFunction<T> {

	public static final double DEFAULT_MAX_COST = 1.0;

	public double getMaxCost() {
		return DEFAULT_MAX_COST;
	}

	public abstract double getCost(T current, T next);

	public abstract double getHeuristic(T node, T goal);
}
