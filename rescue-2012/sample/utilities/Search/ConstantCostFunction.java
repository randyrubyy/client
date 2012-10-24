package sample.utilities.Search;


public class ConstantCostFunction<T> extends CostFunction<T> {

	public double getCost(T current, T next) {
		return getMaxCost();
	}

	@Override
	public double getHeuristic(T node, T goal) {
		boolean same;
		double cost;

		same = node.equals(goal);
		cost = same ? 0 : getMaxCost();
		return cost;
	}
}
