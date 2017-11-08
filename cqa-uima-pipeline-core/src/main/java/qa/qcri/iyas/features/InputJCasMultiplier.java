package qa.qcri.iyas.features;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.component.JCasMultiplier_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.descriptor.OperationalProperties;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.jdom2.input.sax.XMLReaderXSDFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import qa.qcri.iyas.data.preprocessing.TextPreprocessor;
import qa.qcri.iyas.data.readers.DataReader;
import qa.qcri.iyas.types.Comment;
import qa.qcri.iyas.types.Instance;
import qa.qcri.iyas.types.InstanceA;
import qa.qcri.iyas.types.RelatedQuestionBody;
import qa.qcri.iyas.types.RelatedQuestionSubject;
import qa.qcri.iyas.types.UserQuestionBody;
import qa.qcri.iyas.types.UserQuestionSubject;

@OperationalProperties(modifiesCas = false, outputsNewCases = true, multipleDeploymentAllowed = true)
@TypeCapability(
		inputs = {"qa.qcri.iyas.types.UserQuestionSubject",
				   "qa.qcri.iyas.types.UserQuestionBody",
				   "qa.qcri.iyas.types.RelatedQuestionSubject",
				   "qa.qcri.iyas.types.RelatedQuestionBody",
				   "qa.qcri.iyas.types.Comment"},
		
		outputs = {"qa.qcri.iyas.types.UserQuestionSubject",
				   "qa.qcri.iyas.types.UserQuestionBody",
				   "qa.qcri.iyas.types.RelatedQuestionSubject",
				   "qa.qcri.iyas.types.RelatedQuestionBody",
				   "qa.qcri.iyas.types.Comment"}
)
public class InputJCasMultiplier extends JCasMultiplier_ImplBase {
	
	//TODO release the CASes in case of error
	private static enum Status{USER_QUESTION_SUBJECT,USER_QUESTION_BODY,RELATED_QUESTION_SUBJECT,RELATED_QUESTION_BODY,COMMENT};
	private Status status = null;
	private static enum TypeOfInstance{INSTANCE,INSTANCE_A,INSTANCE_B,INSTANCE_C};
	
	public static final String PREPROCESSOR_EXTERNAL_RESOURCE = "preprocessor";
	public static final String CONCATENATE_PARAM = "concatenate";
	
	private LinkedList<MyElement> currentElements = null;
	private SAXBuilder saxBuilder = null;
	
	@ExternalResource(key = PREPROCESSOR_EXTERNAL_RESOURCE)
	private TextPreprocessor preprocessor = null;
	
	@ConfigurationParameter(name = CONCATENATE_PARAM, defaultValue = "false", mandatory = false)
	private boolean concatenateSubjectAndBody;
	
	private class MyElement {
		public TypeOfInstance typeOfInstance;
		public Element element;
		public MyElement(Element element,TypeOfInstance typeOfInstance) {
			this.element = element;
			this.typeOfInstance = typeOfInstance;
		}
		public String toString() {
			return element.getName()+" "+typeOfInstance;
		}
	}
	
	private void parseInstance(Element instance) throws ResourceConfigurationException, ResourceInitializationException, AnalysisEngineProcessException {
		if (instance.getName().equals(DataReader.INSTANCE_TAG)) {
			Element userQuestion = instance.getChild(DataReader.USER_QUESTION_TAG);
			currentElements.addLast(new MyElement(userQuestion,TypeOfInstance.INSTANCE));
			
			String userQuestionID = userQuestion.getAttributeValue(DataReader.ID_ATTRIBUTE);
			if (userQuestionID.split("_").length != 1)
				throw new AnalysisEngineProcessException("The user question ID does not satisfy the requirements",null);
			for (Element relatedQuestion : instance.getChildren(DataReader.RELATED_QUESTION_TAG)) {
				String relatedQuestionID = relatedQuestion.getAttributeValue(DataReader.ID_ATTRIBUTE);
				if (relatedQuestionID.split("_").length != 2 || !relatedQuestionID.startsWith(userQuestionID))
					throw new AnalysisEngineProcessException("The related question ID does not satisfy the requirements",null);
				currentElements.addLast(new MyElement(relatedQuestion,TypeOfInstance.INSTANCE));
				for (Element comment : relatedQuestion.getChildren(DataReader.COMMENT_TAG)) {
					String commentID = comment.getAttributeValue(DataReader.ID_ATTRIBUTE);
					if (commentID.split("_").length != 3 || !commentID.startsWith(relatedQuestionID))
						throw new AnalysisEngineProcessException("The comment question ID does not satisfy the requirements",null);
					currentElements.addLast(new MyElement(comment,TypeOfInstance.INSTANCE));
				}
			}
			status = Status.USER_QUESTION_SUBJECT;
		} else if (instance.getName().equals(DataReader.INSTANCE_A_TAG)) {
			Element relatedQuestion = instance.getChild(DataReader.RELATED_QUESTION_TAG);
			String relatedQuestionID = relatedQuestion.getAttributeValue(DataReader.ID_ATTRIBUTE);
			if (relatedQuestionID.split("_").length != 2)
				throw new AnalysisEngineProcessException("The related question ID does not satisfy the requirements",null);
			currentElements.addLast(new MyElement(relatedQuestion,TypeOfInstance.INSTANCE_A));
			for (Element comment : relatedQuestion.getChildren(DataReader.COMMENT_TAG)) {
				String commentID = comment.getAttributeValue(DataReader.ID_ATTRIBUTE);
				if (commentID.split("_").length != 3 || !commentID.startsWith(relatedQuestionID))
					throw new AnalysisEngineProcessException("The comment question ID does not satisfy the requirements",null);
				currentElements.addLast(new MyElement(comment,TypeOfInstance.INSTANCE_A));
			}
			status = Status.RELATED_QUESTION_SUBJECT;
		}
	}
	
	public String concatenateBodyAndSubject(String subject,String body) {
		if (body.toLowerCase().startsWith(subject.toLowerCase())) {
			return body;
		}
		if (body.length() > 0) {
			if (Character.isUpperCase(body.charAt(0))) {
				return subject+ ". " + body; 
		    }
		}
		return subject+ " " + body;
	}
	
	@Override
	public void initialize(final UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		XMLReaderJDOMFactory factory;
		try {
			factory = new XMLReaderXSDFactory(new File(DataReader.SCHEMA_PATH));
			saxBuilder = new SAXBuilder(factory);
		} catch (JDOMException e) {
			e.printStackTrace();
			throw new ResourceInitializationException(e.getMessage(), null);
		}
		currentElements = new LinkedList<MyElement>();
	}
	
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {	
		try {
			Document document = saxBuilder.build(new StringReader(jcas.getDocumentText()));
			Element root = document.getRootElement();
			Element instance = root.getChildren().get(0);
			parseInstance(instance);
						
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(jcas.getDocumentText());
			throw new AnalysisEngineProcessException(e.getMessage(),null);
		}
	}

	@Override
	public boolean hasNext() throws AnalysisEngineProcessException {
		return !currentElements.isEmpty();
	}
	
	private JCas getUserQuestion(Element userQuestion) {
		String id = userQuestion.getAttributeValue(DataReader.ID_ATTRIBUTE);
		String lang = userQuestion.getAttributeValue(DataReader.LANG_ATTRIBUTE);
		int numberOfCandidates = Integer.parseInt(userQuestion.getAttributeValue(DataReader.NUMBER_OF_CANDIDATES_ATTRIBUTE));
		
		String subject = preprocessor.preprocess(userQuestion.getChild(DataReader.SUBJECT_TAG).getText(),lang);
		String body = preprocessor.preprocess(userQuestion.getChild(DataReader.BODY_TAG).getText(),lang);
		String question = concatenateBodyAndSubject(subject, body);
		
		JCas questionJCas = getEmptyJCas();
		questionJCas.setDocumentLanguage(lang);
		questionJCas.setDocumentText(question);
		UserQuestionBody questionAnnotation = new UserQuestionBody(questionJCas, 0, question.length());
		questionAnnotation.setNumberOfCandidates(numberOfCandidates);
		questionAnnotation.setID(id);
		questionAnnotation.setConcatenated(true);
		questionAnnotation.addToIndexes();
		
		return questionJCas;
	}
	
	private JCas getUserQuestionSubject(Element userQuestion) {
		String id = userQuestion.getAttributeValue(DataReader.ID_ATTRIBUTE);
		String lang = userQuestion.getAttributeValue(DataReader.LANG_ATTRIBUTE);
		
		String subject = preprocessor.preprocess(userQuestion.getChild(DataReader.SUBJECT_TAG).getText(),lang);
		JCas subjectJCas = getEmptyJCas();
		subjectJCas.setDocumentLanguage(lang);
		subjectJCas.setDocumentText(subject);
		UserQuestionSubject subjectAnnotation = new UserQuestionSubject(subjectJCas, 0, subject.length());
		subjectAnnotation.setID(id);
		subjectAnnotation.addToIndexes();
		
		return subjectJCas;
	}
	
	private JCas getUserQuestionBody(Element userQuestion) {
		String id = userQuestion.getAttributeValue(DataReader.ID_ATTRIBUTE);
		String lang = userQuestion.getAttributeValue(DataReader.LANG_ATTRIBUTE);
		int numberOfCandidates = Integer.parseInt(userQuestion.getAttributeValue(DataReader.NUMBER_OF_CANDIDATES_ATTRIBUTE));
		
		String body = preprocessor.preprocess(userQuestion.getChild(DataReader.BODY_TAG).getText(),lang);
		JCas bodyJCas = getEmptyJCas();
		bodyJCas.setDocumentLanguage(lang);
		bodyJCas.setDocumentText(body);
		UserQuestionBody bodyAnnotation = new UserQuestionBody(bodyJCas, 0, body.length());
		bodyAnnotation.setNumberOfCandidates(numberOfCandidates);
		bodyAnnotation.setID(id);
		bodyAnnotation.addToIndexes();
		
		return bodyJCas;
	}
	
	private JCas getRelatedQuestion(Element relatedQuestion) {
		String id = relatedQuestion.getAttributeValue(DataReader.ID_ATTRIBUTE);
		String lang = relatedQuestion.getAttributeValue(DataReader.LANG_ATTRIBUTE);
		int numberOfCandidates = Integer.parseInt(relatedQuestion.getAttributeValue(DataReader.NUMBER_OF_CANDIDATES_ATTRIBUTE));
		
		String subject = preprocessor.preprocess(relatedQuestion.getChild(DataReader.SUBJECT_TAG).getText(),lang);
		String body = preprocessor.preprocess(relatedQuestion.getChild(DataReader.BODY_TAG).getText(),lang);
		String question = concatenateBodyAndSubject(subject, body);
		
		JCas questionJCas = getEmptyJCas();
		questionJCas.setDocumentLanguage(lang);
		questionJCas.setDocumentText(question);
		RelatedQuestionBody questionAnnotation = new RelatedQuestionBody(questionJCas, 0, question.length());
		questionAnnotation.setNumberOfCandidates(numberOfCandidates);
		questionAnnotation.setID(id);
		questionAnnotation.setConcatenated(true);
		questionAnnotation.addToIndexes();
		
		return questionJCas;
	}
	
	private JCas getRelatedQuestionSubject(Element relatedQuestion) {
		String id = relatedQuestion.getAttributeValue(DataReader.ID_ATTRIBUTE);
		String lang = relatedQuestion.getAttributeValue(DataReader.LANG_ATTRIBUTE);
		
		String subject = preprocessor.preprocess(relatedQuestion.getChild(DataReader.SUBJECT_TAG).getText(),lang);
		JCas subjectJCas = getEmptyJCas();
		subjectJCas.setDocumentLanguage(lang);
		subjectJCas.setDocumentText(subject);
		RelatedQuestionSubject subjectAnnotation = new RelatedQuestionSubject(subjectJCas, 0, subject.length());
		subjectAnnotation.setID(id);
		subjectAnnotation.addToIndexes();
		
		return subjectJCas;
	}
	
	private JCas getRelatedQuestionBody(Element relatedQuestion) {
		String id = relatedQuestion.getAttributeValue(DataReader.ID_ATTRIBUTE);
		String lang = relatedQuestion.getAttributeValue(DataReader.LANG_ATTRIBUTE);
		int numberOfCandidates = Integer.parseInt(relatedQuestion.getAttributeValue(DataReader.NUMBER_OF_CANDIDATES_ATTRIBUTE));
		
		String body = preprocessor.preprocess(relatedQuestion.getChild(DataReader.BODY_TAG).getText(),lang);
		JCas bodyJCas = getEmptyJCas();
		bodyJCas.setDocumentLanguage(lang);
		bodyJCas.setDocumentText(body);
		RelatedQuestionBody bodyAnnotation = new RelatedQuestionBody(bodyJCas, 0, body.length());
		bodyAnnotation.setNumberOfCandidates(numberOfCandidates);
		bodyAnnotation.setID(id);
		bodyAnnotation.addToIndexes();
		
		return bodyJCas;
	}
	
	private JCas getComment(Element comment) {
		String id = comment.getAttributeValue(DataReader.ID_ATTRIBUTE);
		String lang = comment.getAttributeValue(DataReader.LANG_ATTRIBUTE);
		
		String commentText = preprocessor.preprocess(comment.getText(),lang);
		JCas commentJCas = getEmptyJCas();
		commentJCas.setDocumentLanguage(lang);
		commentJCas.setDocumentText(commentText);
		Comment commentAnnotation = new Comment(commentJCas, 0, commentText.length());
		commentAnnotation.setID(id);
		commentAnnotation.addToIndexes();
		
		return commentJCas;
	}
	

	@Override
	public AbstractCas next() throws AnalysisEngineProcessException {
		MyElement next = currentElements.getFirst();
		
		JCas jcas = null;
		if (concatenateSubjectAndBody) {
			if (status == Status.USER_QUESTION_SUBJECT && next.element.getName().equals(DataReader.USER_QUESTION_TAG)) {
				jcas = getUserQuestion(next.element);
				currentElements.removeFirst();
				status = Status.RELATED_QUESTION_SUBJECT;
			} else if (status == Status.RELATED_QUESTION_SUBJECT && next.element.getName().equals(DataReader.RELATED_QUESTION_TAG)) {
				jcas = getRelatedQuestion(next.element);
				currentElements.removeFirst();
				status = Status.COMMENT;
			} else if (status == Status.COMMENT && next.element.getName().equals(DataReader.COMMENT_TAG)) {
				jcas = getComment(next.element);
				currentElements.removeFirst();
				
				if (currentElements.isEmpty())
					status = null;
				else if (currentElements.getFirst().element.getName().equals(DataReader.RELATED_QUESTION_TAG))
					status = Status.RELATED_QUESTION_SUBJECT;
			} else {
				throw new IllegalStateException(status+" "+next.element.getName());
			}
		} else {
			if (status == Status.USER_QUESTION_SUBJECT && next.element.getName().equals(DataReader.USER_QUESTION_TAG)) {
				jcas = getUserQuestionSubject(next.element);
				status = Status.USER_QUESTION_BODY;
			} else if (status == Status.USER_QUESTION_BODY && next.element.getName().equals(DataReader.USER_QUESTION_TAG)) {
				jcas = getUserQuestionBody(next.element);
				currentElements.removeFirst();
				status = Status.RELATED_QUESTION_SUBJECT;
			} else if (status == Status.RELATED_QUESTION_SUBJECT && next.element.getName().equals(DataReader.RELATED_QUESTION_TAG)) {
				jcas = getRelatedQuestionSubject(next.element);
				status = Status.RELATED_QUESTION_BODY;
			} else if (status == Status.RELATED_QUESTION_BODY && next.element.getName().equals(DataReader.RELATED_QUESTION_TAG)) {
				jcas = getRelatedQuestionBody(next.element);
				currentElements.removeFirst();
				status = Status.COMMENT;
			} else if (status == Status.COMMENT && next.element.getName().equals(DataReader.COMMENT_TAG)) {
				jcas = getComment(next.element);
				currentElements.removeFirst();
				
				if (currentElements.isEmpty())
					status = null;
				else if (currentElements.getFirst().element.getName().equals(DataReader.RELATED_QUESTION_TAG))
					status = Status.RELATED_QUESTION_SUBJECT;
			} else {
				throw new IllegalStateException(status+" "+next.element.getName());
			}
		}
		
		if (next.typeOfInstance == TypeOfInstance.INSTANCE) {
			Instance instanceAnnotation = new Instance(jcas);
			instanceAnnotation.addToIndexes();
		} else if (next.typeOfInstance == TypeOfInstance.INSTANCE_A) {
			InstanceA instanceAnnotation = new InstanceA(jcas);
			instanceAnnotation.addToIndexes();
		} 

		return jcas;
	}
}
