package org.whdl.frontend.syntaxtree;

public class BitTypeValue extends TypeValue {

  private final static BitTypeValue instance = new BitTypeValue();

  public static BitTypeValue getInstance() {
    return instance;
  }

  private BitTypeValue() {}

  @Override
  public void verify() {}

  @Override
  public String toString() {
    return "whdl.Bit";
  }
}