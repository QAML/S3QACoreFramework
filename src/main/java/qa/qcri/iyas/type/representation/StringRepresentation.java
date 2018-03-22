

/* First created by JCasGen Mon Mar 19 07:47:39 CET 2018 */
package qa.qcri.iyas.type.representation;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Tue Mar 20 16:27:12 CET 2018
 * XML source: /home/sromeo/workspaces/UIMA/workspace/S3QACoreFramework/src/main/resources/descriptors/qa/qcri/iyas/type/RepresentationTypeSystemDescriptor.xml
 * @generated */
public class StringRepresentation extends Representation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(StringRepresentation.class);
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
  protected StringRepresentation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public StringRepresentation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public StringRepresentation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public StringRepresentation(JCas jcas, int begin, int end) {
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
  //* Feature: content

  /** getter for content - gets 
   * @generated
   * @return value of the feature 
   */
  public String getContent() {
    if (StringRepresentation_Type.featOkTst && ((StringRepresentation_Type)jcasType).casFeat_content == null)
      jcasType.jcas.throwFeatMissing("content", "qa.qcri.iyas.type.representation.StringRepresentation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((StringRepresentation_Type)jcasType).casFeatCode_content);}
    
  /** setter for content - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setContent(String v) {
    if (StringRepresentation_Type.featOkTst && ((StringRepresentation_Type)jcasType).casFeat_content == null)
      jcasType.jcas.throwFeatMissing("content", "qa.qcri.iyas.type.representation.StringRepresentation");
    jcasType.ll_cas.ll_setStringValue(addr, ((StringRepresentation_Type)jcasType).casFeatCode_content, v);}    
  }

    