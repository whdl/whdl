package org.whdl.frontend.syntaxtree;

public class AttributeAccessException extends Exception {
  private static final long serialVersionUID = 1L;
  private String name;
  private TupleValue tuple;
  public AttributeAccessException(String name, TupleValue tuple){
    this.name = name;
    this.tuple = tuple;
  }
  @Override
  public String getMessage(){
    return "attribute '" + name + "' not part of tuple" + tuple.toString();
  }
}
