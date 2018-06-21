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

package qa.qcri.iyas.data.reader;

import org.apache.uima.resource.ResourceProcessException;

public class VolatileDataReader extends DataReader {

	private boolean first = true;
	
	@Override
	public boolean hasNext() {
		return first;
	}

	@Override
	public String next() throws ResourceProcessException {
		first = false;
		return "<root>"+
				"	<instance_b>"+
				"		<user_question id=\"Q268\" lang=\"ar\" numberOfCandidates=\"10\">"+
				"			<subject></subject>"+
				"			<body>ما هي أعراض القرحة المعدية؟</body>"+
				"		</user_question>"+
				"		<related_question id=\"Q268_R1\" lang=\"ar\" index=\"0\" relevance=\"PerfectMatch\" totalNumberExamples=\"10\" rank=\"1\" >"+
				"			<subject></subject>"+
				"			<body>ما هي أعراض القرحة المعدية؟</body>"+
				"		</related_question>"+
				"		<related_question id=\"Q268_R2\" lang=\"ar\" index=\"0\" relevance=\"PerfectMatch\" totalNumberExamples=\"10\" rank=\"1\" >"+
				"			<subject></subject>"+
				"			<body>ما هي أعراض القرحة المعدية؟</body>"+
				"		</related_question>"+
				"		<related_question id=\"Q268_R3\" lang=\"ar\" index=\"0\" relevance=\"PerfectMatch\" totalNumberExamples=\"10\" rank=\"1\" >"+
				"			<subject></subject>"+
				"			<body>ما هي أعراض القرحة المعدية؟</body>"+
				"		</related_question>"+
				"		<related_question id=\"Q268_R4\" lang=\"ar\" index=\"0\" relevance=\"PerfectMatch\" totalNumberExamples=\"10\" rank=\"1\" >"+
				"			<subject></subject>"+
				"			<body>ما هي أعراض القرحة المعدية؟</body>"+
				"		</related_question>"+
				"		<related_question id=\"Q268_R5\" lang=\"ar\" index=\"0\" relevance=\"PerfectMatch\" totalNumberExamples=\"10\" rank=\"1\" >"+
				"			<subject></subject>"+
				"			<body>ما هي أعراض القرحة المعدية؟</body>"+
				"		</related_question>"+
				"		<related_question id=\"Q268_R6\" lang=\"ar\" index=\"0\" relevance=\"PerfectMatch\" totalNumberExamples=\"10\" rank=\"1\" >"+
				"			<subject></subject>"+
				"			<body>ما هي أعراض القرحة المعدية؟</body>"+
				"		</related_question>"+
				"		<related_question id=\"Q268_R7\" lang=\"ar\" index=\"0\" relevance=\"PerfectMatch\" totalNumberExamples=\"10\" rank=\"1\" >"+
				"			<subject></subject>"+
				"			<body>ما هي أعراض القرحة المعدية؟</body>"+
				"		</related_question>"+
				"		<related_question id=\"Q268_R8\" lang=\"ar\" index=\"0\" relevance=\"PerfectMatch\" totalNumberExamples=\"10\" rank=\"1\" >"+
				"			<subject></subject>"+
				"			<body>ما هي أعراض القرحة المعدية؟</body>"+
				"		</related_question>"+
				"		<related_question id=\"Q268_R9\" lang=\"ar\" index=\"0\" relevance=\"PerfectMatch\" totalNumberExamples=\"10\" rank=\"1\" >"+
				"			<subject></subject>"+
				"			<body>ما هي أعراض القرحة المعدية؟</body>"+
				"		</related_question>"+
				"		<related_question id=\"Q268_R10\" lang=\"ar\" index=\"0\" relevance=\"PerfectMatch\" totalNumberExamples=\"10\" rank=\"1\" >"+
				"			<subject></subject>"+
				"			<body>ما هي أعراض القرحة المعدية؟</body>"+
				"		</related_question>"+
				"	</instance_b>"+
				"</root>";
	}
	
}
