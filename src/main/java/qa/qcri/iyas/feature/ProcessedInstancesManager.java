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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.ExternalResourceAware;
import org.apache.uima.fit.component.initialize.ConfigurationParameterInitializer;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;
import org.apache.uima.util.CasCreationUtils;

import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.CasCopier;
import qa.qcri.iyas.type.Comment;
import qa.qcri.iyas.type.RelatedQuestion;
import qa.qcri.iyas.type.RelatedQuestionBody;
import qa.qcri.iyas.type.RelatedQuestionSubject;
import qa.qcri.iyas.type.UserQuestion;
import qa.qcri.iyas.type.UserQuestionBody;
import qa.qcri.iyas.type.UserQuestionSubject;

abstract class AbstractProcessedInstance {
	protected <T extends TOP> T getAnnotation(JCas jcas,Class<T> clazz) throws AnalysisEngineProcessException {
		FSIterator<T> it = jcas.getAllIndexedFS(clazz);
		if (!it.hasNext())
			throw new AnalysisEngineProcessException("The specified JCas does not contain any "+clazz.getName()+" annotation",null);
		T annotation = it.next();
		if (it.hasNext())
			throw new AnalysisEngineProcessException("Only one annotation is expected "+clazz.getName(),null);
		
		return annotation;
	}
}

class ProcessedInstanceA extends AbstractProcessedInstance {
	private boolean concatenated;
	private JCas relatedQuestionSubject;
	private JCas relatedQuestionBody;
	private List<JCas> comments = new LinkedList<JCas>();
	
	public boolean addJCas(JCas jcas) throws AnalysisEngineProcessException, CASException, ResourceInitializationException {
		if (JCasUtil.exists(jcas, RelatedQuestionSubject.class))
			setRelatedQuestionSubject(jcas);
		else if (JCasUtil.exists(jcas, RelatedQuestionBody.class))
			setRelatedQuestionBody(jcas);
		else if (JCasUtil.exists(jcas, Comment.class))
			setComment(jcas);
		else
			throw new AnalysisEngineProcessException("The specified JCas does not contain any of the expected annotations",null);
		
		return isReady();
	}
	
	public void getAggregatedJCas(JCas jcas) throws AnalysisEngineProcessException, CASException {
		if (!isReady())
			throw new AnalysisEngineProcessException("Not ready, some JCas is still missing",null);

		RelatedQuestionBody relQuestBody = getAnnotation(relatedQuestionBody, RelatedQuestionBody.class);
		
		RelatedQuestion relatedQuestion = new RelatedQuestion(jcas);
		relatedQuestion.setConcatenated(concatenated);
		relatedQuestion.setCandidateViewNames(new StringArray(jcas, relQuestBody.getNumberOfCandidates()));
		relatedQuestion.setID(relQuestBody.getID());
		
		CasCopier copier = new CasCopier(relatedQuestionBody.getCas(), jcas.getCas());
		copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.RELATED_QUESTION_BODY_VIEW), true);
		
		if (!concatenated) {
			copier = new CasCopier(relatedQuestionSubject.getCas(), jcas.getCas());
			copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.RELATED_QUESTION_SUBJECT_VIEW), true);
		}
		
		int j = 0;
		for (JCas commentJCas : comments) {
			Comment comment = getAnnotation(commentJCas, Comment.class);
			
			copier = new CasCopier(commentJCas.getCas(), jcas.getCas());
			copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.COMMENT_VIEW+"-"+comment.getID()), true);
			
			relatedQuestion.getCandidateViewNames().set(j++,comment.getID());
		}
		
		relatedQuestion.addToIndexes();
		
	}
	
	
	public void setRelatedQuestionSubject(JCas jcas) throws AnalysisEngineProcessException, CASException, ResourceInitializationException {
		getAnnotation(jcas, RelatedQuestionSubject.class);
		
		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		CasCopier.copyCas(jcas.getCas(), cas, true);
		relatedQuestionSubject = cas.getJCas();
	}
	
	public void setRelatedQuestionBody(JCas jcas) throws AnalysisEngineProcessException, CASException, ResourceInitializationException {
		getAnnotation(jcas, RelatedQuestionBody.class);

		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		CasCopier.copyCas(jcas.getCas(), cas, true);
		relatedQuestionBody = cas.getJCas();
	}
	
	public void setComment(JCas jcas) throws AnalysisEngineProcessException, CASException, ResourceInitializationException {
		getAnnotation(jcas, Comment.class);

		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		CasCopier.copyCas(jcas.getCas(), cas, true);
		comments.add(cas.getJCas());
	}
	
	public boolean isReady() throws AnalysisEngineProcessException {
		if (relatedQuestionBody == null || (!concatenated) && relatedQuestionSubject == null)
			return false;
		
		RelatedQuestionBody relQuestBody = getAnnotation(relatedQuestionBody, RelatedQuestionBody.class);
		if (comments.size() != relQuestBody.getNumberOfCandidates())
			return false;
		
		return true;
	}
}

class ProcessedInstanceB extends AbstractProcessedInstance {
	private JCas userQuestionSubject;
	private JCas userQuestionBody;
	private boolean concatenated;
	private Map<String,JCas> relatedQuestionSubjects = new HashMap<String,JCas>();
	private Map<String,JCas> relatedQuestionBodies = new HashMap<String,JCas>();
	
	public boolean addJCas(JCas jcas) throws AnalysisEngineProcessException, CASException, ResourceInitializationException {
		if (JCasUtil.exists(jcas, UserQuestionSubject.class))
			setUserQuestionSubject(jcas);
		else if (JCasUtil.exists(jcas, UserQuestionBody.class))
			setUserQuestionBody(jcas);
		else if (JCasUtil.exists(jcas, RelatedQuestionSubject.class))
			setRelatedQuestionSubject(jcas);
		else if (JCasUtil.exists(jcas, RelatedQuestionBody.class))
			setRelatedQuestionBody(jcas);
		else
			throw new AnalysisEngineProcessException("The specified JCas does not contain any of the expected annotations",null);
		
		return isReady();
	}
	
	public void getAggregatedJCas(JCas jcas) throws AnalysisEngineProcessException, CASException {
		if (!isReady())
			throw new AnalysisEngineProcessException("Not ready, some JCas is still missing",null);

		UserQuestionBody userQuestBody = getAnnotation(userQuestionBody, UserQuestionBody.class);
		
		UserQuestion userQuestion = new UserQuestion(jcas);
		userQuestion.setConcatenated(concatenated);
		userQuestion.setCandidateViewNames(new StringArray(jcas, userQuestBody.getNumberOfCandidates()));
		userQuestion.setID(userQuestBody.getID());
		
		CasCopier copier = new CasCopier(userQuestionBody.getCas(), jcas.getCas());
		copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.USER_QUESTION_BODY_VIEW), true);
		
		if (!concatenated) {
			copier = new CasCopier(userQuestionSubject.getCas(), jcas.getCas());
			copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.USER_QUESTION_SUBJECT_VIEW), true);
		}
		
		int i = 0;
		for (String questionID : relatedQuestionBodies.keySet()) {
			JCas relatedQuestView = jcas.createView(ProcessedInstancesManager.RELATED_QUESTION_VIEW+"-"+questionID);
			
			RelatedQuestion relatedQuestion = new RelatedQuestion(relatedQuestView);
			relatedQuestion.setConcatenated(concatenated);
			
			copier = new CasCopier(relatedQuestionBodies.get(questionID).getCas(), jcas.getCas());
			copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.RELATED_QUESTION_BODY_VIEW+"-"+questionID), true);
			
			if (!concatenated) {
				copier = new CasCopier(relatedQuestionSubjects.get(questionID).getCas(), jcas.getCas());
				copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.RELATED_QUESTION_SUBJECT_VIEW+"-"+questionID), true);
			}
			
			relatedQuestion.addToIndexes();
			userQuestion.getCandidateViewNames().set(i++, questionID);

		}
		
		userQuestion.addToIndexes();
		
	}
	
	public void setUserQuestionSubject(JCas jcas) throws AnalysisEngineProcessException, CASException, ResourceInitializationException {
		getAnnotation(jcas, UserQuestionSubject.class);

		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		this.userQuestionSubject = cas.getJCas();
		CasCopier.copyCas(jcas.getCas(), userQuestionSubject.getCas(), true);
	}
	
	public void setUserQuestionBody(JCas jcas) throws AnalysisEngineProcessException, CASException, ResourceInitializationException {
		UserQuestionBody body = getAnnotation(jcas, UserQuestionBody.class);
		
		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		this.userQuestionBody = cas.getJCas();
		this.concatenated = body.getConcatenated();
		CasCopier.copyCas(jcas.getCas(), userQuestionBody.getCas(), true);
	}
	
	public void setRelatedQuestionSubject(JCas jcas) throws AnalysisEngineProcessException, CASException, ResourceInitializationException {
		RelatedQuestionSubject subject = getAnnotation(jcas, RelatedQuestionSubject.class);
		
		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		CasCopier.copyCas(jcas.getCas(), cas, true);
		relatedQuestionSubjects.put(subject.getID(), cas.getJCas());
		
	}
	
	public void setRelatedQuestionBody(JCas jcas) throws AnalysisEngineProcessException, CASException, ResourceInitializationException {
		RelatedQuestionBody body = getAnnotation(jcas, RelatedQuestionBody.class);

		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		CasCopier.copyCas(jcas.getCas(), cas, true);
		relatedQuestionBodies.put(body.getID(), cas.getJCas());

	}

	public boolean isReady() throws AnalysisEngineProcessException {
		if (userQuestionBody == null || (!concatenated) && userQuestionSubject == null)
			return false;
		
		UserQuestionBody userQuestBody = getAnnotation(userQuestionBody, UserQuestionBody.class);
		if (relatedQuestionBodies.size() != userQuestBody.getNumberOfCandidates() ||
				(!concatenated) && relatedQuestionSubjects.size() != userQuestBody.getNumberOfCandidates())
			return false;

		return true;
	}
}

class ProcessedInstanceC extends AbstractProcessedInstance {
	private JCas userQuestionSubject;
	private JCas userQuestionBody;
	private boolean concatenated;
	private Map<String,JCas> relatedQuestionSubjects = new HashMap<String,JCas>();
	private Map<String,JCas> relatedQuestionBodies = new HashMap<String,JCas>();
	private Map<String, List<JCas>> comments = new HashMap<String, List<JCas>>();
	
	public boolean addJCas(JCas jcas) throws AnalysisEngineProcessException, CASException, ResourceInitializationException {
		if (JCasUtil.exists(jcas, UserQuestionSubject.class))
			setUserQuestionSubject(jcas);
		else if (JCasUtil.exists(jcas, UserQuestionBody.class))
			setUserQuestionBody(jcas);
		else if (JCasUtil.exists(jcas, RelatedQuestionSubject.class))
			setRelatedQuestionSubject(jcas);
		else if (JCasUtil.exists(jcas, RelatedQuestionBody.class))
			setRelatedQuestionBody(jcas);
		else if (JCasUtil.exists(jcas, Comment.class))
			setComment(jcas);
		else
			throw new AnalysisEngineProcessException("The specified JCas does not contain any of the expected annotations",null);
		
		return isReady();
	}
	
	public void getAggregatedJCas(JCas jcas) throws AnalysisEngineProcessException, CASException {
		if (!isReady())
			throw new AnalysisEngineProcessException("Not ready, some JCas is still missing",null);

		UserQuestionBody userQuestBody = getAnnotation(userQuestionBody, UserQuestionBody.class);
		
		UserQuestion userQuestion = new UserQuestion(jcas);
		userQuestion.setConcatenated(concatenated);
		userQuestion.setCandidateViewNames(new StringArray(jcas, userQuestBody.getNumberOfCandidates()));
		userQuestion.setID(userQuestBody.getID());
		
		CasCopier copier = new CasCopier(userQuestionBody.getCas(), jcas.getCas());
		copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.USER_QUESTION_BODY_VIEW), true);
		
		if (!concatenated) {
			copier = new CasCopier(userQuestionSubject.getCas(), jcas.getCas());
			copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.USER_QUESTION_SUBJECT_VIEW), true);
		}
		
		int i = 0;
		for (String questionID : relatedQuestionBodies.keySet()) {
			JCas relatedQuestView = jcas.createView(ProcessedInstancesManager.RELATED_QUESTION_VIEW+"-"+questionID);
			
			RelatedQuestionBody relQuestBody = getAnnotation(relatedQuestionBodies.get(questionID), RelatedQuestionBody.class);

			RelatedQuestion relatedQuestion = new RelatedQuestion(relatedQuestView);
			relatedQuestion.setConcatenated(concatenated);
			relatedQuestion.setCandidateViewNames(new StringArray(relatedQuestView, relQuestBody.getNumberOfCandidates()));

			copier = new CasCopier(relatedQuestionBodies.get(questionID).getCas(), jcas.getCas());
			copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.RELATED_QUESTION_BODY_VIEW+"-"+questionID), true);
			
			if (!concatenated) {
				copier = new CasCopier(relatedQuestionSubjects.get(questionID).getCas(), jcas.getCas());
				copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.RELATED_QUESTION_SUBJECT_VIEW+"-"+questionID), true);
			}
			
			int j = 0;
			for (JCas commentJCas : comments.get(questionID)) {
				Comment comment = getAnnotation(commentJCas, Comment.class);
				
				copier = new CasCopier(commentJCas.getCas(), jcas.getCas());
				copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.COMMENT_VIEW+"-"+comment.getID()), true);
				
				relatedQuestion.getCandidateViewNames().set(j++,comment.getID());
			}
			
			relatedQuestion.addToIndexes();
			userQuestion.getCandidateViewNames().set(i++, questionID);

		}
		
		userQuestion.addToIndexes();
		
	}
	
	public void setUserQuestionSubject(JCas jcas) throws AnalysisEngineProcessException, CASException, ResourceInitializationException {
		getAnnotation(jcas, UserQuestionSubject.class);

		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		this.userQuestionSubject = cas.getJCas();
		CasCopier.copyCas(jcas.getCas(), userQuestionSubject.getCas(), true);
	}
	
	public void setUserQuestionBody(JCas jcas) throws AnalysisEngineProcessException, CASException, ResourceInitializationException {
		UserQuestionBody body = getAnnotation(jcas, UserQuestionBody.class);
		
		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		this.userQuestionBody = cas.getJCas();
		this.concatenated = body.getConcatenated();
		CasCopier.copyCas(jcas.getCas(), userQuestionBody.getCas(), true);
	}
	
	public void setRelatedQuestionSubject(JCas jcas) throws AnalysisEngineProcessException, CASException, ResourceInitializationException {
		RelatedQuestionSubject subject = getAnnotation(jcas, RelatedQuestionSubject.class);
		
		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		CasCopier.copyCas(jcas.getCas(), cas, true);
		relatedQuestionSubjects.put(subject.getID(), cas.getJCas());
		
		if (comments.get(subject.getID()) == null)
			comments.put(subject.getID(), new LinkedList<JCas>());
	}
	
	public void setRelatedQuestionBody(JCas jcas) throws AnalysisEngineProcessException, CASException, ResourceInitializationException {
		RelatedQuestionBody body = getAnnotation(jcas, RelatedQuestionBody.class);

		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		CasCopier.copyCas(jcas.getCas(), cas, true);
		relatedQuestionBodies.put(body.getID(), cas.getJCas());
		
		if (comments.get(body.getID()) == null)
			comments.put(body.getID(), new LinkedList<JCas>());
	}
	
	public void setComment(JCas jcas) throws AnalysisEngineProcessException, CASException, ResourceInitializationException {
		Comment comment = getAnnotation(jcas, Comment.class);

		String split[] = comment.getID().split("_");
		String questionID = split[0]+"_"+split[1];
		if (comments.get(questionID) == null)
			comments.put(questionID, new LinkedList<JCas>());
		
		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		CasCopier.copyCas(jcas.getCas(), cas, true);
		comments.get(questionID).add(cas.getJCas());
	}
	
	public boolean isReady() throws AnalysisEngineProcessException {
		if (userQuestionBody == null || (!concatenated) && userQuestionSubject == null)
			return false;
		
		UserQuestionBody userQuestBody = getAnnotation(userQuestionBody, UserQuestionBody.class);
		if (relatedQuestionBodies.size() != userQuestBody.getNumberOfCandidates() ||
				(!concatenated) && relatedQuestionSubjects.size() != userQuestBody.getNumberOfCandidates())
			return false;
		
		for (String questionID : relatedQuestionBodies.keySet()) {
			RelatedQuestionBody relQuestBody = getAnnotation(
					relatedQuestionBodies.get(questionID), RelatedQuestionBody.class);

			if (comments.get(questionID) == null || comments.get(questionID).size() != relQuestBody.getNumberOfCandidates())
				return false;
		}
		
		return true;
	}
}

public class ProcessedInstancesManager implements SharedResourceObject,ExternalResourceAware {
	
	public static final String USER_QUESTION_BODY_VIEW = "UserQuestionBodyView";
	public static final String USER_QUESTION_SUBJECT_VIEW = "UserQuestionSubjectView";
	public static final String RELATED_QUESTION_VIEW = "RelatedQuestionView";
	public static final String RELATED_QUESTION_BODY_VIEW = "RelatedQuestionBodyView";
	public static final String RELATED_QUESTION_SUBJECT_VIEW = "RelatedQuestionSubjectView";
	public static final String COMMENT_VIEW = "CommentView";
	
	@ConfigurationParameter(name=ExternalResourceFactory.PARAM_RESOURCE_NAME)
	private String resourceName;
	
	private Map<String,ProcessedInstanceA> instancesA = new HashMap<String,ProcessedInstanceA>();
	private Map<String,ProcessedInstanceB> instancesB = new HashMap<String,ProcessedInstanceB>();
	private Map<String,ProcessedInstanceC> instancesC = new HashMap<String,ProcessedInstanceC>();
	
	public boolean addJCasToInstanceA(String id,JCas jcas) throws AnalysisEngineProcessException, CASException, ResourceInitializationException {
		synchronized (instancesA) {
			if (instancesA.get(id) == null)
				instancesA.put(id, new ProcessedInstanceA());
			
			return instancesA.get(id).addJCas(jcas);
		}
	}
	
	public void getJCasForInstanceA(String id, JCas jcas, boolean remove) throws AnalysisEngineProcessException, CASException {
		synchronized (instancesA) {
			instancesA.get(id).getAggregatedJCas(jcas);
			if (remove)
				instancesA.remove(id);
		}
	}
	
	public boolean addJCasToInstanceB(String id,JCas jcas) throws AnalysisEngineProcessException, CASException, ResourceInitializationException {
		synchronized (instancesB) {
			if (instancesB.get(id) == null)
				instancesB.put(id, new ProcessedInstanceB());
			
			return instancesB.get(id).addJCas(jcas);
		}
	}
	
	public void getJCasForInstanceB(String id, JCas jcas, boolean remove) throws AnalysisEngineProcessException, CASException {
		synchronized (instancesB) {
			instancesB.get(id).getAggregatedJCas(jcas);
			if (remove)
				instancesB.remove(id);
		}
	}
	
	public boolean addJCasToInstanceC(String id,JCas jcas) throws AnalysisEngineProcessException, CASException, ResourceInitializationException {
		synchronized (instancesC) {
			if (instancesC.get(id) == null)
				instancesC.put(id, new ProcessedInstanceC());
			
			return instancesC.get(id).addJCas(jcas);
		}
	}
	
	public void getJCasForInstanceC(String id, JCas jcas, boolean remove) throws AnalysisEngineProcessException, CASException {
		synchronized (instancesC) {
			instancesC.get(id).getAggregatedJCas(jcas);
			if (remove)
				instancesC.remove(id);
		}
	}
	
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
