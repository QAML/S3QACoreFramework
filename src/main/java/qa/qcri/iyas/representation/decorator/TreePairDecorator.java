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
 
 
package qa.qcri.iyas.representation.decorator;

import org.apache.uima.cas.CASException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceProcessException;

import qa.qcri.iyas.data.preprocessing.JCasPairGenerator;
import qa.qcri.iyas.type.representation.ConstituencyTree;
import qa.qcri.iyas.type.representation.DependencyTree;
import qa.qcri.iyas.type.representation.PosChunkTree;
import qa.qcri.iyas.type.representation.TreePair;

public class TreePairDecorator extends JCasDecorator {
	
	public enum TreeType {
		DEPENDENCY_TREE("dependency_tree"), CONSTITUENCY_TREE("constituency_tree"), 
		POS_CHUNK_TREE("poschunk_tree");
		private String treeType;
		TreeType(String treeType) {
			this.treeType = treeType;
		}
		public String treeType() {
			return treeType;
		}
	};
	
	public static final String PARAM_NAME_TREE_TYPE = "TreeType";
	
	@ConfigurationParameter(name = PARAM_NAME_TREE_TYPE, defaultValue="poschunk_tree")
	private TreeType treeType;
	
	@Override
	public void decorate(JCas jcas) throws ResourceProcessException {
		try {
			if (treeType == TreeType.POS_CHUNK_TREE) {
				JCas leftJCas = jcas.getView(JCasPairGenerator.LEFT_CAS_VIEW);
				PosChunkTree leftTree = new PosChunkTree(leftJCas);
				leftTree.setCasView(JCasPairGenerator.LEFT_CAS_VIEW);
				leftTree.setBegin(0);
				leftTree.setEnd(leftJCas.getDocumentText().length());
				leftTree.setName("tree");
				leftTree.addToIndexes();
				
				
				JCas rightJCas = jcas.getView(JCasPairGenerator.RIGHT_CAS_VIEW);
				PosChunkTree rightTree = new PosChunkTree(rightJCas);
				rightTree.setCasView(JCasPairGenerator.RIGHT_CAS_VIEW);
				rightTree.setBegin(0);
				rightTree.setEnd(rightJCas.getDocumentText().length());
				rightTree.setName("tree");
				rightTree.addToIndexes();
				
				TreePair treePair = new TreePair(jcas);
				treePair.setLeftTree(leftTree);
				treePair.setRightTree(rightTree);
				treePair.setName("tree-pair");
				treePair.setTreeName("tree");
				treePair.addToIndexes();
			} else if (treeType == TreeType.CONSTITUENCY_TREE) {
				JCas leftJCas = jcas.getView(JCasPairGenerator.LEFT_CAS_VIEW);
				ConstituencyTree leftTree = new ConstituencyTree(leftJCas);
				leftTree.setCasView(JCasPairGenerator.LEFT_CAS_VIEW);
				leftTree.setBegin(0);
				leftTree.setEnd(leftJCas.getDocumentText().length());
				leftTree.setName("tree");
				leftTree.addToIndexes();
				
				
				JCas rightJCas = jcas.getView(JCasPairGenerator.RIGHT_CAS_VIEW);
				ConstituencyTree rightTree = new ConstituencyTree(rightJCas);
				rightTree.setCasView(JCasPairGenerator.RIGHT_CAS_VIEW);
				rightTree.setBegin(0);
				rightTree.setEnd(rightJCas.getDocumentText().length());
				rightTree.setName("tree");
				rightTree.addToIndexes();
				
				TreePair treePair = new TreePair(jcas);
				treePair.setLeftTree(leftTree);
				treePair.setRightTree(rightTree);
				treePair.setName("tree-pair");
				treePair.setTreeName("tree");
				treePair.addToIndexes();
			} else if (treeType == TreeType.DEPENDENCY_TREE) {
				JCas leftJCas = jcas.getView(JCasPairGenerator.LEFT_CAS_VIEW);
				DependencyTree leftTree = new DependencyTree(leftJCas);
				leftTree.setCasView(JCasPairGenerator.LEFT_CAS_VIEW);
				leftTree.setBegin(0);
				leftTree.setEnd(leftJCas.getDocumentText().length());
				leftTree.setName("tree");
				leftTree.addToIndexes();
				
				
				JCas rightJCas = jcas.getView(JCasPairGenerator.RIGHT_CAS_VIEW);
				DependencyTree rightTree = new DependencyTree(rightJCas);
				rightTree.setCasView(JCasPairGenerator.RIGHT_CAS_VIEW);
				rightTree.setBegin(0);
				rightTree.setEnd(rightJCas.getDocumentText().length());
				rightTree.setName("tree");
				rightTree.addToIndexes();
				
				TreePair treePair = new TreePair(jcas);
				treePair.setLeftTree(leftTree);
				treePair.setRightTree(rightTree);
				treePair.setName("tree-pair");
				treePair.setTreeName("tree");
				treePair.addToIndexes();
			}
		} catch (CASException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
