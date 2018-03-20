package qa.qcri.iyas.representation.kelp;

import java.util.Collection;

import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceProcessException;
import org.uimafit.util.JCasUtil;

import it.uniroma2.sag.kelp.data.example.ExamplePair;
import it.uniroma2.sag.kelp.data.example.SimpleExample;
import it.uniroma2.sag.kelp.data.representation.tree.TreeRepresentation;
import qa.qcri.iyas.data.preprocessing.JCasPairGenerator;
import qa.qcri.iyas.representation.Serializer;
import qa.qcri.iyas.type.representation.DenseVector;
import qa.qcri.iyas.type.representation.PosChunkTree;
import qa.qcri.iyas.type.representation.SparseVector;
import qa.qcri.iyas.type.representation.StringRepresentation;
import qa.qcri.iyas.type.representation.Tree;
import qa.qcri.iyas.type.representation.TreePair;
import qa.qcri.iyas.util.tree.RichTree;
import qa.qcri.iyas.util.tree.TokenTree;
import qa.qcri.iyas.util.tree.node.RichNode;

public class KeLPSerializer extends Serializer {
	
	private static TreeSerializer treeSerializer = new TreeSerializer().useRoundBrackets().enableRelationalTags();
	
	private TreeRepresentation getTreeRepresentation(JCas jcas) throws Exception {
		Collection<Tree> trees = JCasUtil.select(jcas, Tree.class);
		if (trees.size() != 1)
			throw new ResourceProcessException("Expexted 1 tree in the left CAS, found "+trees.size(),null);
		Tree tree = trees.iterator().next();
		if (tree instanceof PosChunkTree) {
			TokenTree posChunkTree = RichTree.getPosChunkTree(jcas);
			TreeRepresentation treeRepr = new TreeRepresentation();
			treeRepr.setDataFromText(KeLPSerializer.treeSerializer.serializeTree(posChunkTree, RichNode.OUTPUT_PAR_SEMANTIC_KERNEL));
			
			return treeRepr;
		} else {
			throw new ResourceProcessException("Unsupported operation tree type.",null);
		}
	}

	@Override
	public String serialize(JCas jcas) throws ResourceProcessException {
		SimpleExample leftEx = new SimpleExample();
		SimpleExample rightEx = new SimpleExample();
		
		try {
			JCas leftJCas = jcas.getView(JCasPairGenerator.LEFT_CAS_VIEW);
			JCas rightJCas = jcas.getView(JCasPairGenerator.RIGHT_CAS_VIEW);
			
			Collection<TreePair> pairs = JCasUtil.select(jcas, TreePair.class);
			
			if (pairs.size() != 0) {
				if (pairs.size() > 1)
					throw new ResourceProcessException("Unsupported operation. The current implementation allows only for one pair of trees.",null);
				
				String reprName = pairs.iterator().next().getTreeName();
				
				leftEx.addRepresentation(reprName, getTreeRepresentation(leftJCas));
				rightEx.addRepresentation(reprName, getTreeRepresentation(rightJCas));
			}
			
			//Pair Representations code
			
			ExamplePair pair = new ExamplePair(leftEx,rightEx);
			
			for (DenseVector denseVector : JCasUtil.select(jcas, DenseVector.class)) {
				double features[] = denseVector.getFeatures().toArray();
				pair.addRepresentation(denseVector.getName(), new it.uniroma2.sag.kelp.data.representation.vector.DenseVector(features));
			}
			
			for (StringRepresentation stringRepr: JCasUtil.select(jcas, StringRepresentation.class)) {
				pair.addRepresentation(stringRepr.getName(),
						new it.uniroma2.sag.kelp.data.representation.string.StringRepresentation(stringRepr.getContent()));
			}
			
			for (SparseVector sparseVector :JCasUtil.select(jcas, SparseVector.class)) {
				throw new ResourceProcessException("SparseVector representation currently not supported.", null);
			}
			
			for (Tree tree : JCasUtil.select(jcas, Tree.class))
				throw new ResourceProcessException("Tree representation is currently supported only in example pairs.", null);
			
			
//			
//			if (JCasUtil.exists(jcas, InstanceA.class)) {
//				Collection<Comment> comments = JCasUtil.select(rightJCas, Comment.class);
//				if (comments.size() != 1)
//					throw new ResourceProcessException("Expected 1 comment annotation, found "+comments.size(), null);
//				
//				Comment comment = comments.iterator().next();
//				String id = comment.getID();
//				pair.addRepresentation("identifiers", new StringRepresentation(id));
//				
//			} else if (JCasUtil.exists(jcas, InstanceB.class)) {
//				Collection<RelatedQuestionBody> questions = JCasUtil.select(rightJCas, RelatedQuestionBody.class);
//				if (questions.size() != 1)
//					throw new ResourceProcessException("Expected 1 RelatedQuestionBody annotation, found "+questions.size(), null);
//				
//				RelatedQuestionBody question = questions.iterator().next();
//				String id = question.getID();
//				pair.addRepresentation("identifiers", new StringRepresentation(id));
//			} else
//				throw new ResourceProcessException("Expected an InstanceA or InstanceB annotation.", null);
			
			return pair.toString().replace("NOTYPE##", "").replace("LEX##", "");
		} catch (Exception e) {
			throw new ResourceProcessException(e);
		}
		
	}

	
	
	public String serialize(Tree tree) throws ResourceProcessException {
		throw new ResourceProcessException("Unsupported operation.",null);
	}
	
}
