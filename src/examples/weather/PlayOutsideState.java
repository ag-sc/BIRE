package examples.weather;

import java.util.concurrent.atomic.AtomicInteger;

import utility.VariableID;
import variables.AbstractState;

public class PlayOutsideState extends AbstractState<WeatherInstance> {

	private static final String GENERATED_ENTITY_ID_PREFIX = "G";
	private AtomicInteger entityIDIndex = new AtomicInteger();
	private PlayOutsideVariable playOutside;

	public PlayOutsideState(WeatherInstance weatherInstance) {
		super(weatherInstance);
	}

	public PlayOutsideState(WeatherInstance weatherInstance, boolean playOutside) {
		super(weatherInstance);
		this.playOutside = new PlayOutsideVariable(generateVariableID(), playOutside);
	}

	public PlayOutsideState(PlayOutsideState state) {
		super(state.getInstance());
		this.playOutside = new PlayOutsideVariable(state.playOutside);
	}

	public PlayOutsideVariable getPlayOutsideVariable() {
		return playOutside;
	}

	public void setPlayOutside(PlayOutsideVariable playOutside) {
		this.playOutside = playOutside;
	}

	@Override
	public String toString() {
		return "PlayOutsideState [" + this.id + ": " + instance + ", playOutside=" + playOutside + "]";
	}

	protected VariableID generateVariableID() {
		int currentID = entityIDIndex.getAndIncrement();
		String id = GENERATED_ENTITY_ID_PREFIX + currentID;
		return new VariableID(id);
	}
}
