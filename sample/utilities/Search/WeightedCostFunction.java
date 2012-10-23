package sample.utilities.Search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Knight
 */
public class WeightedCostFunction<T> extends CostFunction<T> {

	protected Map<CostFunction<T>, Double> weights;
	protected List<CostFunction<T>> functions;

	// cache for total weight
	protected double totalWeight;

	public WeightedCostFunction() {
		weights = new HashMap<CostFunction<T>, Double>();
		functions = new ArrayList<CostFunction<T>>();
		totalWeight = 0;
	}

	@Override
	public double getCost(T current, T next) {
		double sum;

		if (functions.isEmpty()) {
			return 0;
		}

		sum = 0;
		for (CostFunction<T> function : functions) {
			double w, cost, weightedCost;

			cost = function.getCost(current, next);
			w = weights.get(function) / totalWeight;
			weightedCost = w * cost;
			sum += weightedCost;
		}
		return sum;
	}

	public void addFunction(CostFunction<T> function, double weight) {
		functions.add(function);
		weights.put(function, weight);
		totalWeight += weight;
	}

	@Override
	public double getHeuristic(T node, T goal) {
		double sum;

		if (functions.isEmpty()) {
			return 0;
		}

		sum = 0;
		for (CostFunction<T> function : functions) {
			double w, cost, weightedCost;

			cost = function.getHeuristic(node, goal);
			w = weights.get(function) / totalWeight;
			weightedCost = w * cost;
			sum += weightedCost;
		}
		return sum;
	}

}
