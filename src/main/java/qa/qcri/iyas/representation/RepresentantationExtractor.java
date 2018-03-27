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


package qa.qcri.iyas.representation;

import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasMultiplier_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.descriptor.OperationalProperties;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceProcessException;
import org.uimafit.util.JCasUtil;

import qa.qcri.iyas.data.preprocessing.JCasPairGenerator;
import qa.qcri.iyas.type.AdditionalInfo;
import qa.qcri.iyas.type.cqa.Comment;
import qa.qcri.iyas.type.cqa.InstanceA;
import qa.qcri.iyas.type.cqa.InstanceB;
import qa.qcri.iyas.type.cqa.RelatedQuestionBody;
import qa.qcri.iyas.type.representation.Label;

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
			pendingJCas = getEmptyJCas();
			JCas rightJCas = jcas.getView(JCasPairGenerator.RIGHT_CAS_VIEW);
			
			Collection<AdditionalInfo> infos = JCasUtil.select(rightJCas, AdditionalInfo.class);
			if (infos.size() != 1)
				throw new AnalysisEngineProcessException("Expected an AdditionalInfo annotation, found "+infos.size(),null);
			
			AdditionalInfo info = infos.iterator().next();
			if (info.getRequesterID() == null)
				throw new AnalysisEngineProcessException("Requerer ID not set", null);
			if (info.getIndex() == -1)
				throw new AnalysisEngineProcessException("Index not set", null);
			if (info.getTotalNumberOfExamples() == -1l)
				throw new AnalysisEngineProcessException("Total number of examples not set", null);
			
			AdditionalInfo newInfo = new AdditionalInfo(pendingJCas);
			newInfo.setRequesterID(info.getRequesterID());
			newInfo.setIndex(info.getIndex());
			newInfo.setTotalNumberOfExamples(info.getTotalNumberOfExamples());
			
			if (info.getRequesterID() == null)
				throw new AnalysisEngineProcessException("Requerer Id not set", null);

			if (JCasUtil.exists(jcas, InstanceA.class)) {
				Comment comment = JCasUtil.select(rightJCas, Comment.class).iterator().next();
				newInfo.setInstanceID(comment.getID());
				
				Label label = new Label(rightJCas);
				label.setLabels(comment.getLabels());
				label.addToIndexes();
				
			} else if (JCasUtil.exists(jcas, InstanceB.class)) {
				RelatedQuestionBody question = JCasUtil.select(rightJCas, RelatedQuestionBody.class).iterator().next();
				newInfo.setInstanceID(question.getID());
				
				Label label = new Label(rightJCas);
				label.setLabels(question.getLabels());
				label.addToIndexes();
			}
			
			newInfo.addToIndexes();
			
			String repr = serializer.serialize(jcas);
//			System.out.println(repr);
			
			pendingJCas.setDocumentText(repr);
		} catch (ResourceProcessException | CASException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

}
