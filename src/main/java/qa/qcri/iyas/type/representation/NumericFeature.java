

/* First created by JCasGen Mon Mar 19 07:46:39 CET 2018 */
package qa.qcri.iyas.type.representation;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Tue Mar 20 16:27:12 CET 2018
 * XML source: /home/sromeo/workspaces/UIMA/workspace/S3QACoreFramework/src/main/resources/descriptors/qa/qcri/iyas/type/RepresentationTypeSystemDescriptor.xml
 * @generated */
public class NumericFeature extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(NumericFeature.class);
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
  protected NumericFeature() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public NumericFeature(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public NumericFeature(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public NumericFeature(JCas jcas, int begin, int end) {
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
    if (NumericFeature_Type.featOkTst && ((NumericFeature_Type)jcasType).casFeat_index == null)
      jcasType.jcas.throwFeatMissing("index", "qa.qcri.iyas.type.representation.NumericFeature");
    return jcasType.ll_cas.ll_getIntValue(addr, ((NumericFeature_Type)jcasType).casFeatCode_index);}
    
  /** setter for index - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setIndex(int v) {
    if (NumericFeature_Type.featOkTst && ((NumericFeature_Type)jcasType).casFeat_index == null)
      jcasType.jcas.throwFeatMissing("index", "qa.qcri.iyas.type.representation.NumericFeature");
    jcasType.ll_cas.ll_setIntValue(addr, ((NumericFeature_Type)jcasType).casFeatCode_index, v);}    
   
    
  //*--------------*
  //* Feature: value

  /** getter for value - gets 
   * @generated
   * @return value of the feature 
   */
  public double getValue() {
    if (NumericFeature_Type.featOkTst && ((NumericFeature_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "qa.qcri.iyas.type.representation.NumericFeature");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((NumericFeature_Type)jcasType).casFeatCode_value);}
    
  /** setter for value - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setValue(double v) {
    if (NumericFeature_Type.featOkTst && ((NumericFeature_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "qa.qcri.iyas.type.representation.NumericFeature");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((NumericFeature_Type)jcasType).casFeatCode_value, v);}    
   
    
  //*--------------*
  //* Feature: name

  /** getter for name - gets 
   * @generated
   * @return value of the feature 
   */
  public String getName() {
    if (NumericFeature_Type.featOkTst && ((NumericFeature_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "qa.qcri.iyas.type.representation.NumericFeature");
    return jcasType.ll_cas.ll_getStringValue(addr, ((NumericFeature_Type)jcasType).casFeatCode_name);}
    
  /** setter for name - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setName(String v) {
    if (NumericFeature_Type.featOkTst && ((NumericFeature_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "qa.qcri.iyas.type.representation.NumericFeature");
    jcasType.ll_cas.ll_setStringValue(addr, ((NumericFeature_Type)jcasType).casFeatCode_name, v);}    
  }

    