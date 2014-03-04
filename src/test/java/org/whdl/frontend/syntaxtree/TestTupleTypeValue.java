package org.whdl.frontend.syntaxtree;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TestTupleTypeValue {
  private static final BitValue FALSE = BitValue.getInstance(false);
  private static final TypeValue BIT = BitTypeValue.getInstance();
  public SpecifiedTupleTypeValue makeTupleTypeValue(String ... args) throws AttributeDefinitionException {
    Collection<TupleAttributeType> types = new ArrayList<TupleAttributeType>();
    for(String a: args) {
      types.add(new TupleAttributeType(BIT, a));
    }
    return new SpecifiedTupleTypeValue(types);
  }

  public SpecifiedTupleTypeValue makeTupleTypeValue(int ... args) throws AttributeDefinitionException {
    Collection<TupleAttributeType> types = new ArrayList<TupleAttributeType>();
    for(int a: args) {
      types.add(new TupleAttributeType(BIT, a));
    }
    return new SpecifiedTupleTypeValue(types);
  }

  public SpecifiedTupleTypeValue makeTupleTypeValue(TupleAttributeType ... args) throws AttributeDefinitionException {
    return new SpecifiedTupleTypeValue(Arrays.asList(args));
  }

  public TupleAttributeType makeAttrType(String name, BitValue defaultValue) throws TypeMismatchException {
    return new TupleAttributeType(BitTypeValue.getInstance(), name, defaultValue);
  }

  public TupleAttributeType makeAttrType(int pos, BitValue defaultValue) throws TypeMismatchException {
    return new TupleAttributeType(BitTypeValue.getInstance(), pos, defaultValue);
  }

  @Test
  public void testAttributeEquality() throws AttributeDefinitionException{
    SpecifiedTupleTypeValue ab = makeTupleTypeValue("a", "b");
    SpecifiedTupleTypeValue ab2 = makeTupleTypeValue("a", "b");
    assertTrue(ab.equals(ab2));
    assertTrue(ab.isSubtypeOf(ab2));
    assertTrue(ab.canCastTo(ab2));
  }

  @Test
  public void testAttributeFunctions() throws Exception {
    SpecifiedTupleTypeValue a01 = makeTupleTypeValue(makeAttrType(0, null),
                                                    makeAttrType(1, null),
                                                    makeAttrType("foo", null));
    assertEquals(a01.getLength(), 2);
    assertTrue(a01.getType() == TypeTypeValue.getInstance());
    Set<String> expectedAttrNames = new HashSet<String>(Arrays.asList(new String[]{"0", "1", "foo"}));
    assertEquals(expectedAttrNames, a01.getAttributeNames());
    assertEquals("(whdl.Bit 0, whdl.Bit 1, whdl.Bit foo)", a01.toString());
  }

  @Test
  public void testPositionalAttributeEquality() throws AttributeDefinitionException{
    SpecifiedTupleTypeValue two = makeTupleTypeValue(0, 1);
    SpecifiedTupleTypeValue two2 = makeTupleTypeValue(1, 0);
    assertTrue(two.equals(two2));
    assertTrue(two.isSubtypeOf(two2));
    assertTrue(two.canCastTo(two2));
    assertEquals(two.getLength(), 2);
  }

  @Test
  public void testAttributeInequality() throws AttributeDefinitionException {
    SpecifiedTupleTypeValue a = makeTupleTypeValue("a");
    SpecifiedTupleTypeValue ab = makeTupleTypeValue("a", "b");
    assertTrue(ab.equals(ab));
    assertFalse(ab.equals(a));
    assertFalse(ab.isSubtypeOf(a));
    assertFalse(a.isSubtypeOf(ab));
    assertTrue(ab.canCastTo(a));
    assertFalse(a.canCastTo(ab));
  }

  @Test
  public void testVerifySuccess() throws Exception {
    SpecifiedTupleTypeValue a = makeTupleTypeValue("a");
    SpecifiedTupleTypeValue ab = makeTupleTypeValue("a", "b");
    a.verify();
    ab.verify();
  }

  // check that error is thrown in constructor for multiple field name definition
  @Test(expected = AttributeDefinitionException.class)
  public void testDeclareTwice() throws Exception {
    SpecifiedTupleTypeValue aa = makeTupleTypeValue("a", "a");
  }

  // check that empty tuple does not cause an exception
  @Test
  public void testDeclareEmpty() throws Exception {
    SpecifiedTupleTypeValue empty = new SpecifiedTupleTypeValue(new ArrayList<TupleAttributeType>());
    empty.verify();
  }

  @Test
  public void testDeclareRange() throws Exception {
    SpecifiedTupleTypeValue vals = makeTupleTypeValue(2, 0, 1);
    vals.verify();
  }

  // Check that tuples can be cast to others with missing fields if they have default values
  @Test
  public void testCanCastDefault() throws Exception {
    SpecifiedTupleTypeValue ab = makeTupleTypeValue(makeAttrType("a", null), makeAttrType("b", FALSE));
    SpecifiedTupleTypeValue a = makeTupleTypeValue(makeAttrType("a", null));
    assertTrue(ab.canCastTo(a));
    assertTrue(a.canCastTo(ab));
    assertTrue(ab.canCastTo(ab));
  }

  @Test
  public void testCastChecksLength() throws Exception {
    SpecifiedTupleTypeValue two = makeTupleTypeValue(0, 1);
    SpecifiedTupleTypeValue one = makeTupleTypeValue(0);
    assertFalse("can't cast without default", one.canCastTo(two));
    assertFalse("can't cast with extra arguments", two.canCastTo(one));
  }

  @Test
  public void testCastWithDefaults() throws Exception {
    SpecifiedTupleTypeValue two = makeTupleTypeValue(makeAttrType(0, null), makeAttrType(1, FALSE));
    SpecifiedTupleTypeValue one = makeTupleTypeValue(makeAttrType(0, null));
    assertTrue("can cast with default values", one.canCastTo(two));
    assertFalse("can't cast with extra arguments", two.canCastTo(one));
  }

  @Test
  public void testHierarchy() throws AttributeDefinitionException {
    SpecifiedTupleTypeValue a = makeTupleTypeValue("a");
    assertTrue(a.canCastTo(TypeTypeValue.getInstance()));
    assertTrue(a.isSubtypeOf(TypeTypeValue.getInstance()));
    assertFalse(a.canCastTo(BIT));
    assertFalse(a.isSubtypeOf(BIT));
    assertFalse(a.equals(BIT));
  }
}
