package Variables;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class RelationAnnotation extends Annotation {

	RelationType type;

	Map<String, EntityAnnotation> arguments = new HashMap<String, EntityAnnotation>();

	// EntityAnnotation arg1;
	// EntityAnnotation arg2;

	public RelationAnnotation(RelationType type,
			Map<String, EntityAnnotation> arguments) {
		super();
		this.type = type;
		this.arguments = arguments;
	}

	@Override
	public String toString() {
		StringBuilder argumentBuilder = new StringBuilder();
		for (Entry<String, EntityAnnotation> a : arguments.entrySet()) {
			argumentBuilder.append("(");
			argumentBuilder.append(a.getKey());
			argumentBuilder.append(" : ");
			argumentBuilder.append(a.getValue() != null ? a.getValue().getID()
					: "NULL");
			argumentBuilder.append(")");
		}
		return "RelationAnnotation [type=" + type + ", arguments="
				+ argumentBuilder.toString() + "]";
	}

	// public RelationAnnotation(RelationType relationType, EntityAnnotation
	// arg1,
	// EntityAnnotation arg2) {
	// this.type = relationType;
	// this.arg1 = arg1;
	// this.arg2 = arg2;
	// }
	//
	// @Override
	// public String toString() {
	// String arg1ID = arg1 != null ? arg1.id : "null";
	// String arg2ID = arg2 != null ? arg2.id : "null";
	// return "RelationAnnotation [type=" + type + ", arg1=" + arg1ID
	// + ", arg2=" + arg2ID + "]";
	// }

}
