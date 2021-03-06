package org.manifold.compiler;

public abstract class TypeValue extends Value {

 /*
  * Java doesn't let us express this but every `TypeValue`, by convention,
  * ought to have a `public abstract final getInstance()` singleton accessor
  * method.
  *
  *   public abstract final TypeValue getInstance();
  *
  * and a private constructor
  *
  *   private abstract TypeValue();
  */
  
  public TypeValue() {
    super(null);
  }

  @Override
  public TypeValue getType() {
    return TypeTypeValue.getInstance();
  }

  public TypeValue getSupertype() {
    return TypeTypeValue.getInstance();
  }

  public boolean isSubtypeOf(TypeValue type) {
    if (this == type) {
      return true;
    } else {
      // TypeTypeValue overrides this method for base case behaviour.
      return getSupertype().isSubtypeOf(type);
    }
  }

  @Override
  public boolean isElaborationtimeKnowable() {
    return true;
  }

  @Override
  public boolean isRuntimeKnowable() {
    return false;
  }
}
