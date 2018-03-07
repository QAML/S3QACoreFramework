

/* First created by JCasGen Sun Mar 04 15:58:29 CET 2018 */
package qa.qcri.iyas.type.feature;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Mar 05 10:11:00 CET 2018
 * XML source: /home/sromeo/workspaces/UIMA/workspace/S3QACoreFramework/src/main/resources/descriptors/qa/qcri/iyas/type/FeatureTypeSystemDescriptor.xml
 * @generated */
public class Similarity extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Similarity.class);
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
  protected Similarity() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Similarity(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Similarity(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Similarity(JCas jcas, int begin, int end) {
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
  //* Feature: value

  /** getter for value - gets 
   * @generated
   * @return value of the feature 
   */
  public double getValue() {
    if (Similarity_Type.featOkTst && ((Similarity_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "qa.qcri.iyas.type.feature.Similarity");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((Similarity_Type)jcasType).casFeatCode_value);}
    
  /** setter for value - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setValue(double v) {
    if (Similarity_Type.featOkTst && ((Similarity_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "qa.qcri.iyas.type.feature.Similarity");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((Similarity_Type)jcasType).casFeatCode_value, v);}    
   
    
  //*--------------*
  //* Feature: name

  /** getter for name - gets 
   * @generated
   * @return value of the feature 
   */
  public String getName() {
    if (Similarity_Type.featOkTst && ((Similarity_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "qa.qcri.iyas.type.feature.Similarity");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Similarity_Type)jcasType).casFeatCode_name);}
    
  /** setter for name - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setName(String v) {
    if (Similarity_Type.featOkTst && ((Similarity_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "qa.qcri.iyas.type.feature.Similarity");
    jcasType.ll_cas.ll_setStringValue(addr, ((Similarity_Type)jcasType).casFeatCode_name, v);}    
  }

    