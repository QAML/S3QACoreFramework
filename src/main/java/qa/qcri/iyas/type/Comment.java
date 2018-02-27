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
package qa.qcri.iyas.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Sun Feb 25 15:10:21 AST 2018
 * XML source: /home/sromeo/workspaces/UIMA/workspace/S3QACoreFramework/src/main/resources/descriptors/qa/qcri/iyas/type/CQATypeSystemDescriptorFile.xml
 * @generated */
public class Comment extends QAAnnotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Comment.class);
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
  protected Comment() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Comment(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Comment(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Comment(JCas jcas, int begin, int end) {
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
  //* Feature: questionID

  /** getter for questionID - gets 
   * @generated
   * @return value of the feature 
   */
  public String getQuestionID() {
    if (Comment_Type.featOkTst && ((Comment_Type)jcasType).casFeat_questionID == null)
      jcasType.jcas.throwFeatMissing("questionID", "qa.qcri.iyas.type.Comment");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Comment_Type)jcasType).casFeatCode_questionID);}
    
  /** setter for questionID - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setQuestionID(String v) {
    if (Comment_Type.featOkTst && ((Comment_Type)jcasType).casFeat_questionID == null)
      jcasType.jcas.throwFeatMissing("questionID", "qa.qcri.iyas.type.Comment");
    jcasType.ll_cas.ll_setStringValue(addr, ((Comment_Type)jcasType).casFeatCode_questionID, v);}    
  }

    