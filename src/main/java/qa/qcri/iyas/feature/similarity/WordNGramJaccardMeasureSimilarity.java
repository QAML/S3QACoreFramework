/**
 * Copyright 2017 Giovanni Da San Martino
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

import java.util.Collection;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import com.google.common.base.Joiner;

import de.tudarmstadt.ukp.similarity.algorithms.api.SimilarityException;
import de.tudarmstadt.ukp.similarity.algorithms.lexical.ngrams.WordNGramJaccardMeasure;
import qa.qcri.iyas.util.tree.node.RichNode;



/**
 * Defines a similarity function between two JCas annotations and computes
 * the Jaccard similarity coefficient between n-gram extracted from the list of tokens. 
 * The similarity itself has one parameter, PARAM_NAME_NGRAM_LENGTH, which determines the length of the 
 * n-grams on which the computation is based on. However, since it operates on a String which 
 * results from a concatenation  of tokens, the parameters of the class extracting such 
 * tokens need to be passed first, see class {@link SimilarityMeasureWithTokenExtraction} for details. 
 * For an example of usage, check the test class {@link WordNGramJaccardMeasureSimilarityTest}.
 * 
 * @author Giovanni Da San Martino
 *
 */
public class WordNGramJaccardMeasureSimilarity extends SimilarityMeasureWithTokenExtraction {
			
	private static final String PARAMETER_LIST = Joiner.on(",").join(
			new String[] { RichNode.OUTPUT_PAR_LEMMA, RichNode.OUTPUT_PAR_TOKEN_LOWERCASE });
	
	public static final String PARAM_NAME_NGRAM_LENGTH = "ngramLength";
	
	@ConfigurationParameter(name = PARAM_NAME_NGRAM_LENGTH, mandatory=true)
	private int ngramLength;
	
	@Override
	public double getSimilarityValue(JCas leftJCas, JCas rightJCas) throws UIMAException {

		WordNGramJaccardMeasure sim = new WordNGramJaccardMeasure(ngramLength);
		
		double similarity;
		Collection<String> tokenListLeftJcas = getTokenList(leftJCas, PARAMETER_LIST);
		Collection<String> tokenListRightJcas = getTokenList(rightJCas, PARAMETER_LIST);
		
		try {
			similarity = sim.getSimilarity(tokenListLeftJcas, tokenListRightJcas);
		} catch (SimilarityException e) {
			throw new UIMAException(new IllegalStateException("ERROR while computing WordNGramJaccardMeasure "
					+ ngramLength + "-gram similarity on strings " + tokenListLeftJcas + " and " + tokenListLeftJcas));
		}
		return similarity;
				
	}
	
}
