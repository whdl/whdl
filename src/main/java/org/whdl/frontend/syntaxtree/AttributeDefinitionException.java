package org.whdl.frontend.syntaxtree;

import java.util.Collection;

public class AttributeDefinitionException extends Exception {

  private static final long serialVersionUID = 1L;
  private String errorMsg;

  public AttributeDefinitionException(Collection<TupleAttributeType> fields, String name) {
    this.errorMsg = "attribute with name '" + name + "' defined twice in " + fields.toString();
  }

  public String toString() {
    return this.errorMsg;
  }
}
