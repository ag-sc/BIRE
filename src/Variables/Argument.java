package Variables;

import java.io.Serializable;
import java.util.List;

public class Argument implements Serializable {

	private ArgumentRole role;
	private List<String> types;

	public Argument(String role, List<String> types) {
		this(new ArgumentRole(role), types);
	}

	public Argument(ArgumentRole role, List<String> types) {
		this.role = role;
		this.types = types;
	}

	public ArgumentRole getRole() {
		return role;
	}

	public List<String> getTypes() {
		return types;
	}

	@Override
	public String toString() {
		return "Argument [role=" + role + ", types=" + types + "]";
	}

}
