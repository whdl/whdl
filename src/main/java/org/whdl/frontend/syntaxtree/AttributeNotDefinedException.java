package org.whdl.frontend.syntaxtree;

public class AttributeNotDefinedException extends Exception {
  private static final long serialVersionUID = 1L;
  private AttributeType attrType;
  public AttributeNotDefinedException(AttributeType attrType){
    this.attrType = attrType;
  }
  @Override
  public String getMessage(){
    return "attribute '" + attrType.getName() + "' not set in tuple";
    // TODO(tyson): something more helpful
  }
}
