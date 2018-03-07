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
 
 
package qa.qcri.iyas.feature.extractor;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.SofaCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.DoubleArray;

import qa.qcri.iyas.feature.JCasPairGenerator;
import qa.qcri.iyas.feature.similarity.SimilarityMeasure;
import qa.qcri.iyas.type.feature.FeatureVector;


@SofaCapability(
		inputSofas = {"_InitialView",JCasPairGenerator.LEFT_CAS_VIEW,JCasPairGenerator.RIGHT_CAS_VIEW}
)
public class SimilarityAnnotator extends JCasAnnotator_ImplBase  {
	
	public final static String PARAM_NAME_SIMILARITIES = "similarities";

	@ConfigurationParameter(name = PARAM_NAME_SIMILARITIES)
	private String similarities[];

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		try {
			JCas leftJCas = jcas.getView(JCasPairGenerator.LEFT_CAS_VIEW);
			JCas rightJCas = jcas.getView(JCasPairGenerator.RIGHT_CAS_VIEW);
			
			UimaContext context = getContext();
			
			FeatureVector simVector = new FeatureVector(jcas);
			simVector.setFeatures(new DoubleArray(jcas,similarities.length));
			for (int i=0;i<similarities.length;i++) {
				Object obj = context.getResourceObject(similarities[i]);
				SimilarityMeasure sim = null;
				if (obj instanceof SimilarityMeasure)
					sim = (SimilarityMeasure)obj;
				
				double simValue = sim.getSimilarityValue(leftJCas, rightJCas);
				simVector.setFeatures(i, simValue);
			}
			simVector.addToIndexes();
			
		} catch (UIMAException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}
}
