

/* First created by JCasGen Mon Mar 19 07:46:39 CET 2018 */
package qa.qcri.iyas.type.representation;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.DoubleArray;


/** 
 * Updated by JCasGen Tue Mar 20 16:27:12 CET 2018
 * XML source: /home/sromeo/workspaces/UIMA/workspace/S3QACoreFramework/src/main/resources/descriptors/qa/qcri/iyas/type/RepresentationTypeSystemDescriptor.xml
 * @generated */
public class DenseVector extends Representation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(DenseVector.class);
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
  protected DenseVector() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public DenseVector(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public DenseVector(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public DenseVector(JCas jcas, int begin, int end) {
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
    if (DenseVector_Type.featOkTst && ((DenseVector_Type)jcasType).casFeat_features == null)
      jcasType.jcas.throwFeatMissing("features", "qa.qcri.iyas.type.representation.DenseVector");
    return (DoubleArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DenseVector_Type)jcasType).casFeatCode_features)));}
    
  /** setter for features - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setFeatures(DoubleArray v) {
    if (DenseVector_Type.featOkTst && ((DenseVector_Type)jcasType).casFeat_features == null)
      jcasType.jcas.throwFeatMissing("features", "qa.qcri.iyas.type.representation.DenseVector");
    jcasType.ll_cas.ll_setRefValue(addr, ((DenseVector_Type)jcasType).casFeatCode_features, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for features - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public double getFeatures(int i) {
    if (DenseVector_Type.featOkTst && ((DenseVector_Type)jcasType).casFeat_features == null)
      jcasType.jcas.throwFeatMissing("features", "qa.qcri.iyas.type.representation.DenseVector");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((DenseVector_Type)jcasType).casFeatCode_features), i);
    return jcasType.ll_cas.ll_getDoubleArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((DenseVector_Type)jcasType).casFeatCode_features), i);}

  /** indexed setter for features - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setFeatures(int i, double v) { 
    if (DenseVector_Type.featOkTst && ((DenseVector_Type)jcasType).casFeat_features == null)
      jcasType.jcas.throwFeatMissing("features", "qa.qcri.iyas.type.representation.DenseVector");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((DenseVector_Type)jcasType).casFeatCode_features), i);
    jcasType.ll_cas.ll_setDoubleArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((DenseVector_Type)jcasType).casFeatCode_features), i, v);}
  }

    