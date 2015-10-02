package Learning.objective;

import Learning.Score;
import Variables.AEntityAnnotation;
import Variables.IState;

public abstract class ObjectiveFunction<StateT extends IState> {

	public abstract Score score(StateT state, StateT goldState);

}
