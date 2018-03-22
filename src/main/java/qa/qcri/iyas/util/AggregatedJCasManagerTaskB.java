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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.UIMAException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.util.CasCopier;
import org.apache.uima.util.CasCreationUtils;
import org.uimafit.util.JCasUtil;

import qa.qcri.iyas.type.cqa.InstanceB;
import qa.qcri.iyas.type.cqa.RelatedQuestion;
import qa.qcri.iyas.type.cqa.RelatedQuestionBody;
import qa.qcri.iyas.type.cqa.RelatedQuestionSubject;
import qa.qcri.iyas.type.cqa.UserQuestion;
import qa.qcri.iyas.type.cqa.UserQuestionBody;
import qa.qcri.iyas.type.cqa.UserQuestionSubject;

public class AggregatedJCasManagerTaskB extends AggregatedJCasManager {
	
	private boolean concatenated;
	private JCas userQuestionSubjectJCas;
	private JCas userQuestionBodyJCas;
	private Map<String,JCas> candidateQuestionSubjects;
	private Map<String,JCas> candidateQuestionBodies;
	private JCas aggregatedJCas;
	
	public AggregatedJCasManagerTaskB() {
		this.concatenated = false;
		this.userQuestionSubjectJCas = null;
		this.userQuestionBodyJCas = null;
		candidateQuestionSubjects = new HashMap<String,JCas>();
		candidateQuestionBodies = new HashMap<String,JCas>();
	}
	
	public AggregatedJCasManagerTaskB(JCas jcas) throws UIMAException {
		if (!JCasUtil.exists(jcas, InstanceB.class))
			throw new UIMAException("No InstanceB annotation found.", null);
		this.aggregatedJCas = jcas;
		
		Collection<UserQuestion> userQuestions = JCasUtil.select(aggregatedJCas, UserQuestion.class);
		if (userQuestions.size() > 1)
			throw new UIMAException("Only one UserQuestion annotation is allowed",null);
		if (userQuestions.size() == 0)
			throw new UIMAException("Expected one UserQuestion annotation",null);
		
		UserQuestion userQuestion = userQuestions.iterator().next();
		
		this.concatenated = userQuestion.getConcatenated();
		if (!concatenated) {
			if (!JCasUtil.exists(aggregatedJCas.getView(USER_QUESTION_SUBJECT_VIEW), UserQuestionSubject.class))
				throw new UIMAException("RelatedQuestionSubject annotation not found", null);
			this.userQuestionSubjectJCas = aggregatedJCas.getView(USER_QUESTION_SUBJECT_VIEW);
		}
		if (!JCasUtil.exists(aggregatedJCas.getView(USER_QUESTION_BODY_VIEW), UserQuestionBody.class))
			throw new UIMAException("RelatedQuestionSubject annotation not found", null);
		this.userQuestionBodyJCas = aggregatedJCas.getView(USER_QUESTION_BODY_VIEW);
		
		this.candidateQuestionSubjects = new HashMap<>();
		this.candidateQuestionBodies = new HashMap<>();
		
		for (String qID : userQuestion.getCandidateIDs().toArray()) {
			if (!concatenated) {
				if (!JCasUtil.exists(aggregatedJCas.getView(RELATED_QUESTION_SUBJECT_VIEW+"-"+qID), RelatedQuestionSubject.class))
					throw new UIMAException("RelatedQuestionSubject annotation not found", null);
				candidateQuestionSubjects.put(qID, aggregatedJCas.getView(RELATED_QUESTION_SUBJECT_VIEW+"-"+qID));
			}
			if (!JCasUtil.exists(aggregatedJCas.getView(RELATED_QUESTION_BODY_VIEW+"-"+qID), RelatedQuestionBody.class))
				throw new UIMAException("RelatedQuestionSubject annotation not found", null);
			candidateQuestionBodies.put(qID, aggregatedJCas.getView(RELATED_QUESTION_BODY_VIEW+"-"+qID));
		}
	}
	
	public boolean isConcatenated() {
		return concatenated;
	}
	
	public JCas getUserQuestionBodyJCas() {
		return userQuestionBodyJCas;
	}
	
	public Collection<JCas> getCandidatesJCases() {
		return this.candidateQuestionBodies.values();
	}
	
	/**
	 * Sets the {@link AggregatedJCasManagerTaskB#userQuestionBodyJCas} instance variable of this {@link AggregatedJCasManagerTaskB}.
	 * All the annotations and SofA are copied from the specified JCas.
	 * @param userQuestionBody
	 * @return true if the aggregated JCas is ready
	 * @throws UIMAException
	 */
	public synchronized boolean setUserQuestionBodyView(JCas userQuestionBody) throws UIMAException {
		if (this.userQuestionBodyJCas != null)
			throw new UIMAException(new IllegalStateException(
					"User question body has already been set."));
		
		if (!JCasUtil.exists(userQuestionBody, InstanceB.class))
			throw new UIMAException(new IllegalStateException(
					"The input JCas is supposed to contain an InstanceB annotation."));
		
		//Called also to check that there is only a UserQuestionBody as QAAnnotation
		UserQuestionBody body = getAnnotation(userQuestionBody,UserQuestionBody.class);
		validateUserQuestionBodyID(body);
		
		if (this.userQuestionSubjectJCas != null && body.getConcatenated())
			throw new UIMAException(new IllegalStateException(
					"UserQuestionBody is set to be concatenated while a UserQuestionSubject has already been received."));
		
		if (this.candidateQuestionBodies.size() > body.getNumberOfCandidates())
			throw new UIMAException(new IllegalStateException(
					"More candidate question bodies than expeted have already been received."));
		
		if (this.candidateQuestionSubjects.size() > body.getNumberOfCandidates())
			throw new UIMAException(new IllegalStateException(
					"More candidate question subjects than expeted have already been received."));
		
//		System.out.println("Received "+body.getID()+"'s body");
		
		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		CasCopier.copyCas(userQuestionBody.getCas(), cas, true);
		this.userQuestionBodyJCas = cas.getJCas();
		this.concatenated = body.getConcatenated();
		
		JCasUtil.select(cas.getJCas(), InstanceB.class).iterator().next().removeFromIndexes(cas.getJCas());
		
		return isReady();
	}
	
	/**
	 * Sets the {@link AggregatedJCasManagerTaskB#userQuestionSubjectJCas} instance variable of this {@link AggregatedJCasManagerTaskB}.
	 * All the annotations and SofA are copied from the specified JCas.
	 * @param relatedQuestionBodyJCas
	 * @return true if the aggregated JCas is ready
	 * @throws UIMAException
	 */
	public synchronized boolean setUserQuestionSubjectView(JCas userQuestionSubject) throws UIMAException {
		if (this.userQuestionSubjectJCas != null)
			throw new UIMAException(new IllegalStateException(
					"User question subject has already been set."));
		
		if (concatenated)
			throw new UIMAException(new IllegalStateException(
					"User question subject is not allowed to be set since the flag \"concatenated\" is true."));
		
		if (!JCasUtil.exists(userQuestionSubject, InstanceB.class))
			throw new UIMAException(new IllegalStateException(
					"The input JCas is supposed to contain an Instance	b annotation."));
		
		//Called also to check that there is only a RelatedQuestionSubject as QAAnnotation
		UserQuestionSubject subject = getAnnotation(userQuestionSubject,UserQuestionSubject.class);
		validateUserQuestioSubjectID(subject);
			
//		System.out.println("Received "+subject.getID()+"'s subject");

		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		CasCopier.copyCas(userQuestionSubject.getCas(), cas, true);
		this.userQuestionSubjectJCas = cas.getJCas();
		
		JCasUtil.select(cas.getJCas(), InstanceB.class).iterator().next().removeFromIndexes(cas.getJCas());
		
		return isReady();
	}
	
	public synchronized boolean addCandidateQuestionBodyView(JCas candidateQuestionBody) throws UIMAException {
		if (this.userQuestionBodyJCas != null) {
			UserQuestionBody body = getAnnotation(this.userQuestionBodyJCas,UserQuestionBody.class);
			if (body.getNumberOfCandidates() == this.candidateQuestionBodies.size()) {
				throw new UIMAException(new IllegalStateException(
					"The number of expected candidate question bodies has already been reached."));
			}
		}
		
		if (!JCasUtil.exists(candidateQuestionBody, InstanceB.class))
			throw new UIMAException(new IllegalStateException(
					"The input JCas is supposed to contain an InstanceB annotation."));
		
		//Called also to check that there is only a Comment as QAAnnotation
		RelatedQuestionBody candidateBody = getAnnotation(candidateQuestionBody,RelatedQuestionBody.class);
		validateRelatedQuestionBodyID(candidateBody);
		
		if (this.candidateQuestionBodies.get(candidateBody.getID()) != null)
			throw new UIMAException(new IllegalStateException(
					"The body for candidate question "+candidateBody.getID()+" has already been reached."));
		
//		System.out.println("Received candidate question body "+candidateBody.getID());
		
		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		CasCopier.copyCas(candidateQuestionBody.getCas(), cas, true);
		this.candidateQuestionBodies.put(candidateBody.getID(),cas.getJCas());
		
		JCasUtil.select(cas.getJCas(), InstanceB.class).iterator().next().removeFromIndexes(cas.getJCas());
		
		return isReady();
	}
	
	public synchronized boolean addCandidateQuestionSubjectView(JCas candidateQuestionSubject) throws UIMAException {
		if (this.userQuestionBodyJCas != null) {
			UserQuestionBody body = getAnnotation(this.userQuestionBodyJCas,UserQuestionBody.class);
			if (body.getNumberOfCandidates() == this.candidateQuestionSubjects.size()) {
				throw new UIMAException(new IllegalStateException(
					"The number of expected candidate question subjects has already been reached."));
			}
		}
		
		if (!JCasUtil.exists(candidateQuestionSubject, InstanceB.class))
			throw new UIMAException(new IllegalStateException(
					"The input JCas is supposed to contain an InstanceB annotation."));
		
		//Called also to check that there is only a Comment as QAAnnotation
		RelatedQuestionSubject candidateSubject = getAnnotation(candidateQuestionSubject,RelatedQuestionSubject.class);
		validateRelatedQuestionSubjectID(candidateSubject);
		
		if (this.candidateQuestionSubjects.get(candidateSubject.getID()) != null)
			throw new UIMAException(new IllegalStateException(
					"The subject for candidate question "+candidateSubject.getID()+" has already been reached."));
		
//		System.out.println("Received candidate question subject "+candidateSubject.getID());
		
		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		CasCopier.copyCas(candidateQuestionSubject.getCas(), cas, true);
		this.candidateQuestionSubjects.put(candidateSubject.getID(),cas.getJCas());
		
		JCasUtil.select(cas.getJCas(), InstanceB.class).iterator().next().removeFromIndexes(cas.getJCas());
		
		return isReady();
	}

	@Override
	public synchronized void getAggregatedJCas(JCas jcas) throws UIMAException {
		if (!isReady())
			throw new UIMAException(new IllegalStateException("Not ready, some JCas is still missing",null));

		UserQuestionBody userQuestBody = getAnnotation(this.userQuestionBodyJCas, UserQuestionBody.class);
		
		UserQuestion userQuestion = new UserQuestion(jcas);
		userQuestion.setConcatenated(this.concatenated);
		userQuestion.setCandidateIDs(new StringArray(jcas, userQuestBody.getNumberOfCandidates()));
		userQuestion.setID(userQuestBody.getID());
		
		CasCopier copier = new CasCopier(this.userQuestionBodyJCas.getCas(), jcas.getCas());
		copier.copyCasView(this.userQuestionBodyJCas.getCas(), jcas.getCas().createView(AggregatedJCasManager.USER_QUESTION_BODY_VIEW), true);
		userQuestionBodyJCas.release();
		
		if (!this.concatenated) {
			copier = new CasCopier(this.userQuestionSubjectJCas.getCas(), jcas.getCas());
			copier.copyCasView(this.userQuestionSubjectJCas.getCas(),
					jcas.getCas().createView(AggregatedJCasManager.USER_QUESTION_SUBJECT_VIEW), true);
			userQuestionSubjectJCas.release();
		}
		
		int i = 0;
		for (String questionID : this.candidateQuestionBodies.keySet()) {
			JCas relatedQuestView;
			try {
				relatedQuestView = jcas.createView(AggregatedJCasManager.RELATED_QUESTION_VIEW+"-"+questionID);
			} catch (CASException e) {
				throw new UIMAException(e);
			}
			
			RelatedQuestion relatedQuestion = new RelatedQuestion(relatedQuestView);
			relatedQuestion.setConcatenated(this.concatenated);
			
			copier = new CasCopier(this.candidateQuestionBodies.get(questionID).getCas(), jcas.getCas());
			copier.copyCasView(this.candidateQuestionBodies.get(questionID).getCas(),
					jcas.getCas().createView(AggregatedJCasManager.RELATED_QUESTION_BODY_VIEW+"-"+questionID), true);
			this.candidateQuestionBodies.get(questionID).release();
			
			if (!this.concatenated) {
				copier = new CasCopier(this.candidateQuestionSubjects.get(questionID).getCas(), jcas.getCas());
				copier.copyCasView(this.candidateQuestionSubjects.get(questionID).getCas(),
						jcas.getCas().createView(AggregatedJCasManager.RELATED_QUESTION_SUBJECT_VIEW+"-"+questionID), true);
				this.candidateQuestionSubjects.get(questionID).release();
			}
			
			relatedQuestion.addToIndexes();
			userQuestion.getCandidateIDs().set(i++, questionID);

		}
		
		userQuestion.addToIndexes();
		InstanceB instanceB = new InstanceB(jcas);
		instanceB.addToIndexes();
	}

	@Override
	public synchronized boolean isReady() throws UIMAException {
		if (this.userQuestionBodyJCas == null || (!this.concatenated) && this.userQuestionSubjectJCas == null)
			return false;
		
		UserQuestionBody userQuestBody = getAnnotation(this.userQuestionBodyJCas, UserQuestionBody.class);
		if (this.candidateQuestionBodies.size() != userQuestBody.getNumberOfCandidates() ||
				(!concatenated) && this.candidateQuestionSubjects.size() != userQuestBody.getNumberOfCandidates())
			return false;

		return true;
	}
	
	private void validateUserQuestionBodyID(UserQuestionBody userQuestionBody) throws UIMAException {		
		String userQuestionID = userQuestionBody.getID();
		if (userQuestionID.split("_").length != 1)
			throw new UIMAException(new IllegalArgumentException("Malformed ID."));
		
		if (this.userQuestionSubjectJCas != null &&
				!userQuestionID.equals(getAnnotation(this.userQuestionSubjectJCas, UserQuestionSubject.class).getID())) {
			throw new UIMAException(new IllegalArgumentException("The ID of the input user question body is not "
					+ "consistent with the already received user question subject."));
		} else if (!this.candidateQuestionBodies.isEmpty()) {	
			String candidateQuestionID = getAnnotation(this.candidateQuestionBodies.values().iterator().next(), 
					RelatedQuestionBody.class).getID();
			String split[] = candidateQuestionID.split("_");
			String id = split[0];
			if (!userQuestionID.equals(id))
				throw new UIMAException(new IllegalArgumentException("The ID of the input user question body is not "
						+ "consistent with the already candidatess."));
		} else if (!this.candidateQuestionSubjects.isEmpty()) {	
			String candidateQuestionID = getAnnotation(this.candidateQuestionSubjects.values().iterator().next(), 
					RelatedQuestionSubject.class).getID();
			String split[] = candidateQuestionID.split("_");
			String id = split[0];
			if (!userQuestionID.equals(id))
				throw new UIMAException(new IllegalArgumentException("The ID of the input user question body is not "
						+ "consistent with the already candidatess."));
		}
	}
	
	private void validateUserQuestioSubjectID(UserQuestionSubject userQuestionSubject) throws UIMAException {		
		String userQuestionID = userQuestionSubject.getID();
		if (userQuestionID.split("_").length != 1)
			throw new UIMAException(new IllegalArgumentException("Malformed ID."));
		
		if (this.userQuestionBodyJCas != null &&
				!userQuestionID.equals(getAnnotation(this.userQuestionBodyJCas, UserQuestionBody.class).getID())) {
			throw new UIMAException(new IllegalArgumentException("The ID of the input user question subject is not "
					+ "consistent with the already received user question body."));
		} else if (!this.candidateQuestionBodies.isEmpty()) {	
			String candidateQuestionID = getAnnotation(this.candidateQuestionBodies.values().iterator().next(), 
					RelatedQuestionBody.class).getID();
			String split[] = candidateQuestionID.split("_");
			String id = split[0];
			if (!userQuestionID.equals(id))
				throw new UIMAException(new IllegalArgumentException("The ID of the input user question body is not "
						+ "consistent with the already candidatess."));
		} else if (!this.candidateQuestionSubjects.isEmpty()) {	
			String candidateQuestionID = getAnnotation(this.candidateQuestionSubjects.values().iterator().next(), 
					RelatedQuestionSubject.class).getID();
			String split[] = candidateQuestionID.split("_");
			String id = split[0];
			if (!userQuestionID.equals(id))
				throw new UIMAException(new IllegalArgumentException("The ID of the input user question body is not "
						+ "consistent with the already candidatess."));
		}
	}

	private void validateRelatedQuestionBodyID(RelatedQuestionBody body) throws UIMAException {		
		String split[] = body.getID().split("_");
		if (split.length != 2)
			throw new UIMAException(new IllegalArgumentException("Malformed ID."));
		
		String userQuestionID = split[0];
		
		if (this.userQuestionBodyJCas != null && 
				!userQuestionID.equals(getAnnotation(this.userQuestionBodyJCas, UserQuestionBody.class).getID())) {
			throw new UIMAException(new IllegalArgumentException("The ID of the input related question body is not "
					+ "consistent with the already received user question body."));
		} else if (this.userQuestionSubjectJCas != null &&
				!userQuestionID.equals(getAnnotation(this.userQuestionSubjectJCas, UserQuestionSubject.class).getID())) {
			throw new UIMAException(new IllegalArgumentException("The ID of the input related question body is not "
					+ "consistent with the already received related question subject."));
		} else if (!this.candidateQuestionBodies.isEmpty()) {	
			String candidateQuestionID = getAnnotation(this.candidateQuestionBodies.values().iterator().next(), 
					RelatedQuestionBody.class).getID();
			String split2[] = candidateQuestionID.split("_");
			String id = split2[0];
			if (!userQuestionID.equals(id))
				throw new UIMAException(new IllegalArgumentException("The ID of the input related question body is not "
						+ "consistent with the already candidates."));
		} else if (!this.candidateQuestionSubjects.isEmpty()) {	
			String candidateQuestionID = getAnnotation(this.candidateQuestionSubjects.values().iterator().next(), 
					RelatedQuestionSubject.class).getID();
			String split2[] = candidateQuestionID.split("_");
			String id = split2[0];
			if (!userQuestionID.equals(id))
				throw new UIMAException(new IllegalArgumentException("The ID of the input related question body is not "
						+ "consistent with the already candidates."));
		}
 		
	}
	
	private void validateRelatedQuestionSubjectID(RelatedQuestionSubject subject) throws UIMAException {		
		String split[] = subject.getID().split("_");
		if (split.length != 2)
			throw new UIMAException(new IllegalArgumentException("Malformed ID."));
		
		String userQuestionID = split[0];
		
		if (this.userQuestionBodyJCas != null && 
				!userQuestionID.equals(getAnnotation(this.userQuestionBodyJCas, UserQuestionBody.class).getID())) {
			throw new UIMAException(new IllegalArgumentException("The ID of the input related question subject is not "
					+ "consistent with the already received user question body."));
		} else if (this.userQuestionSubjectJCas != null &&
				!userQuestionID.equals(getAnnotation(this.userQuestionSubjectJCas, UserQuestionSubject.class).getID())) {
			throw new UIMAException(new IllegalArgumentException("The ID of the input related question subject is not "
					+ "consistent with the already received related question subject."));
		} else if (!this.candidateQuestionBodies.isEmpty()) {	
			String candidateQuestionID = getAnnotation(this.candidateQuestionBodies.values().iterator().next(), 
					RelatedQuestionBody.class).getID();
			String split2[] = candidateQuestionID.split("_");
			String id = split2[0];
			if (!userQuestionID.equals(id))
				throw new UIMAException(new IllegalArgumentException("The ID of the input related question subject is not "
						+ "consistent with the already candidates."));
		} else if (!this.candidateQuestionSubjects.isEmpty()) {	
			String candidateQuestionID = getAnnotation(this.candidateQuestionSubjects.values().iterator().next(), 
					RelatedQuestionSubject.class).getID();
			String split2[] = candidateQuestionID.split("_");
			String id = split2[0];
			if (!userQuestionID.equals(id))
				throw new UIMAException(new IllegalArgumentException("The ID of the input related question subject is not "
						+ "consistent with the already candidates."));
		}
	}
}
