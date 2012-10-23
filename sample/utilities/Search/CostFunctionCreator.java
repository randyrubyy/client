package sample.utilities.Search;

import sample.object.SampleWorldModel;
import sample.utilities.Search.Node;

public class CostFunctionCreator {
	public static CostFunction<Node> getEmptyPathCostFunction(
			SampleWorldModel model) {
		WeightedCostFunction<Node> wcf;
		AdvancedBlockCostFunction bcf;
		ConstantCostFunction<Node> ccf;
		LengthCostFunction lcf;
		StuckAgentCostFunction sacf;
		double wbcf, wccf, wsacf, wlcf;

		bcf = new AdvancedBlockCostFunction(model);
		ccf = new ConstantCostFunction<Node>();
		lcf = new LengthCostFunction(model);
		sacf = new StuckAgentCostFunction(model);

		wbcf = 1;
		wccf = 1;
		wlcf = 2;
		wsacf = 10;
		wcf = new WeightedCostFunction<Node>();
		wcf.addFunction(bcf, wbcf);
		wcf.addFunction(ccf, wccf);
		wcf.addFunction(lcf, wlcf);
		wcf.addFunction(sacf, wsacf);

		return wcf;
	}

	public static CostFunction<Node> getCostFunction(PathType type,
			SampleWorldModel model) {
		CostFunction<Node> costFunction;
		switch (type) {
		case EmptyAndSafe:
			costFunction = getEmptyPathCostFunction(model);
			break;
		case Shortest:
			costFunction = new LengthCostFunction(model);
			break;
		case LowBlockRepair:
			costFunction = new BlockRepairCostFunction(model);
			break;
		case Blockless:
			costFunction = new AdvancedBlockCostFunction(model);
			break;
		default:
			costFunction = null;
			break;
		}
		return costFunction;
	}

}
