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

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.OperationalProperties;
import org.apache.uima.fit.descriptor.SofaCapability;
import org.apache.uima.jcas.JCas;

import qa.qcri.iyas.data.preprocessing.JCasPairGenerator;

@OperationalProperties(modifiesCas = true, outputsNewCases = false, multipleDeploymentAllowed = true)
@SofaCapability(
		inputSofas = {"_InitialView",JCasPairGenerator.LEFT_CAS_VIEW,JCasPairGenerator.RIGHT_CAS_VIEW}
)
public class DecorationAnnotator extends JCasAnnotator_ImplBase {

	public final static String PARAM_NAME_DECORATORS = "decorators";

	@ConfigurationParameter(name = PARAM_NAME_DECORATORS)
	private String decorators[];
	
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		try {
			
			UimaContext context = getContext();
			
			for (int i=0;i<decorators.length;i++) {
				Object obj = context.getResourceObject(decorators[i]);
				JCasDecorator decorator = null;
				if (obj instanceof JCasDecorator)
					decorator = (JCasDecorator)obj;
				else
					throw new AnalysisEngineProcessException("Resource named "+decorators[i]+" not found",null);
				
				decorator.decorate(jcas);
			}
			
		} catch (UIMAException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}
}
