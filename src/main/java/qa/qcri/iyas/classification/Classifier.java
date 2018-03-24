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

import org.apache.ivy.core.resolve.ResolveProcessException;
import org.apache.uima.fit.component.ExternalResourceAware;
import org.apache.uima.fit.component.initialize.ConfigurationParameterInitializer;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;

public abstract class Classifier implements SharedResourceObject, ExternalResourceAware {

	public static final String PARAM_NAME_MODEL_FILE = "modelFile";

	@ConfigurationParameter(name=ExternalResourceFactory.PARAM_RESOURCE_NAME)
	private String resourceName;
	
	@ConfigurationParameter(name = PARAM_NAME_MODEL_FILE)
	private String modelFile;
	
	@Override
	public void load(DataResource data) throws ResourceInitializationException {
		ConfigurationParameterInitializer.initialize(this, data);
		loadModel(modelFile);
	}

	@Override
	public final String getResourceName() {
		return this.resourceName;
	}

	@Override
	public void afterResourcesInitialized() throws ResourceInitializationException {
		
	}
	
	protected abstract void loadModel(String modelFile) throws ResourceInitializationException;
	public abstract Object extractExample(JCas jcas) throws ResolveProcessException;
	public abstract float getPredictionScore(Object example) throws ResolveProcessException;

}
