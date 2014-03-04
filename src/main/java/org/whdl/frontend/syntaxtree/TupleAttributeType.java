package org.whdl.frontend.syntaxtree;

/**
 * Contains a combination of a name/position and a TypeValue.
 * May have a default Value, with type equal to the given TypeValue.
 * Tuples are composed of zero or more AttributeTypes.
 * An AttributeType is not a TypeValue or a Value.
 */
public class TupleAttributeType {
  private final TypeValue type;
  private final Value defaultValue;
  private final String name;
  private final boolean positional;

  // final String destructuringName; // not sure if this is part of a tuple
  private TupleAttributeType(TypeValue type, String name, boolean positional) {
    this.type = type;
    this.defaultValue = null;
    this.name = name;
    this.positional = positional;
  }

  private TupleAttributeType(TypeValue type, String name, boolean positional, Value defaultValue) throws TypeMismatchException {
    this.type = type;
    this.defaultValue = defaultValue;
    this.name = name;
    this.positional = positional;
    if (defaultValue != null) {
      TypeValue actualType = defaultValue.getType();
      if (!actualType.isSubtypeOf(type)) {
        throw new TypeMismatchException(type, actualType);
      }
    }
  }

  public TupleAttributeType(TypeValue type, String name, Value defaultValue) throws TypeMismatchException {
    this(type, name, false, defaultValue);
  }

  public TupleAttributeType(TypeValue type, String name) {
    this(type, name, false);
  }

  public TupleAttributeType(TypeValue type, int pos, Value defaultValue) throws TypeMismatchException{
    this(type, Integer.toString(pos), true, defaultValue);
  }

  public TupleAttributeType(TypeValue type, int pos) {
    this(type, Integer.toString(pos), true);
  }

  public Value getDefaultValue() {
    return defaultValue;
  }

  /**
   * @return the type of the value accessed here
   */
  public TypeValue getValueType() {
    return type;
  }


  /**
   * @return the name this attribute has
   */
  public String getName() {
    return name;
  }

  public boolean isPositional() {
    return positional;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TupleAttributeType other = (TupleAttributeType) obj;
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
    return getValueType().toString() + " " + getName();
  }
}