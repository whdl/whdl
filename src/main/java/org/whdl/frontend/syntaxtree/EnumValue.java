package org.whdl.frontend.syntaxtree;

public class EnumValue extends Value {

	private EnumTypeValue enumType;
	private String enumIdentifier;
	private Value enumValue;
	
	public EnumValue(EnumTypeValue enumType, String enumIdentifier) throws NoSuchEnumIdentifierException {
		this.enumType = enumType;
		this.enumIdentifier = enumIdentifier;
		
		if(!enumType.contains(enumIdentifier)) {
			throw new NoSuchEnumIdentifierException(this.enumType, this.enumIdentifier);
		}
		
		enumValue = enumType.get(enumIdentifier);
	}
	
	public String getIdentifier() {
		return enumIdentifier;
	}
	
	public Value getValue() {
		return enumValue;
	}
	
	@Override
	public TypeValue getType() {
		return enumType;
	}

	@Override
	public void verify() throws Exception {
		enumValue.verify();
	}

	@Override
	public boolean isCompiletimeEvaluable() {
		return enumValue.isCompiletimeEvaluable();
	}

	@Override
	public boolean isSynthesizable() {
		return enumValue.isSynthesizable();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof EnumValue) {
			return enumIdentifier.equals(((EnumValue) obj).enumIdentifier);
		}
		
		return getValue().equals(obj);
	}
}
