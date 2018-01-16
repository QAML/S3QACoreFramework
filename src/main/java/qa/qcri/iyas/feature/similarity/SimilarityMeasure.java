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

import org.apache.uima.fit.component.ExternalResourceAware;
import org.apache.uima.fit.component.initialize.ConfigurationParameterInitializer;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;

/**
 * Given two JCases, a {@link SimilarityMeasure} computes a similarity based on the content and the annotations in the JCases.
 * Each concrete class of {@link SimilarityMeasure} must be thread-safe. Whenever a concrete class requires resources to be
 * inizialized, such initializion must be done in {@link qa.qcri.iyas.feature.similarity.SimilarityMeasure#afterResourcesInitialized()}
 * method. All parameters, for both resources initialization and similarity computation, must be declared as 
 * @ConfigurationParameter. The parameters for the similarity computation, if any, cannot change over the time.
 * 
 * @author Salvatore Romeo
 *
 */
public abstract class SimilarityMeasure implements SharedResourceObject, ExternalResourceAware {

	@ConfigurationParameter(name=ExternalResourceFactory.PARAM_RESOURCE_NAME)
	private String resourceName;
	
	@Override
	public void load(DataResource data) throws ResourceInitializationException {
		ConfigurationParameterInitializer.initialize(this, data);
	}

	@Override
	public final String getResourceName() {
		return this.resourceName;
	}

	@Override
	public void afterResourcesInitialized() throws ResourceInitializationException {
		// TODO Auto-generated method stub	
	}
	
	public abstract double getSimilarityValue(JCas leftJCas, JCas rightJCas);
}
