/**
 * Copyright 2017 Salvatore Romeo
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
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;

import qa.qcri.iyas.data.preprocessing.JCasPairGenerator;
import qa.qcri.iyas.feature.ThreadSafeFeature;
import qa.qcri.iyas.type.representation.NumericFeature;

public class SimilarityMeasureTestAnnotator extends JCasAnnotator_ImplBase {
	
	public static final String LEFT_CAS_VIEW = JCasPairGenerator.LEFT_CAS_VIEW;
	public static final String RIGHT_CAS_VIEW = JCasPairGenerator.RIGHT_CAS_VIEW;
	public final static String PARAM_SIMILARITY_RESOURCE = "similarityMeasure";

	@ExternalResource(key = PARAM_SIMILARITY_RESOURCE)
	ThreadSafeFeature similarityMeasure;

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		try {
//			JCas leftJCas = jcas.getView(LEFT_CAS_VIEW);
//			JCas rightJCas = jcas.getView(RIGHT_CAS_VIEW);
			
			double sim = similarityMeasure.getValue(jcas);
			
			NumericFeature similarity = new NumericFeature(jcas.getView("_InitialView"));
			similarity.setValue(sim);
			similarity.addToIndexes();
			
		} catch (UIMAException e) {
			throw new AnalysisEngineProcessException(e);
		}
		
	}
	
}
