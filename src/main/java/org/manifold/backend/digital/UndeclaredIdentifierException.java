package org.manifold.backend.digital;

public class UndeclaredIdentifierException 
  extends NetlistConstructionException {

  private static final long serialVersionUID = 1596430164652515640L;

  public UndeclaredIdentifierException(String identifier) {
    super("undeclared identifier '" + identifier + "'");
  }

}
