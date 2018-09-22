package sampling.samplingstrategies;

import java.util.List;

import sampling.SamplingUtils;
import variables.AbstractState;

public class SamplingStrategies {
	public static <StateT extends AbstractState<?>> SamplingStrategy<StateT> greedyModelStrategy() {
		return new SamplingStrategy<StateT>() {

			@Override
			public StateT sampleCandidate(List<StateT> candidates) {
				return candidates.stream().max((s1, s2) -> Double.compare(s1.getModelScore(), s2.getModelScore()))
						.get();
			}

			@Override
			public boolean usesModel() {
				return true;
			}

			@Override
			public boolean usesObjective() {
				return false;
			}
		};
	};

	public static <StateT extends AbstractState<?>> SamplingStrategy<StateT> greedyObjectiveStrategy() {
		return new SamplingStrategy<StateT>() {

			@Override
			public StateT sampleCandidate(List<StateT> candidates) {
				return candidates.stream()
						.max((s1, s2) -> Double.compare(s1.getObjectiveScore(), s2.getObjectiveScore())).get();
			}

			@Override
			public boolean usesModel() {
				return false;
			}

			@Override
			public boolean usesObjective() {
				return true;
			}
		};
	}

	public static <StateT extends AbstractState<?>> SamplingStrategy<StateT> topKUniformModelSamplingStrategy(int k) {
		return new SamplingStrategy<StateT>() {

			@Override
			public StateT sampleCandidate(List<StateT> candidates) {
				candidates.sort(AbstractState.modelScoreComparator);
				return SamplingUtils.drawRandomElement(candidates.subList(0, k));
			}

			@Override
			public boolean usesModel() {
				return true;
			}

			@Override
			public boolean usesObjective() {
				return false;
			}
		};
	}

	public static <StateT extends AbstractState<?>> SamplingStrategy<StateT> topKModelDistributionSamplingStrategy(
			int k) {
		return new SamplingStrategy<StateT>() {

			@Override
			public StateT sampleCandidate(List<StateT> candidates) {
				candidates.sort(AbstractState.modelScoreComparator);
				return SamplingUtils.drawFromDistribution(candidates.subList(0, k), true, false);
			}

			@Override
			public boolean usesModel() {
				return true;
			}

			@Override
			public boolean usesObjective() {
				return false;
			}
		};
	}

	public static <StateT extends AbstractState<?>> SamplingStrategy<StateT> linearModelSamplingStrategy() {
		return new SamplingStrategy<StateT>() {

			@Override
			public StateT sampleCandidate(List<StateT> candidates) {
				return SamplingUtils.drawFromDistribution(candidates, true, false);
			}

			@Override
			public boolean usesModel() {
				return true;
			}

			@Override
			public boolean usesObjective() {
				return false;
			}
		};
	}

	public static <StateT extends AbstractState<?>> SamplingStrategy<StateT> linearObjectiveSamplingStrategy() {
		return new SamplingStrategy<StateT>() {

			@Override
			public StateT sampleCandidate(List<StateT> candidates) {
				return SamplingUtils.drawFromDistribution(candidates, false, false);
			}

			@Override
			public boolean usesModel() {
				return false;
			}

			@Override
			public boolean usesObjective() {
				return true;
			}
		};
	}

	public static <StateT extends AbstractState<?>> SamplingStrategy<StateT> softmaxModelSamplingStrategy() {
		return new SamplingStrategy<StateT>() {

			@Override
			public StateT sampleCandidate(List<StateT> candidates) {
				return SamplingUtils.drawFromDistribution(candidates, true, true);
			}

			@Override
			public boolean usesModel() {
				return true;
			}

			@Override
			public boolean usesObjective() {
				return false;
			}
		};
	}

}
