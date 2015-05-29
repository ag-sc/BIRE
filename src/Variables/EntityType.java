package Variables;

import java.util.List;

public class EntityType {

	String type;
	List<Argument> core;
	List<Argument> optional;
	
	public EntityType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "EntityType [type=" + type + "]";
	}

}
