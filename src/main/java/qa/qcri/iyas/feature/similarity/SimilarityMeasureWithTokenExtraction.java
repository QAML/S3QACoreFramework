/**
 * Copyright 2017 Giovanni Da San Martino and Salvatore Romeo
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 */

package qa.qcri.iyas.feature.similarity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import com.google.common.base.Joiner;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import qa.qcri.iyas.data.preprocessing.Stopwords;
import qa.qcri.iyas.util.tree.node.RichNode;
import qa.qcri.iyas.util.tree.node.RichTokenNode;

/**
 * The class extends {@link ThreadSafeSimilarityMeasure} adding a method to extract the list of tokens from a JCas and return
 * them as a space-separated String. 
 * Such String is the input of some of the similarity measures of this package.  
 * 
 * The class has three parameters. 
 * 
 * @author Giovanni Da San Martino
 *
 */
public abstract class SimilarityMeasureWithTokenExtraction extends ThreadSafeSimilarityMeasure {
	
	public static final String PARAM_NAME_STOPWORDS_OBJECT = "stopwordsObject";
	
	public static final String PARAM_NAME_REMOVE_STOPWORDS = "removeStopwords";
		
	@ConfigurationParameter(name = PARAM_NAME_STOPWORDS_OBJECT)
	protected Stopwords stopwordsObject;
	
	@ConfigurationParameter(name = PARAM_NAME_REMOVE_STOPWORDS, defaultValue="true")
	protected boolean removeStopwords;
	
	protected Collection<String> getTokenList(JCas cas, String parameterList) {
		
		List<RichTokenNode> richTokens = new ArrayList<>();
		for (Token token : JCasUtil.select(cas, Token.class)) {
			if (token.getPos() == null && token.getLemma().getValue().matches("\\p{Punct}"))
				continue;
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

		return tokens;
	}
		
	protected String getTokenString(JCas cas, String parameterList) {
		
		return Joiner.on(" ").join(getTokenList(cas, parameterList));
	}
		
}
