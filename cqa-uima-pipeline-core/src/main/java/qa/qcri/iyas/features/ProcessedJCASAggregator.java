package qa.qcri.iyas.features;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSClassRegistry;
import org.apache.uima.fit.component.JCasMultiplier_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.descriptor.OperationalProperties;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.impl.JCasImpl;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCreationUtils;

import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.CasCopier;
import qa.qcri.iyas.types.Comment;
import qa.qcri.iyas.types.Instance;
import qa.qcri.iyas.types.QAAnnotation;
import qa.qcri.iyas.types.RelatedQuestion;
import qa.qcri.iyas.types.RelatedQuestionBody;
import qa.qcri.iyas.types.RelatedQuestionSubject;
import qa.qcri.iyas.types.UserQuestion;
import qa.qcri.iyas.types.UserQuestionBody;
import qa.qcri.iyas.types.UserQuestionSubject;

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
		
		return qaAnn.getID().split("_")[0];
	}
	
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		if (JCasUtil.exists(jcas, Instance.class)) {
			String userQuestionID = getUserQuestionID(jcas);
//			if (instances.get(userQuestionID) == null)
//				instances.put(userQuestionID, new ProcessedJCASAggregator.ProcessedInstance());
			try {
				boolean ready = processedInstancesManager.addJCas(userQuestionID,jcas);
				if (ready) {
					JCas readyJCas = getEmptyJCas();
					processedInstancesManager.getJCasToInstance(userQuestionID, readyJCas, true);
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
