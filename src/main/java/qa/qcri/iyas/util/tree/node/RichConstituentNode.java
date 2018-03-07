/**
 * Copyright 2017 Massimo Nicosia
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
 
 
package qa.qcri.iyas.util.tree.node;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

/**
 * 
 * The RichConstituentNode class wraps a Constituent object, a datatype from the
 * DKPro typesystem
 */
public class RichConstituentNode extends BaseRichNode {

	private Constituent constituent;

	public RichConstituentNode(Constituent constituent) {
		super();
		this.constituent = constituent;
		this.metadata.put(RichNode.TYPE_KEY, RichNode.TYPE_CONSTITUENT_NODE);
		this.value = constituent.getConstituentType();
	}

	/**
	 * 
	 * @return the DKPro Constituent object
	 */
	public Constituent getConstituent() {
		return this.constituent;
	}

}