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

import org.apache.uima.UIMAException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import com.google.common.base.Joiner;

import de.tudarmstadt.ukp.similarity.algorithms.api.SimilarityException;
import de.tudarmstadt.ukp.similarity.algorithms.lexical.string.GreedyStringTiling;
import qa.qcri.iyas.util.tree.node.RichNode;


/**
 * Defines a similarity function between two JCas annotations and computes
 * greedy string tiling. 
 * 
 * The similarity itself has one parameter only, PARAM_NAME_TILE_LENGTH, which specify
 * the minimum length for a valid match (if the maximum matching substring is less than
 * PARAM_NAME_TILE_LENGTH, the similarity returns 0). 
 * However, since it operates on a String which results from a concatenation
 * of tokens, the parameters of the class extracting tokens need to be passed (before
 * PARAM_NAME_TILE_LENGTH), see class {@link SimilarityMeasureWithTokenExtraction}
 * for details on such parameters.  
 * For an example of usage check the test class {@link GreedyStringTilingSimilarityTest}.    
 * 
 * @author Giovanni Da San Martino
 *
 */
public class GreedyStringTilingSimilarity extends SimilarityMeasureWithTokenExtraction {
	
	/**
	 * 
	 */
	public static final String PARAM_NAME_TILE_LENGTH = "tileLength";
			
	@ConfigurationParameter(name = PARAM_NAME_TILE_LENGTH, defaultValue="3")
	private int tileLength;
		
	private static final String PARAMETER_LIST = Joiner.on(",").join(
			new String[] { RichNode.OUTPUT_PAR_LEMMA, RichNode.OUTPUT_PAR_TOKEN_LOWERCASE });
		
	@Override
	public double getSimilarityValue(JCas leftJCas, JCas rightJCas) throws UIMAException {

		GreedyStringTiling sim = new GreedyStringTiling(tileLength);
		
		double similarity;
		String tokenListLeftJcas = getTokenString(leftJCas, PARAMETER_LIST);
		String tokenListRightJcas = getTokenString(rightJCas, PARAMETER_LIST);
		
		try {
			similarity = sim.getSimilarity(tokenListLeftJcas, tokenListRightJcas);
		} catch (SimilarityException e) {
			throw new UIMAException(new IllegalStateException("ERROR while computing GreedyStringTiling"
					+ " similarity on strings " + tokenListLeftJcas + " and " + tokenListLeftJcas));
		}
		return similarity;
				
	}
	
}
