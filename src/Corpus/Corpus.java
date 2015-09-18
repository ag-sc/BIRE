package Corpus;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface Corpus<T extends AnnotatedDocument> extends Serializable {

	public AnnotationConfig getCorpusConfig();

	public List<T> getDocuments();

	public void addDocument(T doc);

	public void addDocuments(Collection<T> documents);

}
