package Variables;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class EntityType implements Serializable {

	private String type;
	private Map<ArgumentRole, Argument> coreArguments;

	private Map<ArgumentRole, Argument> optionalArguments;

	public EntityType(String type) {
		this.type = type;
		this.coreArguments = new HashMap<ArgumentRole, Argument>();
		this.optionalArguments = new HashMap<ArgumentRole, Argument>();
	}

	public EntityType(String type, Map<ArgumentRole, Argument> coreArguments,
			Map<ArgumentRole, Argument> optionalArguments) {
		this.type = type;
		this.coreArguments = coreArguments;
		this.optionalArguments = optionalArguments;
	}

	public String getName() {
		return type;
	}

	public Map<ArgumentRole, Argument> getCoreArguments() {
		return coreArguments;
	}

	public Map<ArgumentRole, Argument> getOptionalArguments() {
		return optionalArguments;
	}

	@Override
	public String toString() {
		return "EntityType [type=" + type + ", coreArguments=" + coreArguments.values() + ", optionalArguments="
				+ optionalArguments.values() + "]";
	}

}
