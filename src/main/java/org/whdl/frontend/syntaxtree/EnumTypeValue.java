package org.whdl.frontend.syntaxtree;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EnumTypeValue extends TypeValue {
	
	private TypeValue enumsType;
	private Map<String, Value> enums;
	
	public EnumTypeValue(TypeValue typeOfEnums, Map<String, Value> enums) throws TypeMismatchException {
		this.enumsType = typeOfEnums;
		this.enums = new HashMap<String, Value>();
		
		for(Map.Entry<String, Value> e : enums.entrySet()) {
			this.enums.put(e.getKey(), e.getValue());
		}
	}
	
	public boolean contains(String key) {
		return enums.containsKey(key);
	}
	
	public Value get(String key) {
		return enums.get(key);
	}
	
	public TypeValue getTypeOfEnums() {
		return enumsType;
	}
	
	public Set<String> names() {
		return enums.keySet();
	}
	
	@Override
	public void verify() throws Exception{
		enumsType.verify();
		for(Value v : enums.values()) {
			if(!enumsType.equals(v.getType())) {
				throw new TypeMismatchException(enumsType, v.getType());
			}
			v.verify();
		}
	}

}
