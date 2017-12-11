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
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** 
 * Updated by JCasGen Mon Dec 11 14:36:21 AST 2017
 * @generated */
public class UserQuestionBody_Type extends QAAnnotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = UserQuestionBody.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("qa.qcri.iyas.types.UserQuestionBody");
 
  /** @generated */
  final Feature casFeat_numberOfCandidates;
  /** @generated */
  final int     casFeatCode_numberOfCandidates;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getNumberOfCandidates(int addr) {
        if (featOkTst && casFeat_numberOfCandidates == null)
      jcas.throwFeatMissing("numberOfCandidates", "qa.qcri.iyas.types.UserQuestionBody");
    return ll_cas.ll_getIntValue(addr, casFeatCode_numberOfCandidates);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setNumberOfCandidates(int addr, int v) {
        if (featOkTst && casFeat_numberOfCandidates == null)
      jcas.throwFeatMissing("numberOfCandidates", "qa.qcri.iyas.types.UserQuestionBody");
    ll_cas.ll_setIntValue(addr, casFeatCode_numberOfCandidates, v);}
    
  
 
  /** @generated */
  final Feature casFeat_concatenated;
  /** @generated */
  final int     casFeatCode_concatenated;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public boolean getConcatenated(int addr) {
        if (featOkTst && casFeat_concatenated == null)
      jcas.throwFeatMissing("concatenated", "qa.qcri.iyas.types.UserQuestionBody");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_concatenated);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setConcatenated(int addr, boolean v) {
        if (featOkTst && casFeat_concatenated == null)
      jcas.throwFeatMissing("concatenated", "qa.qcri.iyas.types.UserQuestionBody");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_concatenated, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public UserQuestionBody_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_numberOfCandidates = jcas.getRequiredFeatureDE(casType, "numberOfCandidates", "uima.cas.Integer", featOkTst);
    casFeatCode_numberOfCandidates  = (null == casFeat_numberOfCandidates) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_numberOfCandidates).getCode();

 
    casFeat_concatenated = jcas.getRequiredFeatureDE(casType, "concatenated", "uima.cas.Boolean", featOkTst);
    casFeatCode_concatenated  = (null == casFeat_concatenated) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_concatenated).getCode();

  }
}



    