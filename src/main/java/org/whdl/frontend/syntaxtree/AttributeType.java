package org.whdl.frontend.syntaxtree;


public class AttributeType {
  private final TypeValue type;
  private final Value defaultValue;
  private final String name;
  private final boolean positional;
  
  // final String destructuringName; // not sure if this is part of a tuple
  private AttributeType(TypeValue type, String name, Value defaultValue, boolean positional) {
    this.type = type;
    this.defaultValue = defaultValue;
    this.name = name;
    this.positional = positional;
  }

  public AttributeType(TypeValue type, String name, Value defaultValue) {
    this(type, name, defaultValue, false);
  }
  
  public AttributeType(TypeValue type, String name) {
    this(type, name, null, false);
  }

  public AttributeType(TypeValue type, int pos, Value defaultValue) {
    this(type, Integer.toString(pos), defaultValue, true);
  }

  public AttributeType(TypeValue type, int pos) {
    this(type, Integer.toString(pos), null, true);
  }
  
  public Value getDefaultValue() {
    return defaultValue;
  }
  
  public TypeValue getType() {
    return type;
  }
  
  public String getName() {
    return name;
  }
  
  public boolean isPositional() {
    return positional;
  }
  
  public void verify() throws TypeMismatchException {
    if(defaultValue == null) {
      return;
    }
    TypeValue actualType = defaultValue.getType();
    if(!actualType.isSubtypeOf(type)) {
      throw new TypeMismatchException(type, actualType);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AttributeType other = (AttributeType) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    return true;
  }
  
  @Override
  public String toString() {
    return getType().toString() + " " + getName();
  }
  
}