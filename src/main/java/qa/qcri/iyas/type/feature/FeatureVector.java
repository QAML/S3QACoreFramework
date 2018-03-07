

/* First created by JCasGen Mon Mar 05 10:11:00 CET 2018 */
package qa.qcri.iyas.type.feature;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.DoubleArray;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Mar 05 10:11:00 CET 2018
 * XML source: /home/sromeo/workspaces/UIMA/workspace/S3QACoreFramework/src/main/resources/descriptors/qa/qcri/iyas/type/FeatureTypeSystemDescriptor.xml
 * @generated */
public class FeatureVector extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(FeatureVector.class);
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
  protected FeatureVector() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public FeatureVector(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public FeatureVector(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public FeatureVector(JCas jcas, int begin, int end) {
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
  //* Feature: features

  /** getter for features - gets 
   * @generated
   * @return value of the feature 
   */
  public DoubleArray getFeatures() {
    if (FeatureVector_Type.featOkTst && ((FeatureVector_Type)jcasType).casFeat_features == null)
      jcasType.jcas.throwFeatMissing("features", "qa.qcri.iyas.type.feature.FeatureVector");
    return (DoubleArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((FeatureVector_Type)jcasType).casFeatCode_features)));}
    
  /** setter for features - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setFeatures(DoubleArray v) {
    if (FeatureVector_Type.featOkTst && ((FeatureVector_Type)jcasType).casFeat_features == null)
      jcasType.jcas.throwFeatMissing("features", "qa.qcri.iyas.type.feature.FeatureVector");
    jcasType.ll_cas.ll_setRefValue(addr, ((FeatureVector_Type)jcasType).casFeatCode_features, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for features - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public double getFeatures(int i) {
    if (FeatureVector_Type.featOkTst && ((FeatureVector_Type)jcasType).casFeat_features == null)
      jcasType.jcas.throwFeatMissing("features", "qa.qcri.iyas.type.feature.FeatureVector");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((FeatureVector_Type)jcasType).casFeatCode_features), i);
    return jcasType.ll_cas.ll_getDoubleArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((FeatureVector_Type)jcasType).casFeatCode_features), i);}

  /** indexed setter for features - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setFeatures(int i, double v) { 
    if (FeatureVector_Type.featOkTst && ((FeatureVector_Type)jcasType).casFeat_features == null)
      jcasType.jcas.throwFeatMissing("features", "qa.qcri.iyas.type.feature.FeatureVector");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((FeatureVector_Type)jcasType).casFeatCode_features), i);
    jcasType.ll_cas.ll_setDoubleArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((FeatureVector_Type)jcasType).casFeatCode_features), i, v);}
  }

    