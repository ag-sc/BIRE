package examples.weather;

import java.util.concurrent.atomic.AtomicInteger;

import utility.VariableID;
import variables.AbstractState;

public class PlayOutsideState extends AbstractState {

	private static final String GENERATED_ENTITY_ID_PREFIX = "G";
	private AtomicInteger entityIDIndex = new AtomicInteger();
	private WeatherInstance weatherInstance;
	private PlayOutsideVariable playOutside;

	public PlayOutsideState(WeatherInstance weatherInstance) {
		this.weatherInstance = weatherInstance;
	}

	public PlayOutsideState(WeatherInstance weatherInstance, boolean playOutside) {
		this.weatherInstance = weatherInstance;
		this.playOutside = new PlayOutsideVariable(generateVariableID(), playOutside);
	}

	public PlayOutsideState(PlayOutsideState state) {
		this.weatherInstance = state.weatherInstance;
		this.playOutside = new PlayOutsideVariable(state.playOutside);
	}

	public WeatherInstance getWeatherInstance() {
		return weatherInstance;
	}

	public void setWeatherInstance(WeatherInstance weatherInstance) {
		this.weatherInstance = weatherInstance;
	}

	public PlayOutsideVariable getPlayOutsideVariable() {
		return playOutside;
	}

	public void setPlayOutside(PlayOutsideVariable playOutside) {
		this.playOutside = playOutside;
	}

	@Override
	public String toString() {
		return "PlayOutsideState [" + this.id + ": " + weatherInstance.weather + ", playOutside=" + playOutside + "]";
	}

	protected VariableID generateVariableID() {
		int currentID = entityIDIndex.getAndIncrement();
		String id = GENERATED_ENTITY_ID_PREFIX + currentID;
		return new VariableID(id);
	}
}
