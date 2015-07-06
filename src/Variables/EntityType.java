package Variables;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class EntityType implements Serializable{

	private String type;
	private Map<String, Argument> coreArguments;

	private Map<String, Argument> optionalArguments;

	public EntityType(String type) {
		this.type = type;
		this.coreArguments = new HashMap<String, Argument>();
		this.optionalArguments = new HashMap<String, Argument>();
	}

	public EntityType(String type, Map<String, Argument> coreArguments,
			Map<String, Argument> optionalArguments) {
		this.type = type;
		this.coreArguments = coreArguments;
		this.optionalArguments = optionalArguments;
	}

	public String getName() {
		return type;
	}

	public Map<String, Argument> getCoreArguments() {
		return coreArguments;
	}

	public Map<String, Argument> getOptionalArguments() {
		return optionalArguments;
	}

	@Override
	public String toString() {
		return "EntityType [type=" + type + ", coreArguments=" + coreArguments.values()
				+ ", optionalArguments=" + optionalArguments.values() + "]";
	}

}
