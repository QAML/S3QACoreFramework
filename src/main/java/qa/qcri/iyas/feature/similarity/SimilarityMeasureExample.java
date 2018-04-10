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

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

public class SimilarityMeasureExample extends ThreadSafeSimilarityMeasure {
	
	/**
	 * Name of the first parameter
	 */
	public static final String PARAM_NAME_1 = "paramer1";
	public static final String PARAM_NAME_FAKE_SIM_TO_GENERATE = "paramer2";

	/**
	 * Just an example of how to declare a {@link ConfigurationParameter}. This parameter is not used in this example.
	 */
	@ConfigurationParameter(name = PARAM_NAME_1)
	String exampleParam1;
	
	/**
	 * Another example of {@link ConfigurationParameter}. Note that type of a {@link ConfigurationParameter} can be anyone.
	 * In this example this parameter is the fake similarity that will returned.
	 */
	@ConfigurationParameter(name = PARAM_NAME_FAKE_SIM_TO_GENERATE)
	double exampleParam2;
	
	/**
	 * Given the two specified JCases, returns the similarity to be computed by this concrete implementation of {@link ThreadSafeSimilarityMeasure}.
	 * In this example the JCases are not considered and a fake similarity value is returned.
	 */
	@Override
	public double getSimilarityValue(JCas leftJCas, JCas rightJCas) {
		return exampleParam2;
	}

}
