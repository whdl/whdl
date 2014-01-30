package org.whdl.frontend.syntaxtree;

public class AttributeDefinitionException extends Exception {

  private static final long serialVersionUID = 1L;
  private String errorMsg;

  public AttributeDefinitionException(SpecifiedTupleTypeValue tupleType, String name) {
    this.errorMsg = "attribute with name '" + name + "' defined twice in " + tupleType.toString();
  }
  
  public AttributeDefinitionException(SpecifiedTupleTypeValue tupleType, int pos) {
    this.errorMsg = "missing attribute in position " + pos + " of " + tupleType.toString();
  }
  
  public String toString() {
    return this.errorMsg;
  }
}
