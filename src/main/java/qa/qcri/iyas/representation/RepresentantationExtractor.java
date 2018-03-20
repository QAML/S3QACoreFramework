/**
 * Copyright 201 Salvatore Romeo
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


package qa.qcri.iyas.representation;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.fit.component.JCasMultiplier_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.descriptor.OperationalProperties;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceProcessException;

@OperationalProperties(modifiesCas = false, outputsNewCases = true, multipleDeploymentAllowed = true)
//@TypeCapability(
//		inputs = {"qa.qcri.iyas.types.UserQuestion",
//				   "qa.qcri.iyas.types.UserQuestionSubject",
//				   "qa.qcri.iyas.types.UserQuestionBody",
//				   "qa.qcri.iyas.types.RelatedQuestion",
//				   "qa.qcri.iyas.types.RelatedQuestionSubject",
//				   "qa.qcri.iyas.types.RelatedQuestionBody",
//				   "qa.qcri.iyas.types.Comment",
//				   "qa.qcri.iyas.type.representation.DenseVector",
//				   "qa.qcri.iyas.type.representation.SparseVector",
//				   "qa.qcri.iyas.type.representation.Tree",
//				   "qa.qcri.iyas.type.representation.TreePair"},
//		
//		outputs = {"qa.qcri.iyas.types.UserQuestion",
//				   "qa.qcri.iyas.types.UserQuestionSubject",
//				   "qa.qcri.iyas.types.UserQuestionBody",
//				   "qa.qcri.iyas.types.RelatedQuestion",
//				   "qa.qcri.iyas.types.RelatedQuestionSubject",
//				   "qa.qcri.iyas.types.RelatedQuestionBody",
//				   "qa.qcri.iyas.types.Comment"}
//)
public class RepresentantationExtractor extends JCasMultiplier_ImplBase {
	
	public static final String SERIALIZER_EXTERNAL_RESOURCE = "serializer";
	
	@ExternalResource(key = SERIALIZER_EXTERNAL_RESOURCE)
	private Serializer serializer;
	
	private JCas pendingJCas = null;
	
	@Override
	public boolean hasNext() throws AnalysisEngineProcessException {
		return pendingJCas != null;
	}

	@Override
	public AbstractCas next() throws AnalysisEngineProcessException {
		JCas jcas = pendingJCas;
		pendingJCas = null;
		return jcas;
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		try {
			String repr = serializer.serialize(jcas);
			pendingJCas = getEmptyJCas();
			pendingJCas.setDocumentText(repr);
		} catch (ResourceProcessException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

}
