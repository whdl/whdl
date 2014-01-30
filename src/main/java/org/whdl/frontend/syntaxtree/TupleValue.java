package org.whdl.frontend.syntaxtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A tuple value. This may contain named and positional attributes.
 * 
 * TODO(tyson): figure out what UnspecifiedTupleTypeValue would be needed for,
 * how it would be handled.
 */
public class TupleValue extends Value implements Iterable<Attribute> {
  // TODO: make an array of Attributes for positional parameters?
	private Map<String, Attribute> attributes = new HashMap<String, Attribute>();
	private SpecifiedTupleTypeValue typeValue; // may be reassigned
	
	/**
	 * Construct a TupleValue by casting another tuple to it.
	 * @param expression the tuple that is being cast
	 * @param typeValue the existing type
	 */
	public TupleValue(TupleValue original, SpecifiedTupleTypeValue typeValue) {
		this.typeValue = typeValue;
		for(AttributeType a: typeValue) {
		  String name = a.getName();

      Value v = null;
		  if(original.hasAttribute(name)) {
        try {
          v = original.getAttribute(name).getValue();
        } catch (AttributeAccessException e) {
          // This should be impossible
          e.printStackTrace();
        }
		  } else {
		    v = a.getDefaultValue();
		  }
		  attributes.put(name, new Attribute(a, v));
		}
		// TODO: verify the counts of numeric attributes
	}
	
	/**
	 * Construct a TupleValue from a list of attributes
	 * @param attrs a list of attributes
	 */
	public TupleValue(Collection<Attribute> attrs) {
	  Collection<AttributeType> attrTypes = new ArrayList<AttributeType>();
	  for(Attribute a: attrs) {
      attrTypes.add(a.getType());
	  }
	  typeValue = new SpecifiedTupleTypeValue(attrTypes);
		for(Attribute a: attrs) {
		  AttributeType type = a.getType();
		  this.attributes.put(type.getName(), a);
		}
	}
	
	@Override
	public TypeValue getType() {
		return typeValue;
	}

	public boolean hasAttribute(String name) {
	  return attributes.containsKey(name);
	}

	public boolean hasAttribute(int pos) {
	  return attributes.containsKey(pos);
	}
	
	public Attribute getAttribute(String name) throws AttributeAccessException {
		Attribute a = attributes.get(name);
		if(a == null) { // a is never null, but a.value may be null
		  throw new AttributeAccessException(name, this);
		}
		return a;
	}
	
	public Attribute getAttribute(int i) throws AttributeAccessException {
		return getAttribute(Integer.toString(i));
	}

	@Override
	public void verify() throws Exception {
	  typeValue.verify();
		for (Attribute attr: attributes.values()) {
		  attr.verify();
		}
	}

	@Override
	public boolean isCompiletimeEvaluable() {
		for (Attribute attr: attributes.values()) {
			if (!attr.getValue().isCompiletimeEvaluable()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * A tuple can be synthesized if each attributes is synthesizable.
	 * TODO: how does this work with Value requiring isSynthesizable() || isCompiletimeEvaluable()
	 */
	@Override
	public boolean isSynthesizable() {
		for (Attribute attr: attributes.values()) {
			if (!attr.getValue().isSynthesizable()) {
				return false;
			}
		}
		return true;
	}
	
	public Set<String> getAttributeNames() {
	  return attributes.keySet();
	  
	}
	public Iterator<Attribute> iterator() {
	  return attributes.values().iterator();
	}
}
