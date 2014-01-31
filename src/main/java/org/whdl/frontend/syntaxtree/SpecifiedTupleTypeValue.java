package org.whdl.frontend.syntaxtree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SpecifiedTupleTypeValue extends TupleTypeValue implements Iterable<TupleAttributeType> {
	private Map<String, TupleAttributeType> attributeTypes = new HashMap<String, TupleAttributeType>();
	private int length = 0;
	// TODO(tyson): get this done....

	public SpecifiedTupleTypeValue(Collection<TupleAttributeType> attrTypes) throws AttributeDefinitionException {
	  for (TupleAttributeType a : attrTypes) {
	    // TODO(tyson) check for duplicates in verification
	    TupleAttributeType old = attributeTypes.put(a.getName(), a);
	    if (old != null) {
	      throw new AttributeDefinitionException(attrTypes, a.getName());
	    }
	  }
	  for (TupleAttributeType a : attributeTypes.values()) {
	    if (a.isPositional()) {
	      length++;
	    }
	  }
	}

	/**
	 * Returns true if this can be casted or assigned to the TypeValue other.
	 */
	public boolean canCastTo(TypeValue other) {
	  if (other == TypeTypeValue.getInstance()) { // can always cast to TypeTypeValue
	    return true;
	  } else if (!(other instanceof SpecifiedTupleTypeValue)) {
	    // TODO(tyson): check for UnspecifiedTupleTypeValue when it is added to language.
	    return false;
	  }
	  SpecifiedTupleTypeValue o = (SpecifiedTupleTypeValue) other;
    // it is a compile time error to drop positional arguments from source tuple.
	  if(length > o.length) {
	    return false;
	  }

	  // check if all attributes can be assigned to from this value.
    for(TupleAttributeType attr: o) {
      final String name = attr.getName();
      final TupleAttributeType ownAttr = attributeTypes.get(name);
      if(ownAttr == null) {
        if(attr.getDefaultValue() == null) { // check if a field won't be filled
          return false;
        }
      } else { // both have the same attribute name/position
        final TypeValue v = attr.getValueType();

        // check if the field types are compatible
        // TODO(tyson): something more specific if nested tuples are possible
        if(!ownAttr.getValueType().isSubtypeOf(v)) {
          return false;
        }
      }
    }
    return true;
	}

	public boolean isSubtypeOf(TypeValue other) {
	  if (other instanceof SpecifiedTupleTypeValue) {
	    return attributeTypes.equals(((SpecifiedTupleTypeValue)other).attributeTypes);
	  }
	  return other == TypeTypeValue.getInstance();
	  // TODO(tyson) it is a subtype of UnspecifiedTupleTypeValue if that is added
	}

	/**
	 * @return the number of positional arguments
	 */
	public int getLength() {
	  return length;
	}

	@Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof SpecifiedTupleTypeValue) {
      return attributeTypes.equals(((SpecifiedTupleTypeValue)other).attributeTypes);
    }
    return false;
  }

  public Set<String> getAttributeNames() {
    return attributeTypes.keySet();
  }

  @Override
  public Iterator<TupleAttributeType> iterator() {
    return attributeTypes.values().iterator();
  }

  // verification is done in constructor
  @Override
  public void verify() {}

  // print a nice representation for debugging / compiler output.
  // This prints positional arguments >= 10 in the wrong order.
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("(");
    boolean first = true;
    for(String name: new TreeSet<String>(getAttributeNames())) {
      if(!first) {
        sb.append(", ");
      }
      first = false;
      sb.append(attributeTypes.get(name));
    }
    sb.append(")");
    return sb.toString();
  }
}