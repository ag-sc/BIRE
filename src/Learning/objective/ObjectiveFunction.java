package Learning.objective;

import Learning.Score;
import Logging.Log;
import Variables.EntityAnnotation;
import Variables.State;

public abstract class ObjectiveFunction {

	public abstract Score score(State state, State goldState);

	public static int overlap(EntityAnnotation entity1, EntityAnnotation entity2) {
		int a = entity1.getBeginTokenIndex();
		int b = entity1.getEndTokenIndex();
		int x = entity2.getBeginTokenIndex();
		int y = entity2.getEndTokenIndex();
		int overlap = Math.max(0, Math.min(b, y) - Math.max(a, x) + 1);
		return overlap;
	}

	public boolean typeMatches(EntityAnnotation entity, EntityAnnotation goldEntity) {
		return entity.getType().equals(goldEntity.getType());
	}

}
