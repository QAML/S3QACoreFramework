package qa.qcri.iyas.representation;

import org.apache.uima.fit.component.ExternalResourceAware;
import org.apache.uima.fit.component.initialize.ConfigurationParameterInitializer;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.resource.SharedResourceObject;

public abstract class Serializer  implements SharedResourceObject, ExternalResourceAware {

	@ConfigurationParameter(name=ExternalResourceFactory.PARAM_RESOURCE_NAME)
	private String resourceName;
	
	@Override
	public void load(DataResource data) throws ResourceInitializationException {
		ConfigurationParameterInitializer.initialize(this, data);
	}

	@Override
	public String getResourceName() {
		return this.resourceName;
	}

	@Override
	public void afterResourcesInitialized() throws ResourceInitializationException {
		// TODO Auto-generated method stub	
	}
	
	public abstract String serialize(JCas jcas) throws ResourceProcessException;
	
}
