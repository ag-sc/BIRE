package learning;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import corpus.Instance;
import corpus.LabeledInstance;
import learning.callbacks.InstanceCallback;
import learning.callbacks.EpochCallback;
import sampling.Initializer;
import sampling.Sampler;
import variables.AbstractState;

public class Trainer {

	private static Logger log = LogManager.getFormatterLogger(Trainer.class.getName());

	/**
	 * This object is a basically a helper that iterates over data instances and
	 * triggers the generation of sampling chains for the provided
	 * documents. </br>
	 * The <b>train</b> function should be used for training while <b>test</b>
	 * and <b>predict</b> can be used to evaluate the trained model.
	 */
	public Trainer() {
		super();
	}

	private List<InstanceCallback> instanceCallbacks = new ArrayList<>();
	private List<EpochCallback> epochCallbacks = new ArrayList<>();

	public List<InstanceCallback> getDocumentCallbacks() {
		return instanceCallbacks;
	}

	public void addInstanceCallbacks(List<InstanceCallback> instanceCallbacks) {
		this.instanceCallbacks.addAll(instanceCallbacks);
	}

	public void addInstanceCallback(InstanceCallback instanceCallback) {
		this.instanceCallbacks.add(instanceCallback);
	}

	public void removeInstanceCallback(InstanceCallback instanceCallback) {
		this.instanceCallbacks.remove(instanceCallback);
	}

	public List<EpochCallback> getEpochCallbacks() {
		return epochCallbacks;
	}

	public void addEpochCallbacks(List<EpochCallback> epochCallbacks) {
		this.epochCallbacks.addAll(epochCallbacks);
	}

	public void addEpochCallback(EpochCallback epochCallback) {
		this.epochCallbacks.add(epochCallback);
	}

	public void removeEpochCallback(EpochCallback epochCallback) {
		this.epochCallbacks.remove(epochCallback);
	}

	/**
	 * This method iterates over the provided training instances and generates
	 * for each such instance a sampling chain using the <i>sampler</i> object.
	 * The chain is initialized by the <i>initializer</i> which creates an
	 * initial state based on the training instance. After each step in the
	 * sampling chain, the sampler notifies the <i>learner</i> to update the
	 * model w.r.t. the generated next states. The overall training iterates
	 * <i>numberOfEpochs</i> times over the training data. The final sampling
	 * state for each document is returned.
	 * 
	 * @param sampler
	 * @param initializer
	 * @param learner
	 * @param instances
	 * @param numberOfEpochs
	 * @param steps
	 * @return
	 */
	public <StateT extends AbstractState, InstanceT extends LabeledInstance<ResultT>, ResultT> List<StateT> train(
			Sampler<StateT, ResultT> sampler, Initializer<? super InstanceT, StateT> initializer,
			Learner<StateT> learner, List<InstanceT> instances, int numberOfEpochs) {
		List<StateT> finalStates = new ArrayList<>();
		long startTime = System.currentTimeMillis();
		log.info("#Epochs=%s, #Instances=%s", numberOfEpochs, instances.size());
		for (int e = 0; e < numberOfEpochs; e++) {
			log.info("##############################");
			log.info("Epoch: %s/%s", e + 1, numberOfEpochs);
			log.info("##############################");
			for (EpochCallback c : epochCallbacks) {
				c.onStartEpoch(this, e, numberOfEpochs, instances.size());
			}

			for (int i = 0; i < instances.size(); i++) {
				InstanceT instance = instances.get(i);
				ResultT goldResult = instance.getGoldResult();
				log.info("===========TRAIN===========");
				log.info("Epoch: %s/%s; Instance: %s/%s", e + 1, numberOfEpochs, i + 1, instances.size());
				log.info("Content   : %s", instance);
				log.info("Gold Result: %s", goldResult);
				log.info("===========================");
				for (InstanceCallback c : instanceCallbacks) {
					c.onStartInstance(this, instance, i, instances.size(), e, numberOfEpochs);
				}

				StateT initialState = initializer.getInitialState(instance);
				List<StateT> generatedChain = sampler.generateChain(initialState, goldResult, learner);
				StateT finalState = generatedChain.get(generatedChain.size() - 1);
				long stopTime = System.currentTimeMillis();

				log.info("++++++++++++++++");
				log.info("Gold Result:   %s", goldResult);
				log.info("Final State:  %s", finalState);
				log.info("TrainingTime: %s (%s seconds)", (stopTime - startTime), (stopTime - startTime) / 1000);
				log.info("++++++++++++++++");

				/*
				 * Store the final predicted state for the current document if
				 * the current epoch is the final one.
				 */
				if (e == numberOfEpochs - 1) {
					finalStates.add(finalState);
				}
				log.info("===========================");
				for (InstanceCallback c : instanceCallbacks) {
					c.onEndInstance(this, instance, i, instances.size(), e, numberOfEpochs);
				}
			}
			log.info("##############################");
			for (EpochCallback c : epochCallbacks) {
				c.onEndEpoch(this, e, numberOfEpochs, instances.size());
			}
		}
		return finalStates;
	}

	/**
	 * This method iterates over the provided training instances and generates
	 * for each such instance a sampling chain using the <i>sampler</i> object.
	 * The chain is initialized by the <i>initializer</i> which creates an
	 * initial state based on the training instance. The final sampling state
	 * for each document is returned. This method differs from the
	 * <b>predict</b> only by a more detailed logging, since it has knowledge
	 * about the expected result for each document.
	 * 
	 * @param sampler
	 * @param initializer
	 * @param documents
	 * @param steps
	 * @return
	 */
	public <StateT extends AbstractState, InstanceT extends LabeledInstance<ResultT>, ResultT> List<StateT> test(
			Sampler<StateT, ResultT> sampler, Initializer<? super InstanceT, StateT> initializer,
			List<InstanceT> documents) {
		List<StateT> finalStates = new ArrayList<>();
		for (int d = 0; d < documents.size(); d++) {
			InstanceT document = documents.get(d);
			log.info("===========TEST============");
			log.info("Document: %s/%s", d + 1, documents.size());
			log.info("Content   : %s", document);
			log.info("Gold Result: %s", document.getGoldResult());
			log.info("===========================");
			StateT initialState = initializer.getInitialState(document);
			List<StateT> generatedChain = sampler.generateChain(initialState);
			StateT finalState = generatedChain.get(generatedChain.size() - 1);
			finalStates.add(finalState);
			log.info("++++++++++++++++");
			log.info("Gold Result:   %s", document.getGoldResult());
			log.info("Final State:  %s", finalState);
			log.info("++++++++++++++++");
		}
		return finalStates;
	}

	/**
	 * This method iterates over the provided instances and generates for each
	 * such instance a sampling chain using the <i>sampler</i> object. The chain
	 * is initialized by the <i>initializer</i> which creates an initial state
	 * based on the training instance. The final sampling state for each
	 * document is returned.
	 * 
	 * @param sampler
	 * @param initializer
	 * @param documents
	 * @param steps
	 * @return
	 */
	public <StateT extends AbstractState, InstanceT extends Instance> List<StateT> predict(Sampler<StateT, ?> sampler,
			Initializer<InstanceT, StateT> initializer, List<InstanceT> documents) {
		List<StateT> finalStates = new ArrayList<>();
		for (int d = 0; d < documents.size(); d++) {
			InstanceT document = documents.get(d);
			log.info("===========================");
			log.info("Document: %s/%s", d + 1, documents.size());
			log.info("Content   : %s", document);
			log.info("===========================");
			StateT initialState = initializer.getInitialState(document);
			List<StateT> generatedChain = sampler.generateChain(initialState);
			StateT finalState = generatedChain.get(generatedChain.size() - 1);
			finalStates.add(finalState);
			log.info("++++++++++++++++");
			log.info("Final State:  %s", finalState);
			log.info("++++++++++++++++");
		}
		return finalStates;
	}

}
