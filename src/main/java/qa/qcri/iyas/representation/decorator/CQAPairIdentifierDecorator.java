package qa.qcri.iyas.representation.decorator;

import java.util.Collection;

import org.apache.uima.cas.CASException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceProcessException;

import qa.qcri.iyas.data.preprocessing.JCasPairGenerator;
import qa.qcri.iyas.type.cqa.Comment;
import qa.qcri.iyas.type.cqa.InstanceA;
import qa.qcri.iyas.type.cqa.InstanceB;
import qa.qcri.iyas.type.cqa.RelatedQuestionBody;
import qa.qcri.iyas.type.representation.StringRepresentation;

public class CQAPairIdentifierDecorator extends JCasDecorator {

	@Override
	public void decorate(JCas jcas) throws ResourceProcessException {
		try {
			JCas rightJCas = jcas.getView(JCasPairGenerator.RIGHT_CAS_VIEW);
			String id = null;
			if (JCasUtil.exists(jcas, InstanceA.class)) {
				Collection<Comment> comments = JCasUtil.select(rightJCas, Comment.class);
				if (comments.size() != 1)
					throw new ResourceProcessException("Expected 1 comment annotation, found "+comments.size(), null);
				
				id = comments.iterator().next().getID();
				
			} else if (JCasUtil.exists(jcas, InstanceB.class)) {
				Collection<RelatedQuestionBody> questions = JCasUtil.select(rightJCas, RelatedQuestionBody.class);
				if (questions.size() != 1)
					throw new ResourceProcessException("Expected 1 RelatedQuestionBody annotation, found "+questions.size(), null);
				
				id = questions.iterator().next().getID();
			}
			
			StringRepresentation identifires = new StringRepresentation(jcas);
			identifires.setContent(id);
			identifires.setName("identifiers");
			identifires.addToIndexes();
			
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}
	}

}
