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

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.descriptor.SofaCapability;
import org.apache.uima.jcas.JCas;

import qa.qcri.iyas.type.Similarity;

@SofaCapability(
		inputSofas = {"_InitialView","leftJCasView","rightJCasView"}
)
public class SimilarityMeasureTestAnnotator extends JCasAnnotator_ImplBase {
	
	public static final String PRAM_LEFT = "leftJCasView";
	public static final String PRAM_RIGHT = "rightJCasView";
	public final static String PARAM_SIMILARITY_RESOURCE = "similarityMeasure";

	@ExternalResource(key = PARAM_SIMILARITY_RESOURCE)
	SimilarityMeasure similarityMeasure;

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		try {
			JCas leftJCas = jcas.getView("leftJCasView");
			JCas rightJCas = jcas.getView("rightJCasView");
			
			double sim = similarityMeasure.getSimilarityValue(leftJCas, rightJCas);
			
			Similarity similarity = new Similarity(jcas.getView("_InitialView"));
			similarity.setLeftViewName("leftJCasView");
			similarity.setRightViewName("rightJCasView");
			similarity.setValue(sim);
			similarity.addToIndexes();
			
		} catch (CASException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
