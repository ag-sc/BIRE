package sampling.samplingstrategies;

import sampling.SamplingUtils;
import variables.AbstractState;

public class SamplingStrategies {
	public static <StateT extends AbstractState> SamplingStrategy<StateT> greedyStrategy() {
		return candidates -> {
			return candidates.stream().max((s1, s2) -> Double.compare(s1.getModelScore(), s2.getModelScore())).get();
		};
	};

	public static <StateT extends AbstractState> SamplingStrategy<StateT> greedyObjectiveStrategy() {
		return candidates -> {
			return candidates.stream().max((s1, s2) -> Double.compare(s1.getObjectiveScore(), s2.getObjectiveScore()))
					.get();
		};
	}

	public static <StateT extends AbstractState> SamplingStrategy<StateT> topKUniformSamplingStrategy(int k) {
		return candidates -> {
			candidates.sort(AbstractState.modelScoreComparator);
			return SamplingUtils.drawRandomElement(candidates.subList(0, k));
		};
	}

	public static <StateT extends AbstractState> SamplingStrategy<StateT> topKModelSamplingStrategy(int k) {
		return candidates -> {
			candidates.sort(AbstractState.modelScoreComparator);
			return SamplingUtils.drawFromDistribution(candidates.subList(0, k), true, false);
		};
	}

	public static <StateT extends AbstractState> SamplingStrategy<StateT> linearSamplingStrategy() {
		return candidates -> SamplingUtils.drawFromDistribution(candidates, true, false);
	}

	public static <StateT extends AbstractState> SamplingStrategy<StateT> linearSamplingObjectiveStrategy() {
		return candidates -> SamplingUtils.drawFromDistribution(candidates, false, false);
	}

	public static <StateT extends AbstractState> SamplingStrategy<StateT> softmaxSamplingStrategy() {
		return candidates -> SamplingUtils.drawFromDistribution(candidates, true, true);
	}

}
