package org.whdl.frontend.syntaxtree;


public class Attribute {
  private final AttributeType type;
  private final Value value;
  
  public Attribute(AttributeType type, Value value) {
    this.type = type;
    this.value = value != null ? value : type.getDefaultValue();
  }
  
  public Value getValue() {
    return value;
  }
  
  public AttributeType getType() {
    return this.type;
  }
  
  public void verify() throws Exception {
    if(value == null) {
      throw new AttributeNotDefinedException(type);
    }
    value.verify();

    TypeValue expectedType = type.getType();
    TypeValue actualType = value.getType();
    if(!actualType.isSubtypeOf(expectedType)) {
      throw new TypeMismatchException(expectedType, actualType);
    }
  }
  
  @Override
  public String toString() {
    return type.toString() + "=" + value;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Attribute)) {
      return false;
    }
    Attribute other = (Attribute) obj;
    if (!type.equals(other.type)) {
      return false;
    }
    if (value == null) {
      if (other.value != null) {
        return false;
      }
    } else if (!value.equals(other.value)) {
      return false;
    }
    return true;
  }
}