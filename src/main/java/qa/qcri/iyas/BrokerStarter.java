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
 
 
package qa.qcri.iyas;

import org.apache.activemq.broker.BrokerService;

/**
 * A simple class to start the broker.
 * @author Salvatore Romeo
 *
 */
public class BrokerStarter {

	public static void main(String[] args) throws Exception {
		BrokerService broker = new BrokerService();
		 
		// configure the broker
		broker.addConnector("tcp://localhost:61616");
		
		// start the broker
		broker.start();
	}

}
