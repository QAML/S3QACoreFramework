

/* First created by JCasGen Thu Nov 09 13:31:39 AST 2017 */
package qa.qcri.iyas.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu Nov 09 13:31:39 AST 2017
 * XML source: /home/shared_files/UIMA/workspace/cqa-uima-pipeline-all/cqa-uima-pipeline-core/resources/descriptors/qa/qcri/iyas/types/CQATypeSystemDescriptorFile.xml
 * @generated */
public class UserQuestion extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(UserQuestion.class);
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
  protected UserQuestion() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public UserQuestion(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public UserQuestion(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public UserQuestion(JCas jcas, int begin, int end) {
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
  //* Feature: concatenated

  /** getter for concatenated - gets 
   * @generated
   * @return value of the feature 
   */
  public boolean getConcatenated() {
    if (UserQuestion_Type.featOkTst && ((UserQuestion_Type)jcasType).casFeat_concatenated == null)
      jcasType.jcas.throwFeatMissing("concatenated", "qa.qcri.iyas.types.UserQuestion");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((UserQuestion_Type)jcasType).casFeatCode_concatenated);}
    
  /** setter for concatenated - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setConcatenated(boolean v) {
    if (UserQuestion_Type.featOkTst && ((UserQuestion_Type)jcasType).casFeat_concatenated == null)
      jcasType.jcas.throwFeatMissing("concatenated", "qa.qcri.iyas.types.UserQuestion");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((UserQuestion_Type)jcasType).casFeatCode_concatenated, v);}    
   
    
  //*--------------*
  //* Feature: candidateViewNames

  /** getter for candidateViewNames - gets 
   * @generated
   * @return value of the feature 
   */
  public StringArray getCandidateViewNames() {
    if (UserQuestion_Type.featOkTst && ((UserQuestion_Type)jcasType).casFeat_candidateViewNames == null)
      jcasType.jcas.throwFeatMissing("candidateViewNames", "qa.qcri.iyas.types.UserQuestion");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((UserQuestion_Type)jcasType).casFeatCode_candidateViewNames)));}
    
  /** setter for candidateViewNames - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setCandidateViewNames(StringArray v) {
    if (UserQuestion_Type.featOkTst && ((UserQuestion_Type)jcasType).casFeat_candidateViewNames == null)
      jcasType.jcas.throwFeatMissing("candidateViewNames", "qa.qcri.iyas.types.UserQuestion");
    jcasType.ll_cas.ll_setRefValue(addr, ((UserQuestion_Type)jcasType).casFeatCode_candidateViewNames, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for candidateViewNames - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getCandidateViewNames(int i) {
    if (UserQuestion_Type.featOkTst && ((UserQuestion_Type)jcasType).casFeat_candidateViewNames == null)
      jcasType.jcas.throwFeatMissing("candidateViewNames", "qa.qcri.iyas.types.UserQuestion");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((UserQuestion_Type)jcasType).casFeatCode_candidateViewNames), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((UserQuestion_Type)jcasType).casFeatCode_candidateViewNames), i);}

  /** indexed setter for candidateViewNames - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setCandidateViewNames(int i, String v) { 
    if (UserQuestion_Type.featOkTst && ((UserQuestion_Type)jcasType).casFeat_candidateViewNames == null)
      jcasType.jcas.throwFeatMissing("candidateViewNames", "qa.qcri.iyas.types.UserQuestion");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((UserQuestion_Type)jcasType).casFeatCode_candidateViewNames), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((UserQuestion_Type)jcasType).casFeatCode_candidateViewNames), i, v);}
   
    
  //*--------------*
  //* Feature: ID

  /** getter for ID - gets 
   * @generated
   * @return value of the feature 
   */
  public String getID() {
    if (UserQuestion_Type.featOkTst && ((UserQuestion_Type)jcasType).casFeat_ID == null)
      jcasType.jcas.throwFeatMissing("ID", "qa.qcri.iyas.types.UserQuestion");
    return jcasType.ll_cas.ll_getStringValue(addr, ((UserQuestion_Type)jcasType).casFeatCode_ID);}
    
  /** setter for ID - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setID(String v) {
    if (UserQuestion_Type.featOkTst && ((UserQuestion_Type)jcasType).casFeat_ID == null)
      jcasType.jcas.throwFeatMissing("ID", "qa.qcri.iyas.types.UserQuestion");
    jcasType.ll_cas.ll_setStringValue(addr, ((UserQuestion_Type)jcasType).casFeatCode_ID, v);}    
  }

    