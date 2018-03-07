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

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.descriptor.OperationalProperties;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;

import qa.qcri.iyas.data.preprocessing.refining.PreprocessingRefiner;


/**
 * A {@link PreprocessingRefiningAnnotator} adds additional information (meta-information) to the aggregated CASes.
 * 
 * @author Salvatore Romeo
 *
 */
@OperationalProperties(modifiesCas = true, outputsNewCases = false, multipleDeploymentAllowed = true)
@TypeCapability(
		inputs = {"qa.qcri.iyas.types.UserQuestion",
				   "qa.qcri.iyas.types.UserQuestionSubject",
				   "qa.qcri.iyas.types.UserQuestionBody",
				   "qa.qcri.iyas.types.RelatedQuestion",
				   "qa.qcri.iyas.types.RelatedQuestionSubject",
				   "qa.qcri.iyas.types.RelatedQuestionBody",
				   "qa.qcri.iyas.types.Comment"}
)
public class PreprocessingRefiningAnnotator extends JCasAnnotator_ImplBase {
	
	public final static String PARAM_PREPROCESSING_REFINER_RESOURCE = "preprocessingRefiner";

	@ExternalResource(key = PARAM_PREPROCESSING_REFINER_RESOURCE)
	PreprocessingRefiner preprocessingRefiner;

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
//		try {
//			if (JCasUtil.exists(jcas, RelatedQuestion.class)) {//Task A
//				RelatedQuestion relatedQuestion = JCasUtil.select(jcas, RelatedQuestion.class).iterator().next();
//				JCas relBody = jcas.getView(ProcessedInstancesManager.RELATED_QUESTION_BODY_VIEW);
//				preprocessingRefiner.refine(relBody);
//				if (relatedQuestion.getConcatenated()) {
//					JCas relSubject = jcas.getView(ProcessedInstancesManager.RELATED_QUESTION_SUBJECT_VIEW);
//					preprocessingRefiner.refine(relSubject);
//				}
//				for (int i=0;i<relatedQuestion.getCandidateViewNames().size();i++) {
//					JCas comment = jcas.getView(relatedQuestion.getCandidateViewNames(i));
//					preprocessingRefiner.refine(comment);
//				}
//			}
//		} catch (CASException | ResourceProcessException e) {
//			throw new AnalysisEngineProcessException(e);
//		}
	}

}
