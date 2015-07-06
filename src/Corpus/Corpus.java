package Corpus;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface Corpus extends Serializable{
	public AnnotationConfig getCorpusConfig();

	public List<AnnotatedDocument> getDocuments();

	public void addDocument(AnnotatedDocument doc);

	public void addDocuments(Collection<AnnotatedDocument> documents);

}
