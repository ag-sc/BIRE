package corpus;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface Corpus<InstanceT extends LabeledInstance<?>> extends Serializable {

	public List<InstanceT> getDocuments();

	public void addDocument(InstanceT instance);

	public void addDocuments(Collection<InstanceT> instances);

}
