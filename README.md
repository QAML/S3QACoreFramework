# S3QACoreFramework
Core Framework of Semantic/Syntactic Structures-based QA Pipeline 

## Installation
To install the core pipeline, the following steps are needed:

1. *Clone S3QACoreFramework repository*
   - git clone https://github.com/QAML/S3QACoreFramework.git
2. *Install UIMA Framework*
   - Please follow the steps reported in [https://uima.apache.org/d/uimaj-current/overview_and_setup.html](https://uima.apache.org/d/uimaj-current/overview_and_setup.html#ugr.ovv.eclipse_setup.installation)

## Usage
usage: java qa.qcri.iyas.Starter  -c | -dc | -dfe | -ef

Commands:
  -c,--classifye                       Classifies the input dataset
  -dc,--deploy-classication            Deploys classification pipeline
  -dfe,--deploy-feature-extraction     Deploys the feature extraction pipeline
  -ef,--extraction-features            Processes an input dataset

Feature extraction pipeline deployment options:
    -qn,--queue-name <queue name>     Name of the queue where the deploy pipeline will receive the requests
    -r,--rank                         Extract rank feature
    -s,--sims                         Extract similarity features
    -sc,--scaleout <scaleout>         Number of instances to be instantiated for the feature extraction pipeline
    -t,--t                            Extract trees

Feature extraction options:
    -if,--input-file <file path>      Input file
    -of,--output-file <file path>     Output file where the extracted feature for each instance will be saved
    -qn,--queue-name <queue name>     Name of the queue where the feature extraction pipeline is listening
    -u,--url <broker URL>             URL of the broker where the feature extraction pipeline is connected

Classification pipeline deployment options:
    -fqn,--feature-extraction-queue-names <queue name>     Name of the queue where the classification pipeline is listening
    -fu,--feature-extraction-url <broker URL>              URL of the broker where the feature extraction pipeline is connected
    -qn,--queue-name <queue name>                          Name of the queue where the classification pipeline will receive the requests

Classification pipeline options:
    -if,--input-file <file path>      Input file
    -of,--output-file <arg>           Output file where the predictions will be saved
    -qn,--queue-name <queue name>     Name of the queue where the classification pipeline is listening
    -u,--url <broker URL>             URL of the broker where the classification pipeline is connected
