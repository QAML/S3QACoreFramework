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
 
 
package qa.qcri.iyas.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.UIMAException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceProcessException;


import qa.qcri.iyas.type.cqa.Comment;
import qa.qcri.iyas.type.cqa.RelatedQuestionBody;
import qa.qcri.iyas.type.cqa.RelatedQuestionSubject;
import qa.qcri.iyas.type.cqa.UserQuestionBody;
import qa.qcri.iyas.type.cqa.UserQuestionSubject;

abstract class AbstractProcessedInstance {
	protected <T extends TOP> T getAnnotation(JCas jcas,Class<T> clazz) throws ResourceProcessException {
		FSIterator<T> it = jcas.getAllIndexedFS(clazz);
		if (!it.hasNext())
			throw new ResourceProcessException(new IllegalArgumentException(
					"The specified JCas does not contain any "+clazz.getName()+" annotation"));
		T annotation = it.next();
		if (it.hasNext())
			throw new ResourceProcessException(new IllegalArgumentException(
					"Only one annotation is expected "+clazz.getName()));
		
		return annotation;
	}
}

//class ProcessedInstanceA extends AbstractProcessedInstance {
//	private boolean concatenated;
//	private JCas relatedQuestionSubject;
//	private JCas relatedQuestionBody;
//	private List<JCas> comments = new LinkedList<JCas>();
//	
//	public boolean addJCas(JCas jcas) throws ResourceProcessException {
//			if (JCasUtil.exists(jcas, RelatedQuestionSubject.class))
//				setRelatedQuestionSubject(jcas);
//			else if (JCasUtil.exists(jcas, RelatedQuestionBody.class))
//				setRelatedQuestionBody(jcas);
//			else if (JCasUtil.exists(jcas, Comment.class))
//				setComment(jcas);
//			else
//				throw new ResourceProcessException(new IllegalArgumentException(
//						"The specified JCas does not contain any of the expected annotations"));
//		
//		return isReady();
//	}
//	
//	public void getAggregatedJCas(JCas jcas) throws ResourceProcessException {
//		if (!isReady())
//			throw new AnalysisEngineProcessException("Not ready, some JCas is still missing",null);
//		
//		RelatedQuestionBody relQuestBody = getAnnotation(relatedQuestionBody, RelatedQuestionBody.class);
//		
//		RelatedQuestion relatedQuestion = new RelatedQuestion(jcas);
//		relatedQuestion.setConcatenated(concatenated);
//		relatedQuestion.setCandidateViewNames(new StringArray(jcas, relQuestBody.getNumberOfCandidates()));
//		relatedQuestion.setID(relQuestBody.getID());
//		
//		
//		CasCopier copier = new CasCopier(relatedQuestionBody.getCas(), jcas.getCas());
//		copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.RELATED_QUESTION_BODY_VIEW), true);
//		
//		if (!concatenated) {
//			copier = new CasCopier(relatedQuestionSubject.getCas(), jcas.getCas());
//			copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.RELATED_QUESTION_SUBJECT_VIEW), true);
//		}
//		
//		int j = 0;
//		for (JCas commentJCas : comments) {
//			Comment comment = getAnnotation(commentJCas, Comment.class);
//			
//			copier = new CasCopier(commentJCas.getCas(), jcas.getCas());
//			copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.COMMENT_VIEW+"-"+comment.getID()), true);
//			
//			relatedQuestion.getCandidateViewNames().set(j++,comment.getID());
//		}
//		
//		relatedQuestion.addToIndexes();
//		
//	}
//	
//	
//	public void setRelatedQuestionSubject(JCas jcas) throws ResourceProcessException {
//		getAnnotation(jcas, RelatedQuestionSubject.class);
//		
//		try {
//			CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
//					null, null);
//			CasCopier.copyCas(jcas.getCas(), cas, true);
//			relatedQuestionSubject = cas.getJCas();
//		} catch (ResourceInitializationException | CASException e) {
//			throw new ResourceProcessException(e);
//		}
//		
//	}
//	
//	public void setRelatedQuestionBody(JCas jcas) throws ResourceProcessException {
//		getAnnotation(jcas, RelatedQuestionBody.class);
//
//		try {
//			CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
//					null, null);
//			CasCopier.copyCas(jcas.getCas(), cas, true);
//			relatedQuestionBody = cas.getJCas();
//		} catch (ResourceInitializationException | CASException e) {
//			throw new ResourceProcessException(e);
//		}
//	}
//	
//	public void setComment(JCas jcas) throws ResourceProcessException {
//		getAnnotation(jcas, Comment.class);
//
//		try {
//			CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
//					null, null);
//			CasCopier.copyCas(jcas.getCas(), cas, true);
//			comments.add(cas.getJCas());
//		} catch (ResourceInitializationException | CASException e) {
//			throw new ResourceProcessException(e);
//		}
//	}
//	
//	public boolean isReady() throws ResourceProcessException {
//		if (relatedQuestionBody == null || (!concatenated) && relatedQuestionSubject == null)
//			return false;
//		
//		RelatedQuestionBody relQuestBody = getAnnotation(relatedQuestionBody, RelatedQuestionBody.class);
//		if (comments.size() != relQuestBody.getNumberOfCandidates())
//			return false;
//		
//		return true;
//	}
//}
//
//class ProcessedInstanceB extends AbstractProcessedInstance {
//	private JCas userQuestionSubject;
//	private JCas userQuestionBody;
//	private boolean concatenated;
//	private Map<String,JCas> relatedQuestionSubjects = new HashMap<String,JCas>();
//	private Map<String,JCas> relatedQuestionBodies = new HashMap<String,JCas>();
//	
//	public boolean addJCas(JCas jcas) throws ResourceProcessException {
//		if (JCasUtil.exists(jcas, UserQuestionSubject.class))
//			setUserQuestionSubject(jcas);
//		else if (JCasUtil.exists(jcas, UserQuestionBody.class))
//			setUserQuestionBody(jcas);
//		else if (JCasUtil.exists(jcas, RelatedQuestionSubject.class))
//			setRelatedQuestionSubject(jcas);
//		else if (JCasUtil.exists(jcas, RelatedQuestionBody.class))
//			setRelatedQuestionBody(jcas);
//		else
//			throw new ResourceProcessException(new IllegalArgumentException(
//					"The specified JCas does not contain any of the expected annotations"));
//		
//		return isReady();
//	}
//	
//	public void getAggregatedJCas(JCas jcas) throws ResourceProcessException {
//		if (!isReady())
//			throw new AnalysisEngineProcessException("Not ready, some JCas is still missing",null);
//
//		UserQuestionBody userQuestBody = getAnnotation(userQuestionBody, UserQuestionBody.class);
//		
//		UserQuestion userQuestion = new UserQuestion(jcas);
//		userQuestion.setConcatenated(concatenated);
//		userQuestion.setCandidateViewNames(new StringArray(jcas, userQuestBody.getNumberOfCandidates()));
//		userQuestion.setID(userQuestBody.getID());
//		
//		CasCopier copier = new CasCopier(userQuestionBody.getCas(), jcas.getCas());
//		copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.USER_QUESTION_BODY_VIEW), true);
//		
//		if (!concatenated) {
//			copier = new CasCopier(userQuestionSubject.getCas(), jcas.getCas());
//			copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.USER_QUESTION_SUBJECT_VIEW), true);
//		}
//		
//		int i = 0;
//		for (String questionID : relatedQuestionBodies.keySet()) {
//			JCas relatedQuestView;
//			try {
//				relatedQuestView = jcas.createView(ProcessedInstancesManager.RELATED_QUESTION_VIEW+"-"+questionID);
//			} catch (CASException e) {
//				throw new ResourceProcessException(e);
//			}
//			
//			RelatedQuestion relatedQuestion = new RelatedQuestion(relatedQuestView);
//			relatedQuestion.setConcatenated(concatenated);
//			
//			copier = new CasCopier(relatedQuestionBodies.get(questionID).getCas(), jcas.getCas());
//			copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.RELATED_QUESTION_BODY_VIEW+"-"+questionID), true);
//			
//			if (!concatenated) {
//				copier = new CasCopier(relatedQuestionSubjects.get(questionID).getCas(), jcas.getCas());
//				copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.RELATED_QUESTION_SUBJECT_VIEW+"-"+questionID), true);
//			}
//			
//			relatedQuestion.addToIndexes();
//			userQuestion.getCandidateViewNames().set(i++, questionID);
//
//		}
//		
//		userQuestion.addToIndexes();
//		
//	}
//	
//	public void setUserQuestionSubject(JCas jcas) throws ResourceProcessException {
//		getAnnotation(jcas, UserQuestionSubject.class);
//
//		try {
//			CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
//					null, null);
//			this.userQuestionSubject = cas.getJCas();
//			CasCopier.copyCas(jcas.getCas(), userQuestionSubject.getCas(), true);
//		} catch (ResourceInitializationException | CASException e) {
//			throw new ResourceProcessException(e);
//		}
//	}
//	
//	public void setUserQuestionBody(JCas jcas) throws ResourceProcessException {
//		UserQuestionBody body = getAnnotation(jcas, UserQuestionBody.class);
//		
//		try {
//			CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
//					null, null);
//			this.userQuestionBody = cas.getJCas();
//			this.concatenated = body.getConcatenated();
//			CasCopier.copyCas(jcas.getCas(), userQuestionBody.getCas(), true);
//		} catch (ResourceInitializationException | CASException e) {
//			throw new ResourceProcessException(e);
//		}
//	}
//	
//	public void setRelatedQuestionSubject(JCas jcas) throws ResourceProcessException {
//		RelatedQuestionSubject subject = getAnnotation(jcas, RelatedQuestionSubject.class);
//		
//		try {
//			CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
//					null, null);
//			CasCopier.copyCas(jcas.getCas(), cas, true);
//			relatedQuestionSubjects.put(subject.getID(), cas.getJCas());
//		} catch (ResourceInitializationException | CASException e) {
//			throw new ResourceProcessException(e);
//		}
//		
//	}
//	
//	public void setRelatedQuestionBody(JCas jcas) throws ResourceProcessException {
//		RelatedQuestionBody body = getAnnotation(jcas, RelatedQuestionBody.class);
//
//		try {
//			CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
//					null, null);
//			CasCopier.copyCas(jcas.getCas(), cas, true);
//			relatedQuestionBodies.put(body.getID(), cas.getJCas());
//		} catch (ResourceInitializationException | CASException e) {
//			throw new ResourceProcessException(e);
//		}
//
//	}
//
//	public boolean isReady() throws ResourceProcessException {
//		if (userQuestionBody == null || (!concatenated) && userQuestionSubject == null)
//			return false;
//		
//		UserQuestionBody userQuestBody = getAnnotation(userQuestionBody, UserQuestionBody.class);
//		if (relatedQuestionBodies.size() != userQuestBody.getNumberOfCandidates() ||
//				(!concatenated) && relatedQuestionSubjects.size() != userQuestBody.getNumberOfCandidates())
//			return false;
//
//		return true;
//	}
//}
//
//class ProcessedInstanceC extends AbstractProcessedInstance {
//	private JCas userQuestionSubject;
//	private JCas userQuestionBody;
//	private boolean concatenated;
//	private Map<String,JCas> relatedQuestionSubjects = new HashMap<String,JCas>();
//	private Map<String,JCas> relatedQuestionBodies = new HashMap<String,JCas>();
//	private Map<String, List<JCas>> comments = new HashMap<String, List<JCas>>();
//	
//	public boolean addJCas(JCas jcas) throws ResourceProcessException {
//		if (JCasUtil.exists(jcas, UserQuestionSubject.class))
//			setUserQuestionSubject(jcas);
//		else if (JCasUtil.exists(jcas, UserQuestionBody.class))
//			setUserQuestionBody(jcas);
//		else if (JCasUtil.exists(jcas, RelatedQuestionSubject.class))
//			setRelatedQuestionSubject(jcas);
//		else if (JCasUtil.exists(jcas, RelatedQuestionBody.class))
//			setRelatedQuestionBody(jcas);
//		else if (JCasUtil.exists(jcas, Comment.class))
//			setComment(jcas);
//		else
//			throw new ResourceProcessException(new IllegalArgumentException(
//					"The specified JCas does not contain any of the expected annotations"));
//		
//		return isReady();
//	}
//	
//	public void getAggregatedJCas(JCas jcas) throws ResourceProcessException {
//		if (!isReady())
//			throw new AnalysisEngineProcessException("Not ready, some JCas is still missing",null);
//
//		UserQuestionBody userQuestBody = getAnnotation(userQuestionBody, UserQuestionBody.class);
//		
//		UserQuestion userQuestion = new UserQuestion(jcas);
//		userQuestion.setConcatenated(concatenated);
//		userQuestion.setCandidateIDs(new StringArray(jcas, userQuestBody.getNumberOfCandidates()));
//		userQuestion.setID(userQuestBody.getID());
//		
//		CasCopier copier = new CasCopier(userQuestionBody.getCas(), jcas.getCas());
//		copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.USER_QUESTION_BODY_VIEW), true);
//		
//		if (!concatenated) {
//			copier = new CasCopier(userQuestionSubject.getCas(), jcas.getCas());
//			copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.USER_QUESTION_SUBJECT_VIEW), true);
//		}
//		
//		int i = 0;
//		for (String questionID : relatedQuestionBodies.keySet()) {
//			JCas relatedQuestView;
//			try {
//				relatedQuestView = jcas.createView(ProcessedInstancesManager.RELATED_QUESTION_VIEW+"-"+questionID);
//			} catch (CASException e) {
//				throw new ResourceProcessException(e);
//			}
//			
//			RelatedQuestionBody relQuestBody = getAnnotation(relatedQuestionBodies.get(questionID), RelatedQuestionBody.class);
//
//			RelatedQuestion relatedQuestion = new RelatedQuestion(relatedQuestView);
//			relatedQuestion.setConcatenated(concatenated);
//			relatedQuestion.setCandidateIDs(new StringArray(relatedQuestView, relQuestBody.getNumberOfCandidates()));
//
//			copier = new CasCopier(relatedQuestionBodies.get(questionID).getCas(), jcas.getCas());
//			copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.RELATED_QUESTION_BODY_VIEW+"-"+questionID), true);
//			
//			if (!concatenated) {
//				copier = new CasCopier(relatedQuestionSubjects.get(questionID).getCas(), jcas.getCas());
//				copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.RELATED_QUESTION_SUBJECT_VIEW+"-"+questionID), true);
//			}
//			
//			int j = 0;
//			for (JCas commentJCas : comments.get(questionID)) {
//				Comment comment = getAnnotation(commentJCas, Comment.class);
//				
//				copier = new CasCopier(commentJCas.getCas(), jcas.getCas());
//				copier.copyCasView(jcas.getCas().createView(ProcessedInstancesManager.COMMENT_VIEW+"-"+comment.getID()), true);
//				
//				relatedQuestion.getCandidateIDs().set(j++,comment.getID());
//			}
//			
//			relatedQuestion.addToIndexes();
//			userQuestion.getCandidateIDs().set(i++, questionID);
//
//		}
//		
//		userQuestion.addToIndexes();
//		
//	}
//	
//	public void setUserQuestionSubject(JCas jcas) throws ResourceProcessException {
//		getAnnotation(jcas, UserQuestionSubject.class);
//
//		try {
//			CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
//					null, null);
//			this.userQuestionSubject = cas.getJCas();
//			CasCopier.copyCas(jcas.getCas(), userQuestionSubject.getCas(), true);
//		} catch (ResourceInitializationException | CASException e) {
//			throw new ResourceProcessException(e);
//		}
//	}
//	
//	public void setUserQuestionBody(JCas jcas) throws ResourceProcessException {
//		UserQuestionBody body = getAnnotation(jcas, UserQuestionBody.class);
//		
//		try {
//			CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
//					null, null);
//			this.userQuestionBody = cas.getJCas();
//			this.concatenated = body.getConcatenated();
//			CasCopier.copyCas(jcas.getCas(), userQuestionBody.getCas(), true);
//		} catch (ResourceInitializationException | CASException e) {
//			throw new ResourceProcessException(e);
//		}
//	}
//	
//	public void setRelatedQuestionSubject(JCas jcas) throws ResourceProcessException {
//		RelatedQuestionSubject subject = getAnnotation(jcas, RelatedQuestionSubject.class);
//		
//		try {
//			CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
//					null, null);
//			CasCopier.copyCas(jcas.getCas(), cas, true);
//			relatedQuestionSubjects.put(subject.getID(), cas.getJCas());
//			
//			if (comments.get(subject.getID()) == null)
//				comments.put(subject.getID(), new LinkedList<JCas>());
//		} catch (ResourceInitializationException | CASException e) {
//			throw new ResourceProcessException(e);
//		}
//	}
//	
//	public void setRelatedQuestionBody(JCas jcas) throws ResourceProcessException {
//		RelatedQuestionBody body = getAnnotation(jcas, RelatedQuestionBody.class);
//
//		try {
//			CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
//					null, null);
//			CasCopier.copyCas(jcas.getCas(), cas, true);
//			relatedQuestionBodies.put(body.getID(), cas.getJCas());
//			
//			if (comments.get(body.getID()) == null)
//				comments.put(body.getID(), new LinkedList<JCas>());
//		} catch (ResourceInitializationException | CASException e) {
//			throw new ResourceProcessException(e);
//		}
//	}
//	
//	public void setComment(JCas jcas) throws ResourceProcessException {
//		Comment comment = getAnnotation(jcas, Comment.class);
//
//		try {
//			String split[] = comment.getID().split("_");
//			String questionID = split[0]+"_"+split[1];
//			if (comments.get(questionID) == null)
//				comments.put(questionID, new LinkedList<JCas>());
//			
//			CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
//					null, null);
//			CasCopier.copyCas(jcas.getCas(), cas, true);
//			comments.get(questionID).add(cas.getJCas());
//		} catch (ResourceInitializationException | CASException e) {
//			throw new ResourceProcessException(e);
//		}
//	}
//	
//	public boolean isReady() throws ResourceProcessException {
//		if (userQuestionBody == null || (!concatenated) && userQuestionSubject == null)
//			return false;
//		
//		UserQuestionBody userQuestBody = getAnnotation(userQuestionBody, UserQuestionBody.class);
//		if (relatedQuestionBodies.size() != userQuestBody.getNumberOfCandidates() ||
//				(!concatenated) && relatedQuestionSubjects.size() != userQuestBody.getNumberOfCandidates())
//			return false;
//		
//		for (String questionID : relatedQuestionBodies.keySet()) {
//			RelatedQuestionBody relQuestBody = getAnnotation(
//					relatedQuestionBodies.get(questionID), RelatedQuestionBody.class);
//
//			if (comments.get(questionID) == null || comments.get(questionID).size() != relQuestBody.getNumberOfCandidates())
//				return false;
//		}
//		
//		return true;
//	}
//}

//TODO make the class more robust checking status of in classes ProcessedInstanceA, ProcessedInstanceB and ProcessedInstanceC
//(Expected annotation, IDs coherence etc)
public class ProcessedInstancesManager 
//implements SharedResourceObject, ExternalResourceAware 
{
	
	public static final String USER_QUESTION_BODY_VIEW = "UserQuestionBodyView";
	public static final String USER_QUESTION_SUBJECT_VIEW = "UserQuestionSubjectView";
	public static final String RELATED_QUESTION_VIEW = "RelatedQuestionView";
	public static final String RELATED_QUESTION_BODY_VIEW = "RelatedQuestionBodyView";
	public static final String RELATED_QUESTION_SUBJECT_VIEW = "RelatedQuestionSubjectView";
	public static final String COMMENT_VIEW = "CommentView";
	
	@ConfigurationParameter(name=ExternalResourceFactory.PARAM_RESOURCE_NAME)
	private String resourceName;
	
	private Map<String,AggregatedJCasManagerTaskA> instancesA = new HashMap<String,AggregatedJCasManagerTaskA>();
	private Map<String,AggregatedJCasManagerTaskB> instancesB = new HashMap<String,AggregatedJCasManagerTaskB>();
	
	public boolean addJCasToInstanceA(String requesterID, String id,JCas jcas) throws ResourceProcessException {
		synchronized (instancesA) {
			String globalID = requesterID+id;
			if (instancesA.get(globalID) == null)
				instancesA.put(globalID, new AggregatedJCasManagerTaskA());
			
			AggregatedJCasManagerTaskA manager = instancesA.get(globalID);
			
			try { 
				if (JCasUtil.exists(jcas, RelatedQuestionSubject.class))
					return manager.setRelatedQuestionSubjectView(jcas);
				else if (JCasUtil.exists(jcas, RelatedQuestionBody.class))
					return manager.setRelatedQuestionBodyView(jcas);
				else if (JCasUtil.exists(jcas, Comment.class))
					return manager.addCommentView(jcas);
				else
					throw new IllegalArgumentException(
							"The specified JCas does not contain any of the expected annotations");
			} catch (UIMAException | IllegalArgumentException e) {
				throw new ResourceProcessException(e);
			}
			
		}
	}
	
	public void getJCasForInstanceA(String requesterID, String id, JCas jcas, boolean remove) throws UIMAException {
		synchronized (instancesA) {
			String globalID = requesterID+id;
			instancesA.get(globalID).getAggregatedJCas(jcas);
			if (remove)
				instancesA.remove(globalID);
		}
	}
	
	public boolean addJCasToInstanceB(String requesterID, String id,JCas jcas) throws ResourceProcessException {
		synchronized (instancesB) {
			String globalID = requesterID+id;
			if (instancesB.get(globalID) == null)
				instancesB.put(globalID, new AggregatedJCasManagerTaskB());
			
			AggregatedJCasManagerTaskB manager = instancesB.get(globalID);
			
			try { 
				if (JCasUtil.exists(jcas, UserQuestionSubject.class))
					return manager.setUserQuestionSubjectView(jcas);
				if (JCasUtil.exists(jcas, UserQuestionBody.class))
					return manager.setUserQuestionBodyView(jcas);
				if (JCasUtil.exists(jcas, RelatedQuestionSubject.class))
					return manager.addCandidateQuestionSubjectView(jcas);
				else if (JCasUtil.exists(jcas, RelatedQuestionBody.class))
					return manager.addCandidateQuestionBodyView(jcas);
				else
					throw new IllegalArgumentException(
							"The specified JCas does not contain any of the expected annotations");
			} catch (UIMAException | IllegalArgumentException e) {
				throw new ResourceProcessException(e);
			}
		}
	}
	
	public void getJCasForInstanceB(String requesterID, String id, JCas jcas, boolean remove) throws UIMAException {
		synchronized (instancesB) {
			String globalID = requesterID+id;
			instancesB.get(globalID).getAggregatedJCas(jcas);
			if (remove)
				instancesB.remove(globalID);
		}
	}
	
//	@Override
//	public void load(DataResource data) throws ResourceInitializationException {
//		ConfigurationParameterInitializer.initialize(this, data);
//	}
//
//	@Override
//	public String getResourceName() {
//		return this.resourceName;
//	}
//
//	@Override
//	public void afterResourcesInitialized() throws ResourceInitializationException {
//		// TODO Auto-generated method stub	
//	}
	
}
