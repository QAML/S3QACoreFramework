package qa.qcri.iyas.features;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.OperationalProperties;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.tcas.DocumentAnnotation;

import qa.qcri.iyas.types.Comment;
import qa.qcri.iyas.types.RelatedQuestionBody;
import qa.qcri.iyas.types.RelatedQuestionSubject;
import qa.qcri.iyas.types.UserQuestionBody;
import qa.qcri.iyas.types.UserQuestionSubject;

@OperationalProperties(modifiesCas = true, outputsNewCases = false, multipleDeploymentAllowed = true)
@TypeCapability(
		inputs = {"qa.qcri.iyas.types.UserQuestionSubject",
				   "qa.qcri.iyas.types.UserQuestionBody",
				   "qa.qcri.iyas.types.RelatedQuestionSubject",
				   "qa.qcri.iyas.types.RelatedQuestionBody",
				   "qa.qcri.iyas.types.Comment"}
)
public class StandardComplexFeatureExtractor extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		for (Annotation annotation : jcas.getAnnotationIndex()) {
			if (annotation instanceof UserQuestionSubject) {
				UserQuestionSubject cqaAnnotation = (UserQuestionSubject)annotation;
				System.out.println(cqaAnnotation.getID()+" subject");
			} else if (annotation instanceof UserQuestionBody) {
				UserQuestionBody cqaAnnotation = (UserQuestionBody)annotation;
				System.out.println(cqaAnnotation.getID()+" body");
			} else if (annotation instanceof RelatedQuestionSubject) {
				RelatedQuestionSubject cqaAnnotation = (RelatedQuestionSubject)annotation;
				System.out.println(cqaAnnotation.getID()+" subject");
			} else if (annotation instanceof RelatedQuestionBody) {
				RelatedQuestionBody cqaAnnotation = (RelatedQuestionBody)annotation;
				System.out.println(cqaAnnotation.getID()+" body");
			} else if (annotation instanceof Comment) {
				Comment cqaAnnotation = (Comment)annotation;
				System.out.println(cqaAnnotation.getID()+" comment");
			} else if (annotation instanceof DocumentAnnotation) {
				//Do nothing
			} else {
				for (Annotation ann : jcas.getAnnotationIndex()) {
					System.err.println(ann.toString());
				}
				throw new RuntimeException("The input CAS must have only one annotation!",null);
			}
		}
	}

}
