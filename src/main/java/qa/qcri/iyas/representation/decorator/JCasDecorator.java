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
 
 
package qa.qcri.iyas.representation.decorator;

import org.apache.uima.fit.component.ExternalResourceAware;
import org.apache.uima.fit.component.initialize.ConfigurationParameterInitializer;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.resource.SharedResourceObject;

import qa.qcri.iyas.data.preprocessing.refining.PreprocessingRefiningAnnotator;

/**
 * Used by a {@link PreprocessingRefiningAnnotator}, adds additional information (meta-information) JCaes.
 * 
 * @author Salvatore Romeo
 *
 */
public abstract class JCasDecorator  implements SharedResourceObject, ExternalResourceAware {

	@ConfigurationParameter(name=ExternalResourceFactory.PARAM_RESOURCE_NAME)
	private String resourceName;
	
	public abstract void decorate(JCas jcas) throws ResourceProcessException;
	
	@Override
	public void load(DataResource data) throws ResourceInitializationException {
		ConfigurationParameterInitializer.initialize(this, data);
	}

	@Override
	public String getResourceName() {
		return this.resourceName;
	}

	@Override
	public void afterResourcesInitialized() throws ResourceInitializationException {
		// TODO Auto-generated method stub	
	}
	
}
