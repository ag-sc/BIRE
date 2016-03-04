package templates;

import corpus.Instance;
import exceptions.UnkownTemplateRequestedException;
import variables.AbstractState;

public interface TemplateFactory<InstanceT extends Instance, StateT extends AbstractState<InstanceT>> {

	public AbstractTemplate<InstanceT, StateT, ?> newInstance(String templateName)
			throws UnkownTemplateRequestedException;

}
