package Variables;

import java.io.Serializable;
import java.util.List;

public class Argument implements Serializable{

	String role;
	List<String> types;

	public Argument(String role, List<String> types) {
		this.role = role;
		this.types = types;
	}

	public String getRole() {
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
