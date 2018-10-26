package learning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import corpus.LabeledInstance;
import corpus.SampledInstance;
import factors.FactorPool;
import sampling.IBeamSearchSampler;
import sampling.Initializer;
import sampling.Sampler;
import variables.AbstractState;

public class Trainer {

	public interface InstanceCallback {

		default public <InstanceT> void onStartInstance(Trainer caller, InstanceT instance, int indexOfInstance,
				int numberOfInstances, int epoch, int numberOfEpochs) {

		}

		public <InstanceT, StateT extends AbstractState<InstanceT>> void onEndInstance(Trainer caller,
				InstanceT instance, int indexOfInstance, StateT finalState, int numberOfInstances, int epoch,
				int numberOfEpochs);

	}

	public interface EpochCallback {

		default void onStartEpoch(Trainer caller, int epoch, int numberOfEpochs, int numberOfInstances) {
		}

		default void onEndEpoch(Trainer caller, int epoch, int numberOfEpochs, int numberOfInstances) {
		}
	}

	private static Logger log = LogManager.getFormatterLogger(Trainer.class.getName());

	/**
	 * This object is a basically a helper that iterates over data instances and
	 * triggers the generation of sampling chains for the provided documents. </br>
	 * The <b>train</b> function should be used for training while <b>test</b> and
	 * <b>predict</b> can be used to evaluate the trained model.
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
	 * This method iterates over the provided training instances and generates for
	 * each such instance a sampling chain using the <i>sampler</i> object. The
	 * chain is initialized by the <i>initializer</i> which creates an initial state
	 * based on the training instance. After each step in the sampling chain, the
	 * sampler notifies the <i>learner</i> to update the model w.r.t. the generated
	 * next states. The overall training iterates <i>numberOfEpochs</i> times over
	 * the training data. The final sampling state for each document is returned.
	 * 
	 * @param sampler
	 * @param initializer
	 * @param learner
	 * @param instances
	 * @param numberOfEpochs
	 * @param steps
	 * @return
	 */

	public boolean stopTraining = false;
	public Random random = new Random(100l);

	public <InstanceT, ResultT, StateT extends AbstractState<InstanceT>> List<SampledInstance<InstanceT, ResultT, StateT>> train(
			Sampler<StateT, ResultT> sampler, Initializer<InstanceT, StateT> initializer, Learner<StateT> learner,
			List<? extends LabeledInstance<InstanceT, ResultT>> instances, int numberOfEpochs) {

		FactorPool.getInstance().clear();

		List<SampledInstance<InstanceT, ResultT, StateT>> finalStates = new ArrayList<>();
		long startTime = System.currentTimeMillis();
		log.info("#Epochs=%s, #Instances=%s", numberOfEpochs, instances.size());
		for (int e = 1; e <= numberOfEpochs; e++) {
			if (stopTraining) {
				log.info("Stop training from outside call!");
				break;
			}
			log.info("##############################");
			log.info("Epoch: %s/%s", e, numberOfEpochs);
			log.info("##############################");
			for (EpochCallback c : epochCallbacks) {
				c.onStartEpoch(this, e, numberOfEpochs, instances.size());
			}

			Collections.shuffle(instances, new Random(random.nextLong()));
			for (int i = 0; i < instances.size(); i++) {
				InstanceT instance = instances.get(i).getInstance();
				ResultT goldResult = instances.get(i).getGoldAnnotation();
				log.info("===========TRAIN===========");
				log.info("Epoch: %s/%s; Instance: %s/%s", e, numberOfEpochs, i + 1, instances.size());
				log.info("Gold Result: %s", goldResult);
				log.info("Instance: %s", instance);
				log.info("===========================");
				for (InstanceCallback c : instanceCallbacks) {
					c.onStartInstance(this, instance, i, instances.size(), e, numberOfEpochs);
				}

				StateT initialState = initializer.getInitialState(instance);
				List<StateT> generatedChain = sampler.generateChain(initialState, goldResult, learner);
				StateT finalState = generatedChain.get(generatedChain.size() - 1);
				long stopTime = System.currentTimeMillis();

				log.info("++++++++++++++++");
				if (log.getLevel().equals(Level.DEBUG))
					log.debug("Gold Result: %s", goldResult);
				log.info("Final State:  %s", finalState);
				log.info("TrainingTime: %s (%s seconds)", (stopTime - startTime), (stopTime - startTime) / 1000);
				log.info("++++++++++++++++");
				/*
				 * Store the final predicted state for the current document if the current epoch
				 * is the final one.
				 */
				if (e == numberOfEpochs) {
					finalStates.add(new SampledInstance<InstanceT, ResultT, StateT>(instance, goldResult, finalState));
				}
				log.info("===========================");
				for (InstanceCallback c : instanceCallbacks) {
					c.onEndInstance(this, instance, i, finalState, instances.size(), e, numberOfEpochs);
				}
				finalState.resetFactorGraph();

			}
			log.info("##############################");
			for (EpochCallback c : epochCallbacks) {
				c.onEndEpoch(this, e, numberOfEpochs, instances.size());
			}
		}
		return finalStates;
	}

	/**
	 * This method iterates over the provided training instances and generates for
	 * each such instance a sampling chain using the <i>sampler</i> object. The
	 * chain is initialized by the <i>initializer</i> which creates an initial state
	 * based on the training instance. The final sampling state for each document is
	 * returned. This method differs from the <b>predict</b> only by a more detailed
	 * logging, since it has knowledge about the expected result for each document.
	 * 
	 * @param sampler
	 * @param initializer
	 * @param instances
	 * @param steps
	 * @return
	 */
	public <InstanceT, ResultT, StateT extends AbstractState<InstanceT>> List<SampledInstance<InstanceT, ResultT, StateT>> test(
			Sampler<StateT, ResultT> sampler, Initializer<InstanceT, StateT> initializer,
			List<? extends LabeledInstance<InstanceT, ResultT>> instances) {
		List<SampledInstance<InstanceT, ResultT, StateT>> finalStates = new ArrayList<>();
		for (int i = 0; i < instances.size(); i++) {
			InstanceT instance = instances.get(i).getInstance();
			ResultT goldResult = instances.get(i).getGoldAnnotation();
			log.info("===========TEST============");
			log.info("Document: %s/%s", i + 1, instances.size());
			log.info("Content   : %s", instance);
			log.info("Gold Result: %s", instances.get(i).getGoldAnnotation());
			log.info("===========================");
			for (InstanceCallback c : instanceCallbacks) {
				c.onStartInstance(this, instance, i, instances.size(), 1, 1);
			}

			StateT initialState = initializer.getInitialState(instance);
			List<StateT> generatedChain = sampler.generateChain(initialState);
			StateT finalState = generatedChain.get(generatedChain.size() - 1);

			finalState.getFactorGraph().clear();
			FactorPool.getInstance().clear();
			finalStates.add(new SampledInstance<InstanceT, ResultT, StateT>(instance, goldResult, finalState));
			log.info("++++++++++++++++");
			if (log.isDebugEnabled())
				log.info("Gold Result:   %s", goldResult);
			log.info("Final State:  %s", finalState);
			log.info("++++++++++++++++");
			log.info("===========================");
			for (InstanceCallback c : instanceCallbacks) {
				c.onEndInstance(this, instance, i, finalState, instances.size(), 1, 1);
			}
		}
		return finalStates;
	}

	/**
	 * This method iterates over the provided instances and generates for each such
	 * instance a sampling chain using the <i>sampler</i> object. The chain is
	 * initialized by the <i>initializer</i> which creates an initial state based on
	 * the training instance. The final sampling state for each document is
	 * returned.
	 * 
	 * @param sampler
	 * @param initializer
	 * @param instances
	 * @param steps
	 * @return
	 */
	public <InstanceT, StateT extends AbstractState<InstanceT>> List<StateT> predict(Sampler<StateT, ?> sampler,
			Initializer<InstanceT, StateT> initializer, List<InstanceT> instances) {

		List<StateT> finalStates = new ArrayList<>();
		for (int instance = 0; instance < instances.size(); instance++) {
			FactorPool.getInstance().clear();
			InstanceT document = instances.get(instance);
			log.info("===========================");
			log.info("Instance: %s/%s", instance + 1, instances.size());
			log.info("Content   : %s", document);
			log.info("===========================");
			StateT initialState = initializer.getInitialState(document);
			List<StateT> generatedChain = sampler.generateChain(initialState);
			StateT finalState = generatedChain.get(generatedChain.size() - 1);
			finalState.getFactorGraph().clear();
			finalStates.add(finalState);
			log.info("++++++++++++++++");
			log.info("Final State:  %s", finalState);
			log.info("++++++++++++++++");
		}
		return finalStates;
	}

	/*
	 * Not Used
	 */
	@Deprecated
	public <InstanceT, ResultT, StateT extends AbstractState<InstanceT>> List<SampledInstance<InstanceT, ResultT, StateT>> train(
			Sampler<StateT, ResultT> sampler, Initializer<InstanceT, StateT> initializer, Learner<StateT> learner,
			List<InstanceT> instances, Function<InstanceT, ResultT> getResult, int numberOfEpochs) {
		List<SampledInstance<InstanceT, ResultT, StateT>> finalStates = new ArrayList<>();
		long startTime = System.currentTimeMillis();
		log.info("#Epochs=%s, #Instances=%s", numberOfEpochs, instances.size());
		for (int e = 1; e <= numberOfEpochs; e++) {
			log.info("##############################");
			log.info("Epoch: %s/%s", e, numberOfEpochs);
			log.info("##############################");
			for (EpochCallback c : epochCallbacks) {
				c.onStartEpoch(this, e, numberOfEpochs, instances.size());
			}
			Collections.shuffle(instances, new Random(random.nextLong()));
			for (int i = 0; i < instances.size(); i++) {
				InstanceT instance = instances.get(i);
				ResultT goldResult = getResult.apply(instances.get(i));
				log.info("===========TRAIN===========");
				log.info("Epoch: %s/%s; Instance: %s/%s", e, numberOfEpochs, i + 1, instances.size());
				log.info("Gold Result: %s", goldResult);
				log.info("Instance: %s", instance);
				log.info("===========================");
				for (InstanceCallback c : instanceCallbacks) {
					c.onStartInstance(this, instance, i, instances.size(), e, numberOfEpochs);
				}

				StateT initialState = initializer.getInitialState(instance);
				List<StateT> generatedChain = sampler.generateChain(initialState, goldResult, learner);
				StateT finalState = generatedChain.get(generatedChain.size() - 1);
				long stopTime = System.currentTimeMillis();

				log.info("++++++++++++++++");
				if (log.isDebugEnabled())
					log.info("Gold Result:   %s", goldResult);
				log.info("Final State:  %s", finalState);
				log.info("TrainingTime: %s (%s seconds)", (stopTime - startTime), (stopTime - startTime) / 1000);
				log.info("++++++++++++++++");

				/*
				 * Store the final predicted state for the current document if the current epoch
				 * is the final one.
				 */
				if (e == numberOfEpochs) {
					finalStates.add(new SampledInstance<InstanceT, ResultT, StateT>(instance, goldResult, finalState));
				}
				log.info("===========================");
				for (InstanceCallback c : instanceCallbacks) {
					c.onEndInstance(this, instance, i, finalState, instances.size(), e, numberOfEpochs);
				}
				finalState.resetFactorGraph();
			}
			log.info("##############################");
			for (EpochCallback c : epochCallbacks) {
				c.onEndEpoch(this, e, numberOfEpochs, instances.size());
			}
		}
		return finalStates;
	}

	/*
	 * Not Used
	 */
	@Deprecated
	public <InstanceT, ResultT, StateT extends AbstractState<InstanceT>> List<SampledInstance<InstanceT, ResultT, StateT>> train(
			IBeamSearchSampler<StateT, ResultT> sampler, Initializer<InstanceT, StateT> initializer,
			Learner<StateT> learner, List<InstanceT> instances, Function<InstanceT, ResultT> getResult,
			int numberOfEpochs) {
		List<SampledInstance<InstanceT, ResultT, StateT>> finalStates = new ArrayList<>();
		long startTime = System.currentTimeMillis();
		log.info("#Epochs=%s, #Instances=%s", numberOfEpochs, instances.size());
		for (int e = 1; e < numberOfEpochs; e++) {
			log.info("##############################");
			log.info("Epoch: %s/%s", e, numberOfEpochs);
			log.info("##############################");
			for (EpochCallback c : epochCallbacks) {
				c.onStartEpoch(this, e, numberOfEpochs, instances.size());
			}
			Collections.shuffle(instances, new Random(random.nextLong()));
			for (int i = 0; i < instances.size(); i++) {
				InstanceT instance = instances.get(i);
				ResultT goldResult = getResult.apply(instances.get(i));
				log.info("===========TRAIN===========");
				log.info("Epoch: %s/%s; Instance: %s/%s", e, numberOfEpochs, i + 1, instances.size());
				log.info("Gold Result: %s", goldResult);
				log.info("Instance: %s", instance);
				log.info("===========================");
				for (InstanceCallback c : instanceCallbacks) {
					c.onStartInstance(this, instance, i, instances.size(), e, numberOfEpochs);
				}

				StateT initialState = initializer.getInitialState(instance);
				List<StateT> initialStates = new ArrayList<>();
				initialStates.add(initialState);
				List<List<StateT>> generatedChain = sampler.generateChain(initialStates, goldResult, learner);
				List<StateT> lastStepStates = generatedChain.get(generatedChain.size() - 1);
				/*
				 * Get the highest scoring state (by model score)
				 */
				StateT finalState = lastStepStates.stream()
						.max((s1, s2) -> Double.compare(s1.getModelScore(), s2.getModelScore())).get();

				long stopTime = System.currentTimeMillis();

				log.info("++++++++++++++++");
				if (log.isDebugEnabled())
					log.info("Gold Result:   %s", goldResult);
				log.info("Final State:  %s", finalState);
				log.info("TrainingTime: %s (%s seconds)", (stopTime - startTime), (stopTime - startTime) / 1000);
				log.info("++++++++++++++++");

				/*
				 * Store the final predicted state for the current document if the current epoch
				 * is the final one.
				 */
				if (e == numberOfEpochs) {
					finalStates.add(new SampledInstance<InstanceT, ResultT, StateT>(instance, goldResult, finalState));
				}
				log.info("===========================");
				for (InstanceCallback c : instanceCallbacks) {
					c.onEndInstance(this, instance, i, finalState, instances.size(), e, numberOfEpochs);
				}
				finalState.resetFactorGraph();
			}
			log.info("##############################");
			for (EpochCallback c : epochCallbacks) {
				c.onEndEpoch(this, e, numberOfEpochs, instances.size());
			}
		}
		return finalStates;
	}

	/*
	 * Not Used
	 */
	@Deprecated
	public <InstanceT, ResultT, StateT extends AbstractState<InstanceT>> List<SampledInstance<InstanceT, ResultT, StateT>> test(
			Sampler<StateT, ResultT> sampler, Initializer<InstanceT, StateT> initializer, List<InstanceT> instances,
			Function<InstanceT, ResultT> getResult) {

		List<SampledInstance<InstanceT, ResultT, StateT>> finalStates = new ArrayList<>();
		for (int i = 0; i < instances.size(); i++) {

			FactorPool.getInstance().clear();

			InstanceT instance = instances.get(i);
			ResultT goldResult = getResult.apply(instances.get(i));
			log.info("===========TEST============");
			log.info("Document: %s/%s", i + 1, instances.size());
			log.info("Content   : %s", instance);
			log.info("Gold Result: %s", goldResult);
			log.info("===========================");
			for (InstanceCallback c : instanceCallbacks) {
				c.onStartInstance(this, instance, i, instances.size(), 1, 1);
			}

			StateT initialState = initializer.getInitialState(instance);
			List<StateT> generatedChain = sampler.generateChain(initialState);
			StateT finalState = generatedChain.get(generatedChain.size() - 1);

			finalState.getFactorGraph().clear();
			finalStates.add(new SampledInstance<InstanceT, ResultT, StateT>(instance, goldResult, finalState));
			log.info("++++++++++++++++");
			if (log.isDebugEnabled())
				log.info("Gold Result:   %s", goldResult);
			log.info("Final State:  %s", finalState);
			log.info("++++++++++++++++");
			log.info("===========================");
			for (InstanceCallback c : instanceCallbacks) {
				c.onEndInstance(this, instance, i, finalState, instances.size(), 1, 1);
			}
		}
		return finalStates;
	}

	/*
	 * Not Used
	 */
	@Deprecated
	public <InstanceT, ResultT, StateT extends AbstractState<InstanceT>> List<SampledInstance<InstanceT, ResultT, StateT>> test(
			IBeamSearchSampler<StateT, ResultT> sampler, Initializer<InstanceT, StateT> initializer,
			List<InstanceT> instances, Function<InstanceT, ResultT> getResult) {
		List<SampledInstance<InstanceT, ResultT, StateT>> finalStates = new ArrayList<>();
		for (int i = 0; i < instances.size(); i++) {
			InstanceT instance = instances.get(i);
			ResultT goldResult = getResult.apply(instances.get(i));
			log.info("===========TEST============");
			log.info("Document: %s/%s", i + 1, instances.size());
			log.info("Content   : %s", instance);
			log.info("Gold Result: %s", goldResult);
			log.info("===========================");
			for (InstanceCallback c : instanceCallbacks) {
				c.onStartInstance(this, instance, i, instances.size(), 1, 1);
			}

			StateT initialState = initializer.getInitialState(instance);
			List<StateT> initialStates = new ArrayList<>();
			initialStates.add(initialState);
			List<List<StateT>> generatedChain = sampler.generateChain(initialStates);
			List<StateT> lastStepStates = generatedChain.get(generatedChain.size() - 1);
			/*
			 * Get the highest scoring state (by model score)
			 */
			StateT finalState = lastStepStates.stream()
					.max((s1, s2) -> Double.compare(s1.getModelScore(), s2.getModelScore())).get();

			finalState.getFactorGraph().clear();
			FactorPool.getInstance().clear();
			finalStates.add(new SampledInstance<InstanceT, ResultT, StateT>(instance, goldResult, finalState));
			log.info("++++++++++++++++");
			if (log.isDebugEnabled())
				log.info("Gold Result:   %s", goldResult);
			log.info("Final State:  %s", finalState);
			log.info("++++++++++++++++");
			log.info("===========================");
			for (InstanceCallback c : instanceCallbacks) {
				c.onEndInstance(this, instance, i, finalState, instances.size(), 1, 1);
			}
		}
		return finalStates;
	}

	public <InstanceT, ResultT, StateT extends AbstractState<InstanceT>> Map<InstanceT, List<StateT>> collectBestNStates(
			Sampler<StateT, ResultT> sampler, Initializer<InstanceT, StateT> initializer,
			List<? extends LabeledInstance<InstanceT, ResultT>> instances, final int N) {

		Map<InstanceT, List<StateT>> bestNStates = new HashMap<>();

		for (int i = 0; i < instances.size(); i++) {

			InstanceT instance = instances.get(i).getInstance();

			log.info("===========COLLECT BEST N STATES============");
			log.info("Document: %s/%s", i + 1, instances.size());
			log.info("Content   : %s", instance);
			log.info("Gold Result: %s", instances.get(i).getGoldAnnotation());
			log.info("===========================");

			StateT initialState = initializer.getInitialState(instance);
			List<StateT> collectedStates = sampler.collectBestNStates(initialState, N);

			bestNStates.put(instance, collectedStates);

			/**
			 * TODO: call??
			 */
//			finalState.getFactorGraph().clear();
			FactorPool.getInstance().clear();

			log.info("===========================");
		}
		return bestNStates;
	}

}
