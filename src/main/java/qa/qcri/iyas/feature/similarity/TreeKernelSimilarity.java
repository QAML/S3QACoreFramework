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

import org.apache.uima.UIMAException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import it.uniroma2.sag.kelp.data.representation.tree.TreeRepresentation;
import it.uniroma2.sag.kelp.kernel.DirectKernel;
import it.uniroma2.sag.kelp.kernel.tree.PartialTreeKernel;
import it.uniroma2.sag.kelp.kernel.tree.SubSetTreeKernel;
import it.uniroma2.sag.kelp.kernel.tree.SubTreeKernel;
import it.uniroma2.sag.kelp.kernel.tree.deltamatrix.DynamicDeltaMatrix;
import qa.qcri.iyas.util.tree.RichTree;
import qa.qcri.iyas.util.tree.TokenTree;
import qa.qcri.iyas.util.tree.TreeSerializer;

/**
 * Defines a similarity function between two JCas annotations based
 * on the following tree kernels (defined in KeLP): PTK (default), SST, ST. 
 *  
 * The tree kernel is defined by the parameter PARAM_NAME_TREE_KERNEL. 
 * Its current values are determined by the type TREE_TYPE.   
 *  The type of tree extracted from the JCas depends on the parameter  
 * PARAM_NAME_TREE_TYPE, current values are: DEPENDENCY_TREE,  
 * CONSTITUENCY_TREE, POS_CHUNK_TREE (default).
 *  All tree kernels depend on a parameter PARAM_NAME_LAMBDA which relates 
 * to the lambda parameter of the kernel. Default value is 0.4. 
 *  All tree kernels can be normalized according to the boolean parameter 
 * PARAM_NAME_NORMALIZED (default value is true, the kernel is normalized).
 *  
 * For an example of usage, see the test class 
 * {@link qa.qcri.iyas.feature.similarity.SimilarityMeasureTreeKernelSimilarityTest}
 * 
 * @author Giovanni Da San Martino
 *
 */
public class TreeKernelSimilarity extends ThreadSafeSimilarityMeasure {
	
	public enum TREE_TYPE {
		DEPENDENCY_TREE("dependency_tree"), CONSTITUENCY_TREE("constituency_tree"), 
		POS_CHUNK_TREE("poschunk_tree");
		private String treeType;
		TREE_TYPE(String treeType) {
			this.treeType = treeType;
		}
		public String treeType() {
			return treeType;
		}
	};

	public enum TREE_KERNEL_FUNCTION {
		PTK("ptk"), SST("sst"), ST("st"); //SPTK("sptk")
		private String kernelFunctionName;
		TREE_KERNEL_FUNCTION(String kernelFunctionName) {
			this.kernelFunctionName = kernelFunctionName;
		}
		public String kernelFunctionName() {
			return kernelFunctionName;
		}
		};
	
	/**
	 * The tree kernel to be used for computing the similarity. Possible values 
	 * are the ones described by the enum TREE_KERNEL_FUNCTION. Default kernel
	 * is the Partial Tree Kernel. 
	 */
	public static final String PARAM_NAME_TREE_KERNEL = "treeKernel";
	
	/**
	 * The type of tree to be extracted from the JCas.
	 * Currently dependency, constituency and PosChunk trees are supported.
	 * Default type of tree extracted is PosChunk  
	 */
	public static final String PARAM_NAME_TREE_TYPE = "treeType";
	
	/**
	 * Parameter lambda of the tree kernel. Default value is 0.4 
	 */
	public static final String PARAM_NAME_LAMBDA = "lambda";

	/**
	 * Whether the kernel should be normalized
	 */
	public static final String PARAM_NAME_NORMALIZED = "normalized";
	
	
	@ConfigurationParameter(name = PARAM_NAME_TREE_KERNEL, defaultValue="ptk")
	private TREE_KERNEL_FUNCTION treeKernel;
	
	@ConfigurationParameter(name = PARAM_NAME_LAMBDA, defaultValue="0.4f")
	private float lambda;
	
	@ConfigurationParameter(name = PARAM_NAME_TREE_TYPE, defaultValue="poschunk_tree")
	private TREE_TYPE treeType; 

	@ConfigurationParameter(name = PARAM_NAME_NORMALIZED, defaultValue="true")
	private boolean normalized; 
	
	@Override
	public double getSimilarityValue(JCas leftJCas, JCas rightJCas) throws UIMAException {

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
//			System.out.println(ts.serializeTree(leftTree));
//			System.out.println(ts.serializeTree(rightTree));
			t1.setDataFromText(ts.serializeTree(leftTree));
			t2.setDataFromText(ts.serializeTree(rightTree));
		} catch (Exception e) {
			throw new UIMAException(new IllegalStateException("ERROR: Unrecognized tree type")); 
		}
		
		DirectKernel<TreeRepresentation> tk = null;
		
		if(treeKernel==TREE_KERNEL_FUNCTION.PTK) {//DynamicDeltaMatrix()
			PartialTreeKernel ptk = new PartialTreeKernel(lambda, 0.4f, 1f, "0");
			ptk.setDeltaMatrix(new DynamicDeltaMatrix());
			tk = ptk;;
		} else if(treeKernel==TREE_KERNEL_FUNCTION.SST) {
			SubSetTreeKernel sst = new SubSetTreeKernel(lambda, "0");
			sst.setDeltaMatrix(new DynamicDeltaMatrix());
			tk = sst;
		} else if(treeKernel==TREE_KERNEL_FUNCTION.ST) {
			SubTreeKernel st = new SubTreeKernel(lambda, "0");
			st.setDeltaMatrix(new DynamicDeltaMatrix());
			tk = st;
//		} else if(treeKernel==TREE_KERNEL_FUNCTION.SPTK) { //many more complex parameters need to be passed to the SPTK, such as the nodeSimilarity, leave them as future work
//			tk = new SmoothedPartialTreeKernel(lambda, MU, terminalFactor, similarityThreshold, nodeSimilarity, representationIdentifier)
		} else {
			throw new UIMAException(
					new IllegalStateException("Unrecognized Tree Kernel Function in " + this.getClass().getName()));
		}
		
		if(normalized) {
			Float normt1 = tk.kernelComputation(t1, t1);
			Float normt2 = tk.kernelComputation(t2, t2);
			if(normt1==0 || normt2==0) {
				return 0;
			} else {
				return tk.kernelComputation(t1, t2)/(Math.sqrt(normt1*normt2));
			}
		} else {
			return tk.kernelComputation(t1, t2);
		}
	}
	
}
