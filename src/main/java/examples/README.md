**(Work In Progress)**

This description of the inner workings of the BIRE Framework is still in progress and incomplete.
However, we hope it is already helpful.


The main components of the BIRE framework are:
* **(Labeled)Instance**: The "observed" variables (e.g. a sentence or document).
* **State**: The "latent" variables for given observed variables (e.g. annotations for NER).
* **Model**: A collection of templates that store their respective model parameters.
* **Tempate**: A "factory" to create factors and compute features for a given state. Also contains the weights for each feature that are shared across its factors.
* **Factor**: An abject that connects observed and latent variables with a feature vector.
* **Scorer**: Computes an overall score for a state using the factors/features that are connected to its variables.
* **Sampler**: Explores the search space of the states to find the highest scoring state given an initial state.
 * **Initializer**: Provides an initial state for a data instance (e.g. containing prior knowledge).
 * **Explorer**: Generates slightly changed variants of a given state that span the sampling area.
* **Objective Function**: Computes a score for a state w.r.t. an expected result. 

To generate states for a data instance (e.g. a sentence), the general procedure is as follows:
* Create an initial state for the given data instance using the Initializer. This initial state can contain prior knowledge or nothing at all.
* Pass this initial state to the Sampler to generate a chain of sampled states.
 * The sampler creates a list of possible successor states that all modify the initial state by some small change. For instance, these states might contain new, removed or otherwise changed annotations. The DefaultSampler uses Explorers for this.
 * During training, the generated states are used to adapt model parameters.
 * All possible successor states are scored (using the model or the objective function).
 * A suitable successor state is selected, depending on the computed score (e.g. the state with the highest score).
 * The sampler repeats the previous steps to create a sequence of states. Usually, the scores of these sampled states should increase with each sampling step.
* At some point (e.g. a fixed amount of steps), the sampler decides to stop sampling and returns the complete sequence of states. The final state in this sequence (hopefully) maximizes the score and is, by that, best suited for the provided data instance.


