package qa.qcri.iyas.feature.similarity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import com.google.common.base.Joiner;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import qa.qcri.iyas.data.preprocessing.Stopwords;
import qa.qcri.iyas.data.tree.nodes.RichNode;
import qa.qcri.iyas.data.tree.nodes.RichTokenNode;

public abstract class SimilarityMeasureWithStringExtraction extends SimilarityMeasure {

	public static final String PARAM_NAME_STOPWORDS_OBJECT = "stopwordsObject";
	
	public static final String PARAM_NAME_REMOVE_STOPWORDS = "removeStopwords";
		
	@ConfigurationParameter(name = PARAM_NAME_STOPWORDS_OBJECT)
	protected Stopwords stopwordsObject;
	
	@ConfigurationParameter(name = PARAM_NAME_REMOVE_STOPWORDS, defaultValue="true")
	protected boolean removeStopwords;
	
	protected String getTokenString(JCas cas, String parameterList) {
		
		List<RichTokenNode> richTokens = new ArrayList<>();
		for (Token token : JCasUtil.select(cas, Token.class)) {
			RichTokenNode richTokenNode = new RichTokenNode(token);
			richTokens.add(richTokenNode);
		}
		
		if(removeStopwords) {
			Iterator<RichTokenNode> i = richTokens.iterator();
			while(i.hasNext()) {
				RichTokenNode token = i.next();
				if(this.stopwordsObject.contains(token.getRepresentation(RichNode.OUTPUT_PAR_TOKEN_LOWERCASE))) {
					i.remove();
				}
			}
		}
		
		List<String> tokens = new ArrayList<>();
		for (RichTokenNode token : richTokens) {
			tokens.add(token.getRepresentation(parameterList));
		}
		
		return Joiner.on(" ").join(tokens);
	}
	
}
