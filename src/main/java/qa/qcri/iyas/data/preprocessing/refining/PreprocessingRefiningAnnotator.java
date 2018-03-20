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
 
 
package qa.qcri.iyas.data.preprocessing.refining;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.descriptor.OperationalProperties;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceProcessException;

import qa.qcri.iyas.representation.decorator.JCasDecorator;


/**
 * A {@link PreprocessingRefiningAnnotator} adds additional information (meta-information) to the aggregated CASes.
 * 
 * @author Salvatore Romeo
 *
 */
@OperationalProperties(modifiesCas = true, outputsNewCases = false, multipleDeploymentAllowed = true)
public class PreprocessingRefiningAnnotator extends JCasAnnotator_ImplBase {
	
	public final static String PARAM_PREPROCESSING_REFINER_RESOURCE = "preprocessingRefiner";

	@ExternalResource(key = PARAM_PREPROCESSING_REFINER_RESOURCE)
	JCasDecorator preprocessingRefiner;

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		try {
			preprocessingRefiner.decorate(jcas);
		} catch (ResourceProcessException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

}
