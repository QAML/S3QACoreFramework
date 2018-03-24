

/* First created by JCasGen Tue Mar 20 15:20:49 CET 2018 */
package qa.qcri.iyas.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu Mar 22 15:29:39 CET 2018
 * XML source: /home/sromeo/workspaces/UIMA/workspace/S3QACoreFramework/src/main/resources/descriptors/qa/qcri/iyas/type/GeneralTypeSystemDescriptor.xml
 * @generated */
public class AdditionalInfo extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(AdditionalInfo.class);
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
  protected AdditionalInfo() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public AdditionalInfo(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public AdditionalInfo(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public AdditionalInfo(JCas jcas, int begin, int end) {
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
  //* Feature: index

  /** getter for index - gets 
   * @generated
   * @return value of the feature 
   */
  public int getIndex() {
    if (AdditionalInfo_Type.featOkTst && ((AdditionalInfo_Type)jcasType).casFeat_index == null)
      jcasType.jcas.throwFeatMissing("index", "qa.qcri.iyas.type.AdditionalInfo");
    return jcasType.ll_cas.ll_getIntValue(addr, ((AdditionalInfo_Type)jcasType).casFeatCode_index);}
    
  /** setter for index - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setIndex(int v) {
    if (AdditionalInfo_Type.featOkTst && ((AdditionalInfo_Type)jcasType).casFeat_index == null)
      jcasType.jcas.throwFeatMissing("index", "qa.qcri.iyas.type.AdditionalInfo");
    jcasType.ll_cas.ll_setIntValue(addr, ((AdditionalInfo_Type)jcasType).casFeatCode_index, v);}    
   
    
  //*--------------*
  //* Feature: totalNumberOfExamples

  /** getter for totalNumberOfExamples - gets 
   * @generated
   * @return value of the feature 
   */
  public int getTotalNumberOfExamples() {
    if (AdditionalInfo_Type.featOkTst && ((AdditionalInfo_Type)jcasType).casFeat_totalNumberOfExamples == null)
      jcasType.jcas.throwFeatMissing("totalNumberOfExamples", "qa.qcri.iyas.type.AdditionalInfo");
    return jcasType.ll_cas.ll_getIntValue(addr, ((AdditionalInfo_Type)jcasType).casFeatCode_totalNumberOfExamples);}
    
  /** setter for totalNumberOfExamples - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTotalNumberOfExamples(int v) {
    if (AdditionalInfo_Type.featOkTst && ((AdditionalInfo_Type)jcasType).casFeat_totalNumberOfExamples == null)
      jcasType.jcas.throwFeatMissing("totalNumberOfExamples", "qa.qcri.iyas.type.AdditionalInfo");
    jcasType.ll_cas.ll_setIntValue(addr, ((AdditionalInfo_Type)jcasType).casFeatCode_totalNumberOfExamples, v);}    
   
    
  //*--------------*
  //* Feature: requesterID

  /** getter for requesterID - gets 
   * @generated
   * @return value of the feature 
   */
  public String getRequesterID() {
    if (AdditionalInfo_Type.featOkTst && ((AdditionalInfo_Type)jcasType).casFeat_requesterID == null)
      jcasType.jcas.throwFeatMissing("requesterID", "qa.qcri.iyas.type.AdditionalInfo");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AdditionalInfo_Type)jcasType).casFeatCode_requesterID);}
    
  /** setter for requesterID - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setRequesterID(String v) {
    if (AdditionalInfo_Type.featOkTst && ((AdditionalInfo_Type)jcasType).casFeat_requesterID == null)
      jcasType.jcas.throwFeatMissing("requesterID", "qa.qcri.iyas.type.AdditionalInfo");
    jcasType.ll_cas.ll_setStringValue(addr, ((AdditionalInfo_Type)jcasType).casFeatCode_requesterID, v);}    
   
    
  //*--------------*
  //* Feature: instanceID

  /** getter for instanceID - gets 
   * @generated
   * @return value of the feature 
   */
  public String getInstanceID() {
    if (AdditionalInfo_Type.featOkTst && ((AdditionalInfo_Type)jcasType).casFeat_instanceID == null)
      jcasType.jcas.throwFeatMissing("instanceID", "qa.qcri.iyas.type.AdditionalInfo");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AdditionalInfo_Type)jcasType).casFeatCode_instanceID);}
    
  /** setter for instanceID - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setInstanceID(String v) {
    if (AdditionalInfo_Type.featOkTst && ((AdditionalInfo_Type)jcasType).casFeat_instanceID == null)
      jcasType.jcas.throwFeatMissing("instanceID", "qa.qcri.iyas.type.AdditionalInfo");
    jcasType.ll_cas.ll_setStringValue(addr, ((AdditionalInfo_Type)jcasType).casFeatCode_instanceID, v);}    
   
    
  //*--------------*
  //* Feature: prediction

  /** getter for prediction - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPrediction() {
    if (AdditionalInfo_Type.featOkTst && ((AdditionalInfo_Type)jcasType).casFeat_prediction == null)
      jcasType.jcas.throwFeatMissing("prediction", "qa.qcri.iyas.type.AdditionalInfo");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AdditionalInfo_Type)jcasType).casFeatCode_prediction);}
    
  /** setter for prediction - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPrediction(String v) {
    if (AdditionalInfo_Type.featOkTst && ((AdditionalInfo_Type)jcasType).casFeat_prediction == null)
      jcasType.jcas.throwFeatMissing("prediction", "qa.qcri.iyas.type.AdditionalInfo");
    jcasType.ll_cas.ll_setStringValue(addr, ((AdditionalInfo_Type)jcasType).casFeatCode_prediction, v);}    
  }

    