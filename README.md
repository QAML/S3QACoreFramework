# S3QACoreFramework
Core Framework of Semantic/Syntactic Structures-based QA Pipeline 

## Installation

### Requirements

1. *Java 8 SDK* is required by some of the UIMA components used by S3QACoreFramework, i.e. UIMA AS Asynchronous Scaleout. 
2. *UIMA AS Asynchronous Scaleout*
   - The S3QACoreFramework has been tested with UIMA AS Asynchronous Scaleout version 2.10.2. The direct link to the download page is [http://uima.apache.org/downloads.cgi#Latest%20Official%20Releases](here). 
   - make sure you execute the instructions in the README (especially the ones about setting the environmental variables)
   - For more information on installing UIMA and the Eclipse plugins, refer to the [https://uima.apache.org/d/uimaj-current/overview_and_setup.html](main page of the project)(section 3.1.3 explains how to install UIMA components). 

### Installing S3QACoreFramework

To install the core pipeline, the following steps are needed:

1. *Clone S3QACoreFramework repository*
   - git clone https://github.com/QAML/S3QACoreFramework.git
2. *Compile S3QACoreFramework and its dependencies*
   - go to the S3QACoreFramework project folder
   - type *mvn compile*
   - create a jar: *mvn package -DskipTests*


### Getting Started

See the tutorial section of the wiki on the github page of the project
