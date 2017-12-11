package qa.qcri.iyas;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.aae.client.UimaAsynchronousEngine;
import org.apache.uima.adapter.jms.client.BaseUIMAAsynchronousEngine_impl;


public class RunStandardPipeline extends UimaAsBaseCallbackListener {
	
	private static final List<String> IDs = new LinkedList<String>();
	
	public static void main(String[] args) throws Exception {
		UimaAsynchronousEngine uimaAsEngine = new BaseUIMAAsynchronousEngine_impl();
		
		// create a Map to hold required parameters
		Map<String,Object> appCtx = new HashMap<String,Object>();
		appCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,System.getenv("UIMA_HOME") + "/bin/dd2spring.xsl");
		appCtx.put(UimaAsynchronousEngine.SaxonClasspath,"file:" + System.getenv("UIMA_HOME") + "/saxon/saxon8.jar");
		
		

		for (int i=0;i<1;i++) {
			IDs.add(uimaAsEngine.deploy(
					new File("resources/descriptors/deployment/StandardSimpleFeatureExtractorAAE_DeploymentDescriptor.xml").getAbsolutePath(), appCtx));
		}
		
		// Deploy service
		for (int i=0;i<1;i++) {
			IDs.add(uimaAsEngine.deploy(
					new File("resources/descriptors/deployment/StandardPipelineAAE_DeploymentDescriptor.xml").getAbsolutePath(), appCtx));
		}
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				 Scanner scanner = new Scanner(System.in);
				 while (true) {
					 String command = scanner.nextLine();
					 if (command.startsWith("s")) {
						 try {
							uimaAsEngine.undeploy(IDs.get(0));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							scanner.close();
						}
					 } else if (command.equals("q")) {
						 System.out.println(IDs);
					 } else if (command.startsWith("a ")) {
						 int n = Integer.parseInt(command.split(" ")[1]);
						 for (int i=0;i<n;i++)
							try {
								IDs.add(uimaAsEngine.deploy(
										new File("resources/descriptors/qa/qcri/iyas/StandardPipelineAAE_DeploymentDescriptor.xml").getAbsolutePath(), appCtx));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					 }
				 }
			}
		});
		t.start();
//		System.out.println(IDs);
		
//		uimaAsEngine.undeploy("-626a937e:15f522aa729:-7fb1");
	}

}
