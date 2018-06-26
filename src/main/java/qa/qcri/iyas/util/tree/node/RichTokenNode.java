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
 
 
package qa.qcri.iyas.util.tree.node;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * 
 * The RichTokenNode class wraps a Token object, a datatype from the DKPro
 * typesystem
 * 
 * The class overrides the getValue() method, which returns the text covered by
 * the token
 * 
 * Moreover, it understands a set of parameters for producing different token
 * representations
 */
public class RichTokenNode extends BaseRichNode {

	private Token token;
	
	public RichTokenNode(Token token) {
		super();
		this.token = token;
		this.metadata.put(RichNode.TYPE_KEY, RichNode.TYPE_TOKEN_NODE);
	}

	public Token getToken() {
		return this.token;
	}

	@Override
	public String getValue() {
		return this.token.getCoveredText();
	}

	/**
	 * Produces a string representation of the node which may be affected by the
	 * provided parameter list. A node can parse this list and react according
	 * to it. In the default case the implementation should return the same
	 * value of getValue()
	 * 
	 * Supported parameters
	 * 
	 * - RichNode.OUTPUT_PAR_TOKEN Return the text covered by the token, which
	 * is also the default behaviour
	 * 
	 * - RichNode.OUTPUT_PAR_LEMMA Return the lemma of the token
	 * 
	 * - RichNode.OUTPUT_PAR_STEM Return the stem of the token
	 * 
	 * - RichNode.OUTPUT_PAR_TOKEN_LOWERCASE Return the lowercased current
	 * representation
	 * 
	 * - RichNode.OUTPUT_PAR_POSTAG Return the postag of the current token
	 * 
	 * - RichNode.OUTPUT_PAR_SEMANTIC_KERNEL Return the form used by the semantic
	 *       kernel (lemma::1st-char-of-postag
	 * 
	 * Pay attention to the order of these parameters in the list. TOKEN and
	 * LEMMA override each other, so the parameter later in the list prevails.
	 * 
	 * @param parameterList
	 *            parameter list (strings separated by comma)
	 * @return the node representation
	 */
	@Override
	public String getRepresentation(String parameterList) {
		String output = this.getValue();
		
		POS pos = null;

		if (parameterList.isEmpty()) {
			return output;
		}

		boolean lowercase = false;

		String[] fields = parameterList.split(",");
		for (String field : fields) {
			switch (field) {
			case RichNode.OUTPUT_PAR_TOKEN:
				output = this.token.getCoveredText();
				break;
			case RichNode.OUTPUT_PAR_LEMMA:				
				if (output.equals("(")) {
					output = "-LRB-";
				} else if (output.equals(")")) {
					output = "-RRB-";
				} else {
					Lemma lemma = this.token.getLemma();
					if(lemma != null) {
						output = lemma.getValue();
					}
				}
				break;
			case RichNode.OUTPUT_PAR_STEM:
				output = this.token.getStem().getValue();
				break;
			case RichNode.OUTPUT_PAR_TOKEN_LOWERCASE:
				lowercase = true;
				break;			
			case RichNode.OUTPUT_PAR_POSTAG:
				pos = this.token.getPos();
				if (pos == null)
					System.out.print(0);
				else
					output = pos.getPosValue();
				break;
			case RichNode.OUTPUT_PAR_SEMANTIC_KERNEL:
				pos = this.token.getPos();
				String lemma = this.token.getLemma().getValue();
				String p = null;
				if (pos.getPosValue().contains("+")) {
					p = ""+pos.getPosValue().split("\\+")[1].toLowerCase().charAt(0);
				} else {
					p = ""+pos.getPosValue().toLowerCase().charAt(0);
				}
				output = lemma + "::" + p;
				break;
			}
		}

		/**
		 * Some tokens (e.g. _ ) may not have a requested representation
		 */
		if (output == null) {
			output = this.getValue();
		}

		if (lowercase) {
			output = output.toLowerCase();
		}

		return output;
	}
}
