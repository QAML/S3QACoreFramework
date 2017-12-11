/**
 * Copyright 2017 Salvatore Romeo
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
 
 


/* First created by JCasGen Mon Dec 11 14:36:21 AST 2017 */
package qa.qcri.iyas.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Mon Dec 11 14:36:21 AST 2017
 * XML source: /home/shared_files/UIMA/workspace/cqa-uima-pipeline-all/cqa-uima-pipeline-core/resources/descriptors/qa/qcri/iyas/types/CQATypeSystemDescriptorFile.xml
 * @generated */
public class RelatedQuestionBody extends QAAnnotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(RelatedQuestionBody.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected RelatedQuestionBody() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public RelatedQuestionBody(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public RelatedQuestionBody(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public RelatedQuestionBody(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: numberOfCandidates

  /** getter for numberOfCandidates - gets 
   * @generated
   * @return value of the feature 
   */
  public int getNumberOfCandidates() {
    if (RelatedQuestionBody_Type.featOkTst && ((RelatedQuestionBody_Type)jcasType).casFeat_numberOfCandidates == null)
      jcasType.jcas.throwFeatMissing("numberOfCandidates", "qa.qcri.iyas.types.RelatedQuestionBody");
    return jcasType.ll_cas.ll_getIntValue(addr, ((RelatedQuestionBody_Type)jcasType).casFeatCode_numberOfCandidates);}
    
  /** setter for numberOfCandidates - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setNumberOfCandidates(int v) {
    if (RelatedQuestionBody_Type.featOkTst && ((RelatedQuestionBody_Type)jcasType).casFeat_numberOfCandidates == null)
      jcasType.jcas.throwFeatMissing("numberOfCandidates", "qa.qcri.iyas.types.RelatedQuestionBody");
    jcasType.ll_cas.ll_setIntValue(addr, ((RelatedQuestionBody_Type)jcasType).casFeatCode_numberOfCandidates, v);}    
   
    
  //*--------------*
  //* Feature: concatenated

  /** getter for concatenated - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getConcatenated() {
    if (RelatedQuestionBody_Type.featOkTst && ((RelatedQuestionBody_Type)jcasType).casFeat_concatenated == null)
      jcasType.jcas.throwFeatMissing("concatenated", "qa.qcri.iyas.types.RelatedQuestionBody");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((RelatedQuestionBody_Type)jcasType).casFeatCode_concatenated);}
    
  /** setter for concatenated - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setConcatenated(boolean v) {
    if (RelatedQuestionBody_Type.featOkTst && ((RelatedQuestionBody_Type)jcasType).casFeat_concatenated == null)
      jcasType.jcas.throwFeatMissing("concatenated", "qa.qcri.iyas.types.RelatedQuestionBody");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((RelatedQuestionBody_Type)jcasType).casFeatCode_concatenated, v);}    
  }

    