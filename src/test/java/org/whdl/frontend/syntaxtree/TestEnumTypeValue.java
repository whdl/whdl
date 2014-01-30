package org.whdl.frontend.syntaxtree;

import java.util.HashMap;

import org.junit.Test;

public class TestEnumTypeValue {

	@Test(expected=TypeMismatchException.class)
	public void testIncorrectType() throws Exception {
		HashMap<String, Value> m = new HashMap<String, Value>();
		m.put("foo", BitValue.getInstance(true));
		new EnumTypeValue(TypeTypeValue.getInstance(), m).verify();;
	}
	
	@Test(expected=NoSuchEnumIdentifierException.class)
	public void testIncorrectIdentifier() throws Exception {
		HashMap<String, Value> m = new HashMap<String, Value>();
		m.put("foo", BitValue.getInstance(true));
		EnumTypeValue enumType = new EnumTypeValue(BitTypeValue.getInstance(), m);
		enumType.verify();
		EnumValue enumValue = new EnumValue(enumType, "bar");
	}
	
	
}
