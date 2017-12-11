package qa.qcri.iyas;

import org.apache.activemq.broker.BrokerService;

public class BrokerStarter {

	public static void main(String[] args) throws Exception {
		BrokerService broker = new BrokerService();
		 
		// configure the broker
		broker.addConnector("tcp://localhost:61616");
		 
		broker.start();
	}

}
