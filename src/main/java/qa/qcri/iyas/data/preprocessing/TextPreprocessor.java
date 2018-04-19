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
 
 
package qa.qcri.iyas.data.preprocessing;

import org.apache.uima.fit.component.Resource_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;

/**
 * A {@link TextPreprocessor} is a resource that in charge of performing the required preprocessing for the text. 
 * The {@link TextPreprocessor} is used by the {@link InputJCasMultiplier} to preprocess the text content for each 
 * of the created JCases. Each implementation of this class must be thread-safe.
 * 
 * @author Salvatore Romeo
 *
 */
public abstract class TextPreprocessor extends Resource_ImplBase {

	@Override
	public void afterResourcesInitialized() {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void destroy() {
		try {
			releaseResources();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Called by the framework, initializes the resources of this {@link TextPreprocessor}, if any. If a sub-class need some
	 * parameter, each of them must be declared as {@link ConfigurationParameter}. Please refer to 
	 * <a href="https://uima.apache.org/d/uimafit-current/tools.uimafit.book.html#ugr.tools.uimafit.configurationparameters">Apache uimaFIT Documentation</a> 
	 * for the declaration and initialization of {@link ConfigurationParameter}s. {@link TextPreprocessor} provides empty
	 * implementation for this method. Each implementation of this method must be thread-safe.
	 * 
	 * @throws Exception
	 */
	protected void init() throws Exception {}
	
	/**
	 * Called by the framework, releases the resources binded at the initialization time. {@link TextPreprocessor} 
	 * provides empty implementation for this method. Each implementation of this method must be thread-safe.
	 */
	protected void releaseResources()  {}
	
	/**
	 * Performs the preprocessing on the specified text and returns the preprocessed one.
	 * @param text text to preprocess
	 * @param lang language of the text to preprocess
	 * @return preprocessed text
	 */
	public abstract String preprocess(String text, String lang);
	
	public abstract String concatenateBodyAndSubject(String subject,String body,boolean lowercase);
}
