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

package qa.qcri.iyas.classification;

import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.descriptor.OperationalProperties;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import qa.qcri.iyas.type.AdditionalInfo;

@OperationalProperties(modifiesCas = true, outputsNewCases = false, multipleDeploymentAllowed = false)
public class ClassificationAnnotator extends JCasAnnotator_ImplBase {

	public final static String PARAM_CLASSIFIER_RESOURCE = "classifier";
	
	@ExternalResource(key = PARAM_CLASSIFIER_RESOURCE)
	private Classifier classifier;

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		Collection<AdditionalInfo> infos = JCasUtil.select(jcas, AdditionalInfo.class);
		if (infos.size() != 1)
			throw new AnalysisEngineProcessException("Expected an AdditionalInfo annotation, found "+infos.size(),null);
		
		AdditionalInfo info = infos.iterator().next();
		
		float predictionScore = classifier.getPredictionScore(classifier.extractExample(jcas));
		
		info.setPrediction(""+predictionScore);
	}
}
