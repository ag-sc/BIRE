package Learning.objective;

import Learning.Score;
import Variables.AEntityAnnotation;
import Variables.IState;

public abstract class ObjectiveFunction<StateT extends IState> {

	public abstract Score score(StateT state, StateT goldState);

	public static int overlap(AEntityAnnotation entity1, AEntityAnnotation entity2) {
		int a = entity1.getBeginTokenIndex();
		int b = entity1.getEndTokenIndex();
		int x = entity2.getBeginTokenIndex();
		int y = entity2.getEndTokenIndex();
		int overlap = Math.max(0, Math.min(b, y) - Math.max(a, x));
		return overlap;
	}

	public boolean typeMatches(AEntityAnnotation entity, AEntityAnnotation goldEntity) {
		return entity.getType().equals(goldEntity.getType());
	}

}
