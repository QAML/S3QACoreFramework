

/* First created by JCasGen Wed Mar 21 14:30:44 CET 2018 */
package qa.qcri.iyas.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu Mar 22 15:29:39 CET 2018
 * XML source: /home/sromeo/workspaces/UIMA/workspace/S3QACoreFramework/src/main/resources/descriptors/qa/qcri/iyas/type/GeneralTypeSystemDescriptor.xml
 * @generated */
public class Model extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Model.class);
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
  protected Model() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Model(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Model(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Model(JCas jcas, int begin, int end) {
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
  //* Feature: file

  /** getter for file - gets 
   * @generated
   * @return value of the feature 
   */
  public String getFile() {
    if (Model_Type.featOkTst && ((Model_Type)jcasType).casFeat_file == null)
      jcasType.jcas.throwFeatMissing("file", "qa.qcri.iyas.type.Model");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Model_Type)jcasType).casFeatCode_file);}
    
  /** setter for file - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setFile(String v) {
    if (Model_Type.featOkTst && ((Model_Type)jcasType).casFeat_file == null)
      jcasType.jcas.throwFeatMissing("file", "qa.qcri.iyas.type.Model");
    jcasType.ll_cas.ll_setStringValue(addr, ((Model_Type)jcasType).casFeatCode_file, v);}    
  }

    