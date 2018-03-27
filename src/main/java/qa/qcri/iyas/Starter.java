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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.uima.aae.client.UimaAsynchronousEngine;
import org.apache.uima.adapter.jms.client.BaseUIMAAsynchronousEngine_impl;

public class Starter {
	
	private static final String QUEUE_NAME_OPT = "qn";
	private static final String SCALEOUT_NAME_OPT = "sc";
	private static final String INPUT_FILE_OPT = "if";
	private static final String OUTPUT_FILE_OPT = "of";
	private static final String URL_OPT = "u";
	
	private static final String QUEUE_NAME_LONG_OPT = "queue-name";
	private static final String SCALEOUT_LONG_NAME_OPT = "scaleout";
	private static final String INPUT_FILE_LONG_OPT = "input-file";
	private static final String OUTPUT_FILE_LONG_OPT = "output-file";
	private static final String URL_LONG_OPT = "url";
	
	
	private static final String DEPLOY_FEATURE_EXTRACTION_OPT = "dfe";
	private static final String USE_SIMS_OPT = "s";
	private static final String USE_RANK_OPT = "r";
	private static final String USE_TREES_OPT = "t";

	private static final String DEPLOY_FEATURE_EXTRACTION_LONG_OPT = "deploy-feature-extraction";
	private static final String USE_SIMS_LONG_OPT = "sims";
	private static final String USE_RANK_LONG_OPT = "rank";
	private static final String USE_TREES_LONG_OPT = "t";
	
	
	private static final String PROCESS_DATA_OPT = "ef";
	
	private static final String PROCESS_DATA_LONG_OPT = "extraction-features";

	
	private static final String DEPLOY_CLASSIFICATION_OPT = "dc";
	private static final String FE_QUEUE_NAME_OPT = "fqn";
	private static final String FE_URL_OPT = "fu";

	private static final String DEPLOY_CLASSIFICATION_LONG_OPT = "deploy-classication";
	private static final String FE_QUEUE_NAME_LONG_OPT = "feature-extraction-queue-names";
	private static final String FE_URL_LONG_OPT = "feature-extraction-url";
	
	private static final String CLASSIFICATION_OPT = "c";

	private static final String CLASSIFICATION_LONG_OPT = "classifye";

	
	private static final String HELP_OPT = "h";

	private static final String HELP_LONG_OPT = "help";


	
	public static String[] depoyFeatureExtraction(UimaAsynchronousEngine uimaAsEngine,String queueName,int scaleout,boolean sims,boolean rank,boolean trees) throws Exception {
		Map<String,Object> appCtx = new HashMap<String,Object>();
		appCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,System.getenv("UIMA_HOME") + "/bin/dd2spring.xsl");
		appCtx.put(UimaAsynchronousEngine.SaxonClasspath,"file:" + System.getenv("UIMA_HOME") + "/saxon/saxon8.jar");

		String feDescr = DescriptorGenerator.generateFeatureExtractionPipelineDeploymentDescriptor(queueName, scaleout,sims,rank,trees);
		String id2 = uimaAsEngine.deploy(new File(feDescr).getAbsolutePath(), appCtx);
		
		return new String[] {id2};
	}
	
	public static String depoyClassification(UimaAsynchronousEngine uimaAsEngine,String queueName,int scaleout,
			String featureExtractionURL,String featureExtractionQueueName) throws Exception {
		Map<String,Object> appCtx = new HashMap<String,Object>();
		appCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,System.getenv("UIMA_HOME") + "/bin/dd2spring.xsl");
		appCtx.put(UimaAsynchronousEngine.SaxonClasspath,"file:" + System.getenv("UIMA_HOME") + "/saxon/saxon8.jar");
		
		String descr = DescriptorGenerator.generateClassificationPipelineDeploymentDescriptor(
				queueName, featureExtractionURL, featureExtractionQueueName);
		String id = uimaAsEngine.deploy(new File(descr).getAbsolutePath(), appCtx);
		
		return id;
	}
	
	public static void undeployPipeline(String id,UimaAsynchronousEngine uimaAsEngine) throws Exception {
		uimaAsEngine.undeploy(id);
	}
	
	public static void main(String args[]) throws Exception {
		CommandLineParser parser = new DefaultParser();
		
		
//		depoyClassification(uimaAsEngine,"classificationQueue",1,"tcp://localhost:61616","featureExtractionQueue");
		
		Option deployFEOpt = new Option(DEPLOY_FEATURE_EXTRACTION_OPT, DEPLOY_FEATURE_EXTRACTION_LONG_OPT, false, "Deploys the feature extraction pipeline");
		
		Option queueName1Opt = new Option(QUEUE_NAME_OPT,QUEUE_NAME_LONG_OPT,true,"Name of the queue where the deploy pipeline will receive the requests");
		queueName1Opt.setArgName("queue name");
		queueName1Opt.setRequired(true);
		
		Option scaleoutOpt = new Option(SCALEOUT_NAME_OPT,SCALEOUT_LONG_NAME_OPT,true,"Number of instances to be instantiated for the feature extraction pipeline");
		scaleoutOpt.setArgName("scaleout");
		scaleoutOpt.setRequired(true);
		
		Option simsOpt = new Option(USE_SIMS_OPT, USE_SIMS_LONG_OPT, false, "Extract similarity features");
		Option rankOpt = new Option(USE_RANK_OPT, USE_RANK_LONG_OPT, false, "Extract rank feature");
		Option treesOpt = new Option(USE_TREES_OPT, USE_TREES_LONG_OPT, false, "Extract trees");

		Options deployFEOpts = new Options();
		deployFEOpts.addOption(queueName1Opt);
		deployFEOpts.addOption(scaleoutOpt);
		deployFEOpts.addOption(simsOpt);
		deployFEOpts.addOption(rankOpt);
		deployFEOpts.addOption(treesOpt);
		
		
		Option processDataOpt = new Option(PROCESS_DATA_OPT, PROCESS_DATA_LONG_OPT, false, "Processes an input dataset");
		
		Option queueName2Opt = new Option(QUEUE_NAME_OPT,QUEUE_NAME_LONG_OPT,true,"Name of the queue where the feature extraction pipeline is listening");
		queueName2Opt.setArgName("queue name");
		queueName2Opt.setRequired(true);
		
		Option url2Opt = new Option(URL_OPT,URL_LONG_OPT,true,"URL of the broker where the feature extraction pipeline is connected");
		url2Opt.setArgName("broker URL");
		url2Opt.setRequired(true);
		
		Option ifOpt = new Option(INPUT_FILE_OPT,INPUT_FILE_LONG_OPT,true,"Input file");
		ifOpt.setArgName("file path");
		ifOpt.setRequired(true);
		
		Option ofOpt = new Option(OUTPUT_FILE_OPT,OUTPUT_FILE_LONG_OPT,true,"Output file where the extracted feature for each instance will be saved");
		ofOpt.setArgName("file path");
		ofOpt.setRequired(true);
		
		Options processOpts = new Options();
		processOpts.addOption(url2Opt);
		processOpts.addOption(queueName2Opt);
		processOpts.addOption(ifOpt);
		processOpts.addOption(ofOpt);
		
		
		Option deployClassificationOpt = new Option(DEPLOY_CLASSIFICATION_OPT, DEPLOY_CLASSIFICATION_LONG_OPT, false, "Deploys classification pipeline");

		Option queueName3Opt = new Option(QUEUE_NAME_OPT,QUEUE_NAME_LONG_OPT,true,"Name of the queue where the classification pipeline will receive the requests");
		queueName3Opt.setArgName("queue name");
		queueName3Opt.setRequired(true);

		Option queueNameFEOpt = new Option(FE_QUEUE_NAME_OPT,FE_QUEUE_NAME_LONG_OPT,true,"Name of the queue where the classification pipeline is listening");
		queueNameFEOpt.setArgName("queue name");
		queueNameFEOpt.setRequired(true);
		

		Option urlFEOpt = new Option(FE_URL_OPT,FE_URL_LONG_OPT,true,"URL of the broker where the feature extraction pipeline is connected");
		urlFEOpt.setArgName("broker URL");
		urlFEOpt.setRequired(true);
		
		Options classificationDeploymentOpts = new Options();
		classificationDeploymentOpts.addOption(queueName3Opt);
		classificationDeploymentOpts.addOption(urlFEOpt);
		classificationDeploymentOpts.addOption(queueNameFEOpt);
		
		
		Option classificationOpt = new Option(CLASSIFICATION_OPT, CLASSIFICATION_LONG_OPT, false, "Classifies the input dataset");
		
		Option queueName4Opt = new Option(QUEUE_NAME_OPT,QUEUE_NAME_LONG_OPT,true,"Name of the queue where the classification pipeline is listening");
		queueName4Opt.setArgName("queue name");
		queueName4Opt.setRequired(true);
		
		Option url3Opt = new Option(URL_OPT,URL_LONG_OPT,true,"URL of the broker where the classification pipeline is connected");
		url3Opt.setArgName("broker URL");
		url3Opt.setRequired(true);
		
		Option of2Opt = new Option(OUTPUT_FILE_OPT,OUTPUT_FILE_LONG_OPT,true,"Output file where the predictions will be saved");
		ofOpt.setArgName("file path");
		ofOpt.setRequired(true);
		
		Options classificationOpts = new Options();
		classificationOpts.addOption(url3Opt);
		classificationOpts.addOption(queueName4Opt);
		classificationOpts.addOption(ifOpt);
		classificationOpts.addOption(of2Opt);
		
		
		Option helpOpt = new Option(HELP_OPT, HELP_LONG_OPT, false, "Help");
		
		OptionGroup optGr = new OptionGroup();
		optGr.addOption(deployFEOpt);
		optGr.addOption(processDataOpt);
		optGr.addOption(deployClassificationOpt);
		optGr.addOption(classificationOpt);
		optGr.setRequired(true);
		
		Options commandOptions = new Options();
		commandOptions.addOptionGroup(optGr);
		
		
		
		try {
			CommandLine line = parser.parse( commandOptions, Arrays.copyOfRange(args,0,Math.min(1, args.length)));
			if (line.hasOption(DEPLOY_FEATURE_EXTRACTION_OPT)) {
				line = parser.parse( deployFEOpts, Arrays.copyOfRange(args,1,args.length));
				int scaleout = Integer.parseInt(line.getOptionValue(SCALEOUT_NAME_OPT));
				String queueName = line.getOptionValue(QUEUE_NAME_OPT);
				boolean useSims = line.hasOption(USE_SIMS_OPT);
				boolean useRank = line.hasOption(USE_RANK_OPT);
				boolean useTrees = line.hasOption(USE_TREES_OPT);
				UimaAsynchronousEngine uimaAsEngine = new BaseUIMAAsynchronousEngine_impl();
				depoyFeatureExtraction(uimaAsEngine,queueName,scaleout,useSims,useRank,useTrees);
			} else if (line.hasOption(HELP_OPT)) {
				printHelp(commandOptions, deployFEOpts, processOpts, classificationDeploymentOpts, classificationOpts);

			}
			
		} catch (ParseException e) {
			printHelp(commandOptions, deployFEOpts, processOpts, classificationDeploymentOpts, classificationOpts);
		}
	}
	
	public static void printHelp(Options commandOptions,Options deployFEOpts,Options processOpts
			,Options classificationDeploymentOpts,Options classificationOpts) throws IOException {
		HelpFormatter formatter = new HelpFormatter();

		StringWriter sw = new StringWriter();
    	PrintWriter pw = new PrintWriter(sw);
    	
    	formatter.printUsage(pw, 1000000000, "java qa.qcri.iyas.Starter ", commandOptions);
    	pw.flush();
    	pw.close();
    	String cmd = sw.toString();
    	sw.close();
    	
    	System.out.println(cmd);
    	
    	sw = new StringWriter();
    	pw = new PrintWriter(sw);
    	pw.println("Commands:");
    	formatter.printOptions(pw, 1000, commandOptions, 2, 5);
    	pw.println("\nFeature extraction pipeline deployment options:");
    	formatter.printOptions(pw, 1000, deployFEOpts, 4, 5);
    	pw.println("\nFeature extraction options:");
    	formatter.printOptions(pw, 2000, processOpts, 4, 5);
    	pw.println("\nClassification pipeline deployment options:");
    	formatter.printOptions(pw, 2000, classificationDeploymentOpts, 4, 5);
    	pw.println("\nClassification pipeline options:");
    	formatter.printOptions(pw, 2000, classificationOpts, 4, 5);
    	pw.flush();
    	System.out.println(sw.toString());
	}
}
