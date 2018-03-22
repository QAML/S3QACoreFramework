

/* First created by JCasGen Sun Mar 04 15:52:51 CET 2018 */
package qa.qcri.iyas.type.cqa;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



import org.apache.uima.jcas.cas.StringArray;


import org.apache.uima.jcas.cas.StringList;


/** 
 * Updated by JCasGen Thu Mar 22 09:14:34 CET 2018
 * XML source: /home/sromeo/workspaces/UIMA/workspace/S3QACoreFramework/src/main/resources/descriptors/qa/qcri/iyas/type/CQATypeSystemDescriptor.xml
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
      jcasType.jcas.throwFeatMissing("questionID", "qa.qcri.iyas.type.cqa.Comment");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Comment_Type)jcasType).casFeatCode_questionID);}
    
  /** setter for questionID - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setQuestionID(String v) {
    if (Comment_Type.featOkTst && ((Comment_Type)jcasType).casFeat_questionID == null)
      jcasType.jcas.throwFeatMissing("questionID", "qa.qcri.iyas.type.cqa.Comment");
    jcasType.ll_cas.ll_setStringValue(addr, ((Comment_Type)jcasType).casFeatCode_questionID, v);}    
   
    
  //*--------------*
  //* Feature: labels

  /** getter for labels - gets 
   * @generated
   * @return value of the feature 
   */
  public StringArray getLabels() {
    if (Comment_Type.featOkTst && ((Comment_Type)jcasType).casFeat_labels == null)
      jcasType.jcas.throwFeatMissing("labels", "qa.qcri.iyas.type.cqa.Comment");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Comment_Type)jcasType).casFeatCode_labels)));}
    
  /** setter for labels - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setLabels(StringArray v) {
    if (Comment_Type.featOkTst && ((Comment_Type)jcasType).casFeat_labels == null)
      jcasType.jcas.throwFeatMissing("labels", "qa.qcri.iyas.type.cqa.Comment");
    jcasType.ll_cas.ll_setRefValue(addr, ((Comment_Type)jcasType).casFeatCode_labels, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for labels - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getLabels(int i) {
    if (Comment_Type.featOkTst && ((Comment_Type)jcasType).casFeat_labels == null)
      jcasType.jcas.throwFeatMissing("labels", "qa.qcri.iyas.type.cqa.Comment");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Comment_Type)jcasType).casFeatCode_labels), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Comment_Type)jcasType).casFeatCode_labels), i);}

  /** indexed setter for labels - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setLabels(int i, String v) { 
    if (Comment_Type.featOkTst && ((Comment_Type)jcasType).casFeat_labels == null)
      jcasType.jcas.throwFeatMissing("labels", "qa.qcri.iyas.type.cqa.Comment");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Comment_Type)jcasType).casFeatCode_labels), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Comment_Type)jcasType).casFeatCode_labels), i, v);}
   
    
  //*--------------*
  //* Feature: rank

  /** getter for rank - gets 
   * @generated
   * @return value of the feature 
   */
  public int getRank() {
    if (Comment_Type.featOkTst && ((Comment_Type)jcasType).casFeat_rank == null)
      jcasType.jcas.throwFeatMissing("rank", "qa.qcri.iyas.type.cqa.Comment");
    return jcasType.ll_cas.ll_getIntValue(addr, ((Comment_Type)jcasType).casFeatCode_rank);}
    
  /** setter for rank - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setRank(int v) {
    if (Comment_Type.featOkTst && ((Comment_Type)jcasType).casFeat_rank == null)
      jcasType.jcas.throwFeatMissing("rank", "qa.qcri.iyas.type.cqa.Comment");
    jcasType.ll_cas.ll_setIntValue(addr, ((Comment_Type)jcasType).casFeatCode_rank, v);}    
  }

    