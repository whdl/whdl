package org.whdl.frontend.syntaxtree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class SpecifiedTupleTypeValue extends TupleTypeValue implements Iterable<AttributeType> {
	private Map<String, AttributeType> attributeTypes = new HashMap<String, AttributeType>();
	private AttributeType duplicatedAttribute = null;
	private int length = 0;
	// TODO(tyson): get this done....

	public SpecifiedTupleTypeValue(Collection<AttributeType> attrTypes) {
	  for(AttributeType a : attrTypes) {
	    // TODO(tyson) check for duplicates in verification
	    AttributeType old = attributeTypes.put(a.getName(), a);
	    if(old != null) {
	      duplicatedAttribute = old;
	    }
	  }
	  for(AttributeType a : attributeTypes.values()) {
	    if(a.isPositional()) {
	      length++;
	    }
	  }
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

  @Override
  public void verify() throws Exception {
    if(duplicatedAttribute != null) {
      throw new AttributeDefinitionException(this, duplicatedAttribute.getName());
    } else if (attributeTypes.isEmpty()) {
      throw new AttributeDefinitionException(this, null);
    }
    
    int totalPositional = 0;
    for(AttributeType a : attributeTypes.values()) {
      if(a.isPositional()) totalPositional++;
    }
    for(int i = 0; i < totalPositional; i++) {
      if(!attributeTypes.containsKey(Integer.toString(i))) {
        throw new AttributeDefinitionException(this, i);
      }
    }
  }
  
  public Set<String> getAttributeNames() {
    return attributeTypes.keySet();
  }

  @Override
  public Iterator<AttributeType> iterator() {
    return attributeTypes.values().iterator();
  }
}