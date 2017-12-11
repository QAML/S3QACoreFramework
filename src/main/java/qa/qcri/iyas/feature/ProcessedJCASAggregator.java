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
 
 
package qa.qcri.iyas.feature;

import java.util.LinkedList;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasMultiplier_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.descriptor.OperationalProperties;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;

import qa.qcri.iyas.type.Comment;
import qa.qcri.iyas.type.InstanceA;
import qa.qcri.iyas.type.InstanceB;
import qa.qcri.iyas.type.InstanceC;
import qa.qcri.iyas.type.QAAnnotation;
import qa.qcri.iyas.type.RelatedQuestionBody;
import qa.qcri.iyas.type.RelatedQuestionSubject;
import qa.qcri.iyas.type.UserQuestionBody;
import qa.qcri.iyas.type.UserQuestionSubject;

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
	

	
	public static final String AGGREGATE_QUESTION_COMMENT_PAIRS = "aggregate-question-comment-pairs";//Subtask A
	public static final String AGGREGATE_QUESTION_QUESTION_PAIRS = "aggregate-question-questions-pairs";//Subtask B
	public static final String AGGREGATE_USERQUESTION_COMMENT_PAIRS = "aggregate-userquestion-comment-pairs";
	public static final String AGGREGATE_THREADS = "aggregate-threads";
	public static final String AGGREGATE_THREADS_AND_USER_QUESTIONS = "aggregate-threads-and-questions";
	
	public final static String PARAM_PROCESSED_INSTANCES_MANAGER_RESOURCE = "processedInstancesManager";
	@ExternalResource(key = PARAM_PROCESSED_INSTANCES_MANAGER_RESOURCE)
	private ProcessedInstancesManager processedInstancesManager;
	
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
		
		if (JCasUtil.exists(jcas, UserQuestionBody.class))
			qaAnn = getAnnotation(jcas, UserQuestionBody.class);
		else if (JCasUtil.exists(jcas, UserQuestionSubject.class))
			qaAnn = getAnnotation(jcas, UserQuestionSubject.class);
		else if (JCasUtil.exists(jcas, RelatedQuestionBody.class))
			qaAnn = getAnnotation(jcas, RelatedQuestionBody.class);
		else if (JCasUtil.exists(jcas, RelatedQuestionSubject.class))
			qaAnn = getAnnotation(jcas, RelatedQuestionSubject.class);
		else if (JCasUtil.exists(jcas, Comment.class))
			qaAnn = getAnnotation(jcas, Comment.class);
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
	
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		if (JCasUtil.exists(jcas, InstanceC.class)) {
			String userQuestionID = getUserQuestionID(jcas);
			try {
				boolean ready = processedInstancesManager.addJCasToInstanceC(userQuestionID,jcas);
				if (ready) {
					JCas readyJCas = getEmptyJCas();
					processedInstancesManager.getJCasForInstanceC(userQuestionID, readyJCas, true);
					pendingJCases.addLast(readyJCas);
				}
			} catch (CASException e) {
				e.printStackTrace();
				throw new AnalysisEngineProcessException(e.getMessage(),null);
			} catch (ResourceInitializationException e) {
				e.printStackTrace();
				throw new AnalysisEngineProcessException(e.getMessage(),null);

			}
		} else if (JCasUtil.exists(jcas, InstanceA.class)) {
			String relatedQuestionID = getRelatedQuestionID(jcas);
			try {
				boolean ready = processedInstancesManager.addJCasToInstanceA(relatedQuestionID,jcas);
				if (ready) {
					JCas readyJCas = getEmptyJCas();
					processedInstancesManager.getJCasForInstanceA(relatedQuestionID, readyJCas, true);
					pendingJCases.addLast(readyJCas);
				}
			} catch (CASException e) {
				e.printStackTrace();
				throw new AnalysisEngineProcessException(e.getMessage(),null);
			} catch (ResourceInitializationException e) {
				e.printStackTrace();
				throw new AnalysisEngineProcessException(e.getMessage(),null);

			}
		} else if (JCasUtil.exists(jcas, InstanceB.class)) {
			String userQuestionID = getUserQuestionID(jcas);
			try {
				boolean ready = processedInstancesManager.addJCasToInstanceB(userQuestionID,jcas);
				if (ready) {
					JCas readyJCas = getEmptyJCas();
					processedInstancesManager.getJCasForInstanceB(userQuestionID, readyJCas, true);
					pendingJCases.addLast(readyJCas);
				}
			} catch (CASException e) {
				e.printStackTrace();
				throw new AnalysisEngineProcessException(e.getMessage(),null);
			} catch (ResourceInitializationException e) {
				e.printStackTrace();
				throw new AnalysisEngineProcessException(e.getMessage(),null);

			}
		}
	}

}
