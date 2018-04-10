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
 
 
package qa.qcri.iyas.feature;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.OperationalProperties;
import org.apache.uima.fit.descriptor.SofaCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.DoubleArray;

import qa.qcri.iyas.data.preprocessing.JCasPairGenerator;
import qa.qcri.iyas.feature.similarity.ThreadSafeSimilarityMeasure;
import qa.qcri.iyas.type.representation.DenseVector;

@OperationalProperties(modifiesCas = true, outputsNewCases = false, multipleDeploymentAllowed = true)
@SofaCapability(
		inputSofas = {"_InitialView",JCasPairGenerator.LEFT_CAS_VIEW,JCasPairGenerator.RIGHT_CAS_VIEW}
)
public class VectorialFeaturesAnnotator extends JCasAnnotator_ImplBase  {
	
	public final static String PARAM_NAME_SIMILARITIES = "similarities";
	public final static String PARAM_NAME_OUT_VECTOR_NAME = "vector_name";

	@ConfigurationParameter(name = PARAM_NAME_SIMILARITIES)
	private String similarities[];
	
	@ConfigurationParameter(name = PARAM_NAME_OUT_VECTOR_NAME)
	private String name;
	
	

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		try {
			UimaContext context = getContext();
			
			DenseVector simVector = new DenseVector(jcas);
			simVector.setName(name);
			simVector.setFeatures(new DoubleArray(jcas,similarities.length));
			for (int i=0;i<similarities.length;i++) {
				Object obj = context.getResourceObject(similarities[i]);
				ThreadSafeFeature feature = null;
				if (obj instanceof ThreadSafeFeature)
					feature = (ThreadSafeFeature)obj;
				
				double simValue = feature.getValue(jcas);
				simVector.setFeatures(i, simValue);
			}
			simVector.addToIndexes();
			
		} catch (UIMAException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}
}
