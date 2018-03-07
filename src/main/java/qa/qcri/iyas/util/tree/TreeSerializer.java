/**
 * Copyright 2017 Massimo Nicosia and Antonio Uva
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
 
 
package qa.qcri.iyas.util.tree;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

import qa.qcri.iyas.util.tree.node.RichNode;

/**
 * 
 * Serializes a tree made of RichNodes
 */
public class TreeSerializer {

	/**
	 * Additional labels are combined and joined by this separator
	 */
	private static final String LABEL_SEPARATOR = "-";

	/**
	 * Default parenthesis used in serialization
	 */
	private String lbr = "(";
	private String rbr = ")";

	/**
	 * Outputting additional labels is optional and thus must be explicitly
	 * specified
	 */
	private boolean enableAdditionalLabels = false;

	/**
	 * Outputting relational tags is optional and thus must be explicitly
	 * specified
	 */
	private boolean enableRelationalTags = false;
	
	private boolean svmlightTKFormat = false;
	
	/**
	 * Adopts square brackets in the serialized trees
	 * 
	 * @return the current TreeSerialized instance (for chaining)
	 */
	public TreeSerializer useSquareBrackets() {
		this.lbr = "[";
		this.rbr = "]";
		return this;
	}

	/**
	 * Adopts round brackets in the serialized trees
	 * 
	 * @return the current TreeSerialized instance (for chaining)
	 */
	public TreeSerializer useRoundBrackets() {
		this.lbr = "(";
		this.rbr = ")";
		return this;
	}

	/**
	 * Enables additional labels in the output
	 * 
	 * @return the current TreeSerialized instance (for chaining)
	 */
	public TreeSerializer enableAdditionalLabels() {
		this.enableAdditionalLabels = true;
		return this;
	}

	/**
	 * Disables additional labels in the output
	 * 
	 * @return the current TreeSerialized instance (for chaining)
	 */
	public TreeSerializer disableAdditionalLabels() {
		this.enableAdditionalLabels = false;
		return this;
	}

	/**
	 * Enables relational tags in the output
	 * 
	 * @return the current TreeSerialized instance (for chaining)
	 */
	public TreeSerializer enableRelationalTags() {
		this.enableRelationalTags = true;
		return this;
	}

	/**
	 * Disables relational tags in the output
	 * 
	 * @return the current TreeSerialized instance (for chaining)
	 */
	public TreeSerializer disableRelationalTags() {
		this.enableRelationalTags = false;
		return this;
	}

	public TreeSerializer svmlightTKFormat() {
		this.svmlightTKFormat = true;
		return this;
	}
	
	/**
	 * Serializes a tree starting from the specified node
	 * 
	 * @param node
	 *            the root node of the tree
	 * @param parameterList
	 *            the parameter list for the node output
	 * @return the serialized tree
	 */
	public String serializeTree(RichNode node, String parameterList) {
		List<String> leftParts = new ArrayList<>();
		List<String> rightParts = new ArrayList<>();

		if(!(this.svmlightTKFormat && node.isLeaf())) {
			leftParts.add(this.lbr);
		}
		List<String> labels = new ArrayList<>();
		
		String nodeValue = node.getRepresentation(parameterList)
			.replaceAll("\\(", "{")
			.replaceAll("\\)", "}");
		
		labels.add(nodeValue);
		
		if (this.enableAdditionalLabels) {
			labels.addAll(node.getAdditionalLabels());
		}

		if (this.enableRelationalTags) {
			if (node.getMetadata().containsKey(RichNode.REL_KEY)) {
				labels.add(node.getMetadata().get(RichNode.REL_KEY));
			}
		}

		leftParts.add(Joiner.on(LABEL_SEPARATOR).join(labels));

		if (!node.isLeaf()) {
			leftParts.add(" ");
		}
		if(!(this.svmlightTKFormat && node.isLeaf())) {
			rightParts.add(0, this.rbr);
		}

		for (RichNode child : node.getChildren()) {
			leftParts.add(serializeTree(child, parameterList));
		}

		leftParts.addAll(rightParts);

		return Joiner.on("").join(leftParts);
	}
	
	/**
	 * Serializes a tree starting from the specified node
	 * using an empty parameter list
	 * 
	 * @param node
	 *            the root node of the tree
	 * @return the serialized tree
	 */
	public String serializeTree(RichNode node) {
		return this.serializeTree(node, "");
	}
}
