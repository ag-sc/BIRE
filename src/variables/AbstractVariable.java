package variables;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import utility.VariableID;

public class AbstractVariable implements Serializable {
	private final static AtomicLong variableIDIndex = new AtomicLong();

	protected VariableID id;

	public AbstractVariable() {
		this.id = generateVariableID();
	}

	public VariableID getID() {
		return id;
	}

	public void markAsModified() {
		id = generateVariableID();
	}

	private VariableID generateVariableID() {
		long currentID = variableIDIndex.getAndIncrement();
		String id = "V" + String.valueOf(currentID);
		return new VariableID(id);
	}
}
