package qa.qcri.iyas.data.readers;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.descriptor.OperationalProperties;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.jdom2.input.sax.XMLReaderXSDFactory;

@OperationalProperties(outputsNewCases = true,multipleDeploymentAllowed = false)
@TypeCapability(
		outputs = {"qa.qcri.iyas.types.UserQuestionSubject",
				   "qa.qcri.iyas.types.UserQuestionBody",
				   "qa.qcri.iyas.types.RelatedQuestionSubject",
				   "qa.qcri.iyas.types.RelatedQuestionBody",
				   "qa.qcri.iyas.types.Comment"}
)
public class InputCollectionDataReader extends JCasCollectionReader_ImplBase {
	
	public static final String INPUT_READER_PARAM = "InputFile";

	@ExternalResource(key = INPUT_READER_PARAM)
	private DataReader reader;
	
	@Override
	public void close() {
		try {
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void getNext(JCas jcas) throws IOException, CollectionException {
		try {
			XMLReaderJDOMFactory factory = new XMLReaderXSDFactory(new File(DataReader.SCHEMA_PATH));
			SAXBuilder saxBuilder = new SAXBuilder(factory);

			String nextStr = reader.next();
			
			saxBuilder.build(new StringReader(nextStr));
//			System.out.println(nextStr);
//			
//			System.exit(0);
			
			jcas.setDocumentText(nextStr);

		} catch (Exception e) {
			e.printStackTrace();
			throw new CollectionException();
		}
	}
	@Override
	public Progress[] getProgress() {
		return new Progress[]{new ProgressImpl(1,1,Progress.ENTITIES)};
	}
	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return reader.hasNext();
	}

}