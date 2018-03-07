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

import cc.mallet.types.FeatureVector;
import cc.mallet.types.NormalizedDotProductMetric;
import qa.qcri.iyas.data.preprocessing.BowProvider;
import qa.qcri.iyas.util.tree.node.RichNode;


/**
 * Defines a similarity function between two JCas annotations and computes
 * cosine similarity between a bag-of-words representation of the data. 
 * 
 * The similarity itself has one parameter only, PARAM_NAME_REPRESENTATION_TYPE, whose
 * examples of values are PARAMETER_LIST_LEMMAS and PARAMETER_LIST_POSTAGS, depending 
 * on whether the tokens are generated from lemmas or postags. 
 * However, since it operates on a bag-of-words representation, the parameters of the 
 * parent class need to be passed (before PARAM_NAME_REPRESENTATION_TYPE), 
 * see class {@link SimilarityMeasureWithBowExtraction}
 * for details on such parameters.  
 * For an example of usage check the test class {@link CosineBowSimilarityTest}.    
 * 
 * @author Giovanni Da San Martino
 *
 */
public class CosineBowSimilarity extends SimilarityMeasureWithBowExtraction {
	
	public static final String PARAMETER_LIST_LEMMAS = Joiner.on(",").join(
			new String[] { RichNode.OUTPUT_PAR_LEMMA, RichNode.OUTPUT_PAR_TOKEN_LOWERCASE });
	public static final String PARAMETER_LIST_POSTAGS = RichNode.OUTPUT_PAR_POSTAG;
	
	public static final String PARAM_NAME_REPRESENTATION_TYPE = "paramenterList";
	
	@ConfigurationParameter(name = PARAM_NAME_REPRESENTATION_TYPE, mandatory=true)
	private String paramenterList;
	
	@Override
	public double getSimilarityValue(JCas leftJCas, JCas rightJCas) throws UIMAException {
		
		BowProvider bowProvider = getBowProvider(paramenterList);
		NormalizedDotProductMetric metric = new NormalizedDotProductMetric();
		
		FeatureVector fv1 = bowProvider.getFeatureVector(getTokenList(leftJCas, paramenterList));
		FeatureVector fv2 = bowProvider.getFeatureVector(getTokenList(rightJCas, paramenterList));
		
		double distance = metric.distance(fv1, fv2);
		
		if(Double.isNaN(distance)) {
			return 0.0;
		} else {
			return 1.0 - distance;
		}
						
	}
	
}
