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
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** 
 * Updated by JCasGen Mon Dec 11 14:36:21 AST 2017
 * @generated */
public class Comment_Type extends QAAnnotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Comment.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("qa.qcri.iyas.types.Comment");
 
  /** @generated */
  final Feature casFeat_questionID;
  /** @generated */
  final int     casFeatCode_questionID;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getQuestionID(int addr) {
        if (featOkTst && casFeat_questionID == null)
      jcas.throwFeatMissing("questionID", "qa.qcri.iyas.types.Comment");
    return ll_cas.ll_getStringValue(addr, casFeatCode_questionID);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setQuestionID(int addr, String v) {
        if (featOkTst && casFeat_questionID == null)
      jcas.throwFeatMissing("questionID", "qa.qcri.iyas.types.Comment");
    ll_cas.ll_setStringValue(addr, casFeatCode_questionID, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Comment_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_questionID = jcas.getRequiredFeatureDE(casType, "questionID", "uima.cas.String", featOkTst);
    casFeatCode_questionID  = (null == casFeat_questionID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_questionID).getCode();

  }
}



    