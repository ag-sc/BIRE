package evaluation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import Corpus.Document;
import Learning.Score;
import Sampling.Sampler;
import Variables.State;

public class SamplingProcedureRecord implements Serializable {

	public int numberOfSteps;
	public int numberOfDocuments;
	public int numberOfSamplers;
	public double alpha;
	public boolean sampleNextState;
	public List<SamplingStepRecord>[][] samplingSteps;

	public SamplingProcedureRecord(int numberOfDocuments, int numberOfSteps,
			int numberOfSamplers, double alpha) {
		super();
		this.numberOfDocuments = numberOfDocuments;
		this.numberOfSteps = numberOfSteps;
		this.numberOfSamplers = numberOfSamplers;
		this.alpha = alpha;
		this.samplingSteps = new List[numberOfDocuments][numberOfSteps];
		for (int d = 0; d < numberOfDocuments; d++) {
			for (int s = 0; s < numberOfSteps; s++) {
				this.samplingSteps[d][s] = new ArrayList<SamplingStepRecord>();
			}
		}
	}

	public void recordSamplingStep(Document document, int d, int s, Sampler sampler,
			List<State> nextStates, State currentState) {
		SamplingStepRecord record = new SamplingStepRecord();
		record.document = document;
		record.documentIndex = d;
		record.samplingStep = s;
		record.samplerClass = sampler.getClass();
		record.acceptedState = new StateRecord(currentState.getModelScore(),
				currentState.getObjectiveFunctionScore());
		record.generatedStates = new ArrayList<StateRecord>();
		for (State state : nextStates) {
			record.generatedStates.add(new StateRecord(state.getModelScore(),
					state.getObjectiveFunctionScore()));
		}
		samplingSteps[d][s].add(record);
	}

	public class SamplingStepRecord implements Serializable {
		public int documentIndex;
		public int samplingStep;
		public Class<? extends Sampler> samplerClass;
		public List<StateRecord> generatedStates;
		public StateRecord acceptedState;
		public Document document;
	}

	public class StateRecord implements Serializable {
		public double modelScore;
		public Score objectiveFunctionScore;

		public StateRecord(double modelScore, Score objectiveFunctionScore) {
			super();
			this.modelScore = modelScore;
			this.objectiveFunctionScore = objectiveFunctionScore;
		}

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("SamplingProcedureRecord");
		builder.append("numberOfDocuments=");
		builder.append(numberOfDocuments);
		builder.append(", numberOfSteps=");
		builder.append(numberOfSteps);
		builder.append(", numberOfSamplers=");
		builder.append(numberOfSamplers);
		builder.append(", alpha=" + alpha);
		builder.append(", sampleNextState=");
		builder.append(sampleNextState);

		for (int d = 0; d < numberOfDocuments; d++) {
			for (int s = 0; s < numberOfSteps; s++) {
				for (SamplingStepRecord r : samplingSteps[d][s]) {
					builder.append("\ndocument: ");
					builder.append(r.documentIndex);
					builder.append(", sampling step: ");
					builder.append(r.samplingStep);
					builder.append(": ");
					builder.append(r.samplerClass);
					builder.append("; accepted:\tmodel score: ");
					builder.append(r.acceptedState.modelScore);
					builder.append(", objective fucntion score: ");
					builder.append(r.acceptedState.objectiveFunctionScore);

				}
			}
		}
		return builder.toString();
	}

	public String toDetailedString() {
		StringBuilder builder = new StringBuilder("SamplingProcedureRecord");
		builder.append("numberOfDocuments=");
		builder.append(numberOfDocuments);
		builder.append(", numberOfSteps=");
		builder.append(numberOfSteps);
		builder.append(", numberOfSamplers=");
		builder.append(numberOfSamplers);
		builder.append(", alpha=" + alpha);
		builder.append(", sampleNextState=");
		builder.append(sampleNextState);

		for (int d = 0; d < numberOfDocuments; d++) {
			for (int s = 0; s < numberOfSteps; s++) {
				for (SamplingStepRecord r : samplingSteps[d][s]) {
					builder.append("\ndocument: ");
					builder.append(r.documentIndex);
					builder.append(", sampling step: ");
					builder.append(r.samplingStep);
					builder.append(": ");
					builder.append(r.samplerClass);
					for (StateRecord g : r.generatedStates) {
						builder.append("\nmodel score: ");
						builder.append(g.modelScore);
						builder.append(", objective fucntion score: ");
						builder.append(g.objectiveFunctionScore);
					}
					builder.append("\naccepted: ");
					builder.append("\nmodel score: ");
					builder.append(r.acceptedState.modelScore);
					builder.append(", objective fucntion score: ");
					builder.append(r.acceptedState.objectiveFunctionScore);
				}
			}
		}
		return builder.toString();
	}
}