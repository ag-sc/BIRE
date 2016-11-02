package examples.weather;

import java.util.concurrent.atomic.AtomicInteger;

import variables.AbstractState;

public class PlayOutsideState extends AbstractState<WeatherInstance> {

	private static final String GENERATED_ENTITY_ID_PREFIX = "G";
	private AtomicInteger entityIDIndex = new AtomicInteger();
	private boolean playOutside;

	public PlayOutsideState(WeatherInstance weatherInstance) {
		super(weatherInstance);
	}

	public PlayOutsideState(WeatherInstance weatherInstance, boolean playOutside) {
		super(weatherInstance);
		this.playOutside = playOutside;
	}

	public PlayOutsideState(PlayOutsideState state) {
		super(state);
		this.playOutside = state.playOutside;
	}

	public boolean isPlayingOutside() {
		return playOutside;
	}

	public void setPlayOutside(boolean playOutside) {
		this.playOutside = playOutside;
	}

	@Override
	public String toString() {
		return "PlayOutsideState [" + this.id + ": " + instance + ", playOutside=" + playOutside + "]";
	}

}
