package sampling;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import corpus.AnnotatedDocument;
import learning.Learner;
import variables.AbstractState;

public class MultiSampler<StateT extends AbstractState> extends Sampler<StateT> {

	private static Logger log = LogManager.getFormatterLogger(MultiSampler.class.getName());
	private List<AbstractSampler<StateT>> samplers;

	public MultiSampler(List<AbstractSampler<StateT>> samplers) {
		super();
		this.samplers = samplers;
	}

	@Override
	public List<StateT> generateChain(AnnotatedDocument<StateT> document, Learner<StateT> learner, int steps) {
		List<StateT> generatedChain = new ArrayList<>();
		StateT goldState = document.getGoldState();
		StateT currentState = generateInitialState(document);

		for (int s = 0; s < steps; s++) {
			log.info("---------------------------");
			for (AbstractSampler<StateT> sampler : samplers) {
				log.info("...............");
				log.info("Step: %s/%s; Sampler: %s", s + 1, steps, sampler.getClass().getSimpleName());
				// log.info("...............");
				generatedChain.add(currentState = sampler.performStep(learner, goldState, currentState));
				log.info("Sampled State:  %s", currentState);
				// log.info("...............");
			}
			// log.info("---------------------------");
		}
		return generatedChain;
	}

	@Override
	public List<StateT> generateChain(AnnotatedDocument<StateT> document, int steps) {
		return generateChain(document, null, steps);
	}

}
