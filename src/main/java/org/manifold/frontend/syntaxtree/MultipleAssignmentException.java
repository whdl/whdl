package org.manifold.frontend.syntaxtree;

public class MultipleAssignmentException extends Exception {
  private static final long serialVersionUID = 1L;

  private Variable variable;
  
  public MultipleAssignmentException(Variable var) {
    this.variable = var;
  }

  @Override
  public String getMessage() {
    return "multiple assignment to variable '" + variable.getIdentifier() + "'";
  }
}