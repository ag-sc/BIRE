package Corpus.parser.stanford;

import java.net.URL;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

public class StanfordParser {
	public static final String PARSER_MODEL = "/edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";

	public static void main(String[] args) {
//		URL modelFilePath = StanfordParser.class.getResource(PARSER_MODEL);
//		System.out.println("M: " + modelFilePath);
//		LexicalizedParser lp = LexicalizedParser.loadModel();
		LexicalizedParser lp = LexicalizedParser.loadModel(PARSER_MODEL);
		String[] sent = { "This", "is", "an", "easy", "sentence", "." };
		List<CoreLabel> rawWords = Sentence.toCoreLabelList(sent);
		Tree parse = lp.apply(rawWords);
		parse.pennPrint();
		System.out.println();
	}
}
