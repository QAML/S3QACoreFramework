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

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;

/**
 * 
 * The RichChunkNode class wraps a Chunk object, a datatype from the DKPro
 * typesystem
 */
public class RichChunkNode extends BaseRichNode {

	private Chunk chunk;

	public RichChunkNode(Chunk chunk) {
		super();
		this.chunk = chunk;
		this.metadata.put(RichNode.TYPE_KEY, RichNode.TYPE_CHUNK_NODE);
		this.value = chunk.getChunkValue();
	}

	/**
	 * 
	 * @return the DKPro Chunk object
	 */
	public Chunk getChunk() {
		return this.chunk;
	}

}
