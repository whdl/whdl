package org.whdl.frontend.syntaxtree;

public abstract class Expression {
	public abstract Value eval();
	public abstract TypeValue getType();
}