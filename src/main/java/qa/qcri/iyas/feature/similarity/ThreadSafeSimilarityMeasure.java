/**
 * Copyright 2018 Salvatore Romeo
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
import org.apache.uima.jcas.JCas;

import qa.qcri.iyas.data.preprocessing.JCasPairGenerator;
import qa.qcri.iyas.feature.ThreadSafeFeature;

/**
 * Given two JCases, a {@link ThreadSafeSimilarityMeasure} computes a similarity based on the content and the annotations in the JCases.
 * Each concrete class of {@link ThreadSafeSimilarityMeasure} must be thread-safe. Whenever a concrete class requires resources to be
 * inizialized, such initializion must be done in {@link qa.qcri.iyas.feature.similarity.ThreadSafeSimilarityMeasure#afterResourcesInitialized()}
 * method. All parameters, for both resources initialization and similarity computation, must be declared as 
 * @ConfigurationParameter. The parameters for the similarity computation, if any, cannot change over the time.
 * 
 * @author Salvatore Romeo
 *
 */
public abstract class ThreadSafeSimilarityMeasure extends ThreadSafeFeature {
	
	@Override
	public double getValue(JCas jcas) throws UIMAException {
		JCas leftJCas = jcas.getView(JCasPairGenerator.LEFT_CAS_VIEW);
		JCas rightJCas = jcas.getView(JCasPairGenerator.RIGHT_CAS_VIEW);
		
		return getSimilarityValue(leftJCas, rightJCas);
	}
	
	public abstract double getSimilarityValue(JCas leftJCas, JCas rightJCas) throws UIMAException;
}
