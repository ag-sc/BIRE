package Variables;

public class RelationAnnotation extends Annotation {

	RelationType type;

	EntityAnnotation arg1;
	EntityAnnotation arg2;

	public RelationAnnotation(RelationType relationType, EntityAnnotation arg1,
			EntityAnnotation arg2) {
		this.type = relationType;
		this.arg1 = arg1;
		this.arg2 = arg2;
	}

	@Override
	public String toString() {
		String arg1ID = arg1 != null ? arg1.id : "null";
		String arg2ID = arg2 != null ? arg2.id : "null";
		return "RelationAnnotation [type=" + type + ", arg1=" + arg1ID
				+ ", arg2=" + arg2ID + "]";
	}

}
