package org.whdl.frontend.syntaxtree;

import static org.junit.Assert.*;

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
	
	@Test
	public void testEnumEquality() throws TypeMismatchException, NoSuchEnumIdentifierException {
		HashMap<String, Value> m = new HashMap<String, Value>();
		m.put("foo", BitValue.getInstance(true));
		EnumTypeValue type = new EnumTypeValue(BitTypeValue.getInstance(), m);
		EnumValue foo1 = new EnumValue(type, "foo");
		EnumValue foo2 = new EnumValue(type, "foo");
		
		assertEquals(foo1, foo2);
		assertEquals(foo1, BitValue.getInstance(true));
		assertNotEquals(foo1, BitValue.getInstance(false));
	}
}
