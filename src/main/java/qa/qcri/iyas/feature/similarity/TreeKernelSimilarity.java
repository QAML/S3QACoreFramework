/**
 * Copyright 2017 Giovanni Da San Martino
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
package qa.qcri.iyas.feature.similarity;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import it.uniroma2.sag.kelp.data.representation.tree.TreeRepresentation;
import it.uniroma2.sag.kelp.kernel.DirectKernel;
import it.uniroma2.sag.kelp.kernel.tree.PartialTreeKernel;
import it.uniroma2.sag.kelp.kernel.tree.SmoothedPartialTreeKernel;
import it.uniroma2.sag.kelp.kernel.tree.SubSetTreeKernel;
import it.uniroma2.sag.kelp.kernel.tree.SubTreeKernel;
import qa.qcri.iyas.data.tree.RichTree;
import qa.qcri.iyas.data.tree.TokenTree;
import qa.qcri.iyas.data.tree.TreeSerializer;

/**
 * Defines a similarity function between two JCas annotations based
 * on any kernel function defined in KeLP.   
 * 
 * @author Giovanni Da San Martino
 *
 */
public class TreeKernelSimilarity extends SimilarityMeasure {
	
	public enum TREE_TYPE {
		DEPENDENCY_TREE("dependency_tree"), CONSTITUENCY_TREE("constituency_tree"), POS_CHUNK_TREE("poschunk_tree");
		private String treeType;
		TREE_TYPE(String treeType) {
			this.treeType = treeType;
		}
		public String treeType() {
			return treeType;
		}
	};
	//public enum TREE_KERNEL_FUNCTION {SPTK, PTK, SST, ST};
	public enum TREE_KERNEL_FUNCTION {
		SPTK("sptk"), PTK("ptk"), SST("sst"), ST("st");
		private String kernelFunctionName;
		TREE_KERNEL_FUNCTION(String kernelFunctionName) {
			this.kernelFunctionName = kernelFunctionName;
		}
		public String kernelFunctionName() {
			return kernelFunctionName;
		}
		};
	
	/**
	 * The tree kernel to be used for computing the similarity
	 */
	public static final String PARAM_NAME_TREE_KERNEL = "treeKernel";
	
	/**
	 * The type of tree to be extracted from the JCas.
	 * Currently dependency, constituency and PosChunk trees are supported  
	 */
	public static final String PARAM_NAME_TREE_TYPE = "treeType";
	
	/**
	 * Parameter lambda of the kernel 
	 */
	public static final String PARAM_NAME_LAMBDA = "lambda";

	@ConfigurationParameter(name = PARAM_NAME_TREE_KERNEL, defaultValue="ptk")
	private TREE_KERNEL_FUNCTION treeKernel;
	
	@ConfigurationParameter(name = PARAM_NAME_LAMBDA, defaultValue="0.4f")
	private float lambda;
	
	@ConfigurationParameter(name = PARAM_NAME_TREE_TYPE, defaultValue="poschunk_tree")
	private TREE_TYPE treeType; 
		
	@Override
	public double getSimilarityValue(JCas leftJCas, JCas rightJCas) {

		TreeRepresentation t1 = new TreeRepresentation();
		TreeRepresentation t2 = new TreeRepresentation();
		TreeSerializer ts = new TreeSerializer().enableRelationalTags().useRoundBrackets();
		TokenTree leftTree = null, rightTree = null;
		
		if(treeType==TREE_TYPE.DEPENDENCY_TREE) {
			leftTree = RichTree.getDependencyTree(leftJCas);
			rightTree = RichTree.getDependencyTree(rightJCas);
		}else if(treeType==TREE_TYPE.CONSTITUENCY_TREE) {
			leftTree = RichTree.getConstituencyTree(leftJCas);
			rightTree = RichTree.getConstituencyTree(rightJCas);
		} else if (treeType==TREE_TYPE.POS_CHUNK_TREE) {
			leftTree = RichTree.getPosChunkTree(leftJCas);
			rightTree = RichTree.getPosChunkTree(rightJCas);
		}
			
		try {
			t1.setDataFromText(ts.serializeTree(leftTree));
			t2.setDataFromText(ts.serializeTree(rightTree));
		} catch (Exception e) {
			System.out.println("ERROR: Unrecognized tree type");
			e.printStackTrace();
			//throw new Exception(); //TODO add UIMA exception
		}
		
		if(treeKernel==TREE_KERNEL_FUNCTION.PTK) {
			PartialTreeKernel tk = new PartialTreeKernel();
			tk.setLambda(lambda);
			return tk.kernelComputation(t1, t2);
		} else if(treeKernel==TREE_KERNEL_FUNCTION.SST) {
			SubSetTreeKernel tk = new SubSetTreeKernel();
			tk.setLambda(lambda);
			return tk.kernelComputation(t1, t2);
		} else if(treeKernel==TREE_KERNEL_FUNCTION.ST) {
			SubTreeKernel tk = new SubTreeKernel();
			tk.setLambda(lambda);
			return tk.kernelComputation(t1, t2);
		} else if(treeKernel==TREE_KERNEL_FUNCTION.SPTK) {
			SmoothedPartialTreeKernel tk = new SmoothedPartialTreeKernel();
			tk.setLambda(lambda);
			return tk.kernelComputation(t1, t2);
		} else {
			//TODO add UIMA exception
		}
		
		return 0;
	}
	
}
