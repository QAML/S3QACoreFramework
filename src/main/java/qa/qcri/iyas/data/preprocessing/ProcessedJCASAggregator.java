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

import java.util.Collection;
import java.util.LinkedList;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasMultiplier_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.descriptor.OperationalProperties;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import qa.qcri.iyas.type.AdditionalInfo;
import qa.qcri.iyas.type.cqa.Comment;
import qa.qcri.iyas.type.cqa.InstanceA;
import qa.qcri.iyas.type.cqa.InstanceB;
import qa.qcri.iyas.type.cqa.InstanceC;
import qa.qcri.iyas.type.cqa.QAAnnotation;
import qa.qcri.iyas.type.cqa.RelatedQuestionBody;
import qa.qcri.iyas.type.cqa.RelatedQuestionSubject;
import qa.qcri.iyas.type.cqa.UserQuestionBody;
import qa.qcri.iyas.type.cqa.UserQuestionSubject;
import qa.qcri.iyas.util.ProcessedInstancesManager;
import qa.qcri.iyas.util.tree.node.RichTokenNode;

@OperationalProperties(modifiesCas = false, outputsNewCases = true, multipleDeploymentAllowed = true)
@TypeCapability(
		inputs = {"qa.qcri.iyas.types.UserQuestionSubject",
				   "qa.qcri.iyas.types.UserQuestionBody",
				   "qa.qcri.iyas.types.RelatedQuestionSubject",
				   "qa.qcri.iyas.types.RelatedQuestionBody",
				   "qa.qcri.iyas.types.Comment"},
		
		outputs = {"qa.qcri.iyas.types.UserQuestion",
				   "qa.qcri.iyas.types.UserQuestionSubject",
				   "qa.qcri.iyas.types.UserQuestionBody",
				   "qa.qcri.iyas.types.RelatedQuestion",
				   "qa.qcri.iyas.types.RelatedQuestionSubject",
				   "qa.qcri.iyas.types.RelatedQuestionBody",
				   "qa.qcri.iyas.types.Comment"}
)
public class ProcessedJCASAggregator extends JCasMultiplier_ImplBase {

	//TODO insert ID format check
	//TODO release the CASes in case of error
	
	public final static String PARAM_PROCESSED_INSTANCES_MANAGER_RESOURCE = "processedInstancesManager";
	
//	@ExternalResource(key = PARAM_PROCESSED_INSTANCES_MANAGER_RESOURCE)
	private ProcessedInstancesManager processedInstancesManager = new ProcessedInstancesManager();
	
	private LinkedList<JCas> pendingJCases = new LinkedList<JCas>();
	
	@Override
	public boolean hasNext() throws AnalysisEngineProcessException {
		return !pendingJCases.isEmpty();
	}

	@Override
	public AbstractCas next() throws AnalysisEngineProcessException {
		JCas jcas = pendingJCases.removeFirst();
		
		return jcas;
	}

	private static <T extends TOP> T getAnnotation(JCas jcas,Class<T> clazz) throws AnalysisEngineProcessException {
		FSIterator<T> it = jcas.getAllIndexedFS(clazz);
		if (!it.hasNext())
			throw new AnalysisEngineProcessException("The specified JCas does not contain any "+clazz.getName()+" annotation",null);
		T annotation = it.next();
		if (it.hasNext())
			throw new AnalysisEngineProcessException("Only one annotation is expected "+clazz.getName(),null);
		
		return annotation;
	}
	
	private String getUserQuestionID(JCas jcas) throws AnalysisEngineProcessException {
		QAAnnotation qaAnn = null;
		
		if (JCasUtil.exists(jcas, UserQuestionBody.class)) {
			qaAnn = getAnnotation(jcas, UserQuestionBody.class);
		} else if (JCasUtil.exists(jcas, UserQuestionSubject.class)) {
			qaAnn = getAnnotation(jcas, UserQuestionSubject.class);
		} else if (JCasUtil.exists(jcas, RelatedQuestionBody.class)) {
			qaAnn = getAnnotation(jcas, RelatedQuestionBody.class);
		} else if (JCasUtil.exists(jcas, RelatedQuestionSubject.class)) {
			qaAnn = getAnnotation(jcas, RelatedQuestionSubject.class);
		} else if (JCasUtil.exists(jcas, Comment.class)) {
			qaAnn = getAnnotation(jcas, Comment.class);
		}
		else
			throw new AnalysisEngineProcessException("Expected annotation not found!",null);
		
		return qaAnn.getID().split("_")[0];
	}
	
	private String getRelatedQuestionID(JCas jcas) throws AnalysisEngineProcessException {
		QAAnnotation qaAnn = null;
		
		if (JCasUtil.exists(jcas, RelatedQuestionBody.class))
			qaAnn = getAnnotation(jcas, RelatedQuestionBody.class);
		else if (JCasUtil.exists(jcas, RelatedQuestionSubject.class))
			qaAnn = getAnnotation(jcas, RelatedQuestionSubject.class);
		else if (JCasUtil.exists(jcas, Comment.class))
			qaAnn = getAnnotation(jcas, Comment.class);
		else
			throw new AnalysisEngineProcessException("Expected annotation not found!",null);
		
		return qaAnn.getID().split("_")[0]+"_"+qaAnn.getID().split("_")[1];
	}
	
	//TODO: add release if the JCas are not automatically released when an exception occurs
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
//		for (Token token : JCasUtil.select(jcas, Token.class)) {
//			RichTokenNode richTokenNode = new RichTokenNode(token);
//		}
		
		Collection<AdditionalInfo> infos = JCasUtil.select(jcas, AdditionalInfo.class);
		if (infos.size() != 1)
			throw new AnalysisEngineProcessException("Expected an AdditionalInfo annotation, found "+infos.size(),null);
		
		AdditionalInfo info = infos.iterator().next();
		
		if (info.getRequesterID() == null)
			throw new AnalysisEngineProcessException("Requerer ID not set", null);
		
		String requesterID = info.getRequesterID();
		info.setRequesterID(null);
		
		if (JCasUtil.exists(jcas, InstanceA.class)) {
			String relatedQuestionID = getRelatedQuestionID(jcas);
			
			if (JCasUtil.exists(jcas, RelatedQuestionBody.class) || JCasUtil.exists(jcas, RelatedQuestionSubject.class))
				info.removeFromIndexes();
			
			try {
				boolean ready = processedInstancesManager.addJCasToInstanceA(requesterID,relatedQuestionID,jcas);
				if (ready) {
					JCas readyJCas = getEmptyJCas();
					processedInstancesManager.getJCasForInstanceA(requesterID,relatedQuestionID, readyJCas, true);
					
					AdditionalInfo newInfo = new AdditionalInfo(readyJCas);
					newInfo.setRequesterID(requesterID);
					newInfo.addToIndexes();
					
					pendingJCases.addLast(readyJCas);
				}
			} catch (UIMAException e) {
				throw new AnalysisEngineProcessException(e);
			}
		} else if (JCasUtil.exists(jcas, InstanceB.class)) {
			String userQuestionID = getUserQuestionID(jcas);
			
			if (JCasUtil.exists(jcas, UserQuestionBody.class) || JCasUtil.exists(jcas, UserQuestionSubject.class))
				info.removeFromIndexes();
			
//			for (Sentence sent : JCasUtil.select(jcas, Sentence.class)) {
//				for (Token token : JCasUtil.selectCovered(Token.class, sent)) {
//					int i = 0;
//				}
//			}
			
			try {
				boolean ready = processedInstancesManager.addJCasToInstanceB(requesterID,userQuestionID,jcas);
				if (ready) {
					JCas readyJCas = getEmptyJCas();
					processedInstancesManager.getJCasForInstanceB(requesterID,userQuestionID, readyJCas, true);
					
					AdditionalInfo newInfo = new AdditionalInfo(readyJCas);
					newInfo.setRequesterID(requesterID);
					newInfo.addToIndexes();
					
					pendingJCases.addLast(readyJCas);
				}
			} catch (UIMAException e) {
				throw new AnalysisEngineProcessException(e);
			}
		} else if (JCasUtil.exists(jcas, InstanceC.class)) {
//			String userQuestionID = getUserQuestionID(jcas);
//			try {
//				boolean ready = processedInstancesManager.addJCasToInstanceC(userQuestionID,jcas);
//				if (ready) {
//					JCas readyJCas = getEmptyJCas();
//					processedInstancesManager.getJCasForInstanceC(userQuestionID, readyJCas, true);
//					pendingJCases.addLast(readyJCas);
//				}
//			} catch (ResourceProcessException e) {
//				throw new AnalysisEngineProcessException(e);
//			}
			throw new AnalysisEngineProcessException("Task C currently not supported",null);
		} else {
			throw new AnalysisEngineProcessException("No Task specified",null);
		}
	}

}
