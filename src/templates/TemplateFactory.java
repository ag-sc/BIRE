package templates;

import exceptions.UnkownTemplateRequestedException;
import variables.AbstractState;

public interface TemplateFactory<InstanceT, StateT extends AbstractState<InstanceT>> {

	public AbstractTemplate<InstanceT, StateT, ?> newInstance(String templateName)
			throws UnkownTemplateRequestedException, Exception;

}
