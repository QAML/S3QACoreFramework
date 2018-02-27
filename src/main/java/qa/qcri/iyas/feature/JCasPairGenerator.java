package qa.qcri.iyas.feature;

import java.util.LinkedList;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.component.JCasMultiplier_ImplBase;
import org.apache.uima.fit.descriptor.OperationalProperties;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCopier;
import org.apache.uima.util.CasCreationUtils;

import qa.qcri.iyas.type.InstanceA;
import qa.qcri.iyas.type.InstanceB;
import qa.qcri.iyas.util.AggregatedJCasManagerTaskA;
import qa.qcri.iyas.util.AggregatedJCasManagerTaskB;

@OperationalProperties(modifiesCas = false, outputsNewCases = true, multipleDeploymentAllowed = true)
@TypeCapability(
		inputs = {"qa.qcri.iyas.types.UserQuestion",
				   "qa.qcri.iyas.types.UserQuestionSubject",
				   "qa.qcri.iyas.types.UserQuestionBody",
				   "qa.qcri.iyas.types.RelatedQuestion",
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
public class JCasPairGenerator extends JCasMultiplier_ImplBase {

	public static final String LEFT_CAS_VIEW = "leftJCas";
	public static final String RIGHT_CAS_VIEW = "rightJCas";
	
	private LinkedList<JCas> pendingJCases;

	@Override
	public boolean hasNext() throws AnalysisEngineProcessException {
		return !pendingJCases.isEmpty();
	}
	
	@Override
	public void initialize(final UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		pendingJCases = new LinkedList<>();
	}

	@Override
	public AbstractCas next() throws AnalysisEngineProcessException {
		JCas jcas = getEmptyJCas();
		
		CAS cas = pendingJCases.getFirst().getCas();
		CasCopier.copyCas(cas, jcas.getCas(), true);
		pendingJCases.removeFirst();
		cas.release();
		
		return jcas;
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		try {
			if (JCasUtil.exists(jcas, InstanceA.class)) {
				AggregatedJCasManagerTaskA aggrJCasManager = new AggregatedJCasManagerTaskA(jcas);

				if (!aggrJCasManager.isConcatenated())
					throw new UIMAException("The not concatenated question are not yet supported.", null);
				
				JCas questionBody = aggrJCasManager.getRelatedQuestionBodyJCas();
				
				
				for (JCas candidate : aggrJCasManager.getCandidatesJCases()) {
					CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
							null, null);

					CasCopier leftCopier = new CasCopier(questionBody.getCas(), cas);
					leftCopier.copyCasView(questionBody.getCas(), cas.createView(LEFT_CAS_VIEW), true);
					
					CasCopier rightCopier = new CasCopier(candidate.getCas(), cas);
					rightCopier.copyCasView(candidate.getCas(), cas.createView(RIGHT_CAS_VIEW), true);
					
					pendingJCases.add(cas.getJCas());
				}
			} else if (JCasUtil.exists(jcas, InstanceB.class)) {
				AggregatedJCasManagerTaskB aggrJCasManager = new AggregatedJCasManagerTaskB(jcas);

				if (!aggrJCasManager.isConcatenated())
					throw new UIMAException("The not concatenated question are not yet supported.", null);
				
				JCas questionBody = aggrJCasManager.getUserQuestionBodyJCas();
				
				for (JCas candidate : aggrJCasManager.getCandidatesJCases()) {
					CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
							null, null);

					CasCopier leftCopier = new CasCopier(questionBody.getCas(), cas);
					leftCopier.copyCasView(questionBody.getCas(), cas.createView(LEFT_CAS_VIEW), true);
					
					CasCopier rightCopier = new CasCopier(candidate.getCas(), cas);
					rightCopier.copyCasView(candidate.getCas(), cas.createView(RIGHT_CAS_VIEW), true);
					
					pendingJCases.add(cas.getJCas());
				}
			}
		} catch (UIMAException e) {
			throw new AnalysisEngineProcessException(e);
		} 
		
	}

}
