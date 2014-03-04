package org.whdl.frontend.syntaxtree;

import static org.junit.Assert.*;
import org.junit.Test;

public class TestTupleAttributeType {
  public TupleAttributeType makeAttributeType(int pos) {
    return new TupleAttributeType(BitTypeValue.getInstance(), pos);
  }

  public TupleAttributeType makeAttribute(String name) {
    return new TupleAttributeType(BitTypeValue.getInstance(), name);
  }
  @Test
  public void testNumericAttributeType() throws TypeMismatchException {
    TupleAttributeType t = new TupleAttributeType(BitTypeValue.getInstance(), 12);
    assertEquals("12", t.getName());
    assertTrue(t.isPositional());
    assertEquals(null, t.getDefaultValue());
  }

  @Test
  public void testStringAttributeType() throws TypeMismatchException {
    TupleAttributeType t = new TupleAttributeType(BitTypeValue.getInstance(), "foo");
    assertEquals("foo", t.getName());
    assertFalse(t.isPositional());
    assertEquals(BitTypeValue.getInstance(), t.getValueType());
    assertEquals(null, t.getDefaultValue());
  }

  @Test
  public void testAttributeEquality() {
    assertEquals(makeAttributeType(1), makeAttributeType(1));
    assertNotEquals(makeAttributeType(1), makeAttributeType(0));
    assertEquals(makeAttribute("foo"), makeAttribute("foo"));
    assertNotEquals(makeAttribute("foo"), makeAttribute("foz"));
    assertNotEquals(makeAttribute("a"), makeAttributeType(0));
  }

  @Test
  public void testDefault() throws TypeMismatchException {
    TupleAttributeType defaultTrue = new TupleAttributeType(BitTypeValue.getInstance(), 12, BitValue.getInstance(true));
    assertEquals(BitValue.getInstance(true), defaultTrue.getDefaultValue());
  }

  @Test(expected=TypeMismatchException.class)
  public void testDefaultInvalid() throws TypeMismatchException {
    new TupleAttributeType(BitTypeValue.getInstance(), 12, TypeTypeValue.getInstance());
  }
}
