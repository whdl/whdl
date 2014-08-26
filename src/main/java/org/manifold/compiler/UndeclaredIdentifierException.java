package org.manifold.compiler;

import org.manifold.compiler.middle.SchematicException;

public class UndeclaredIdentifierException extends SchematicException {
  private static final long serialVersionUID = -5785755001929744865L;
  public String identifier;
  public String getIdentifier() {
    return identifier;
  }
  
  public String context;
  public String getContext() {
    return context;
  }
  
  public UndeclaredIdentifierException(String identifier){
    this.identifier = identifier;
    this.context = "";
  }
  
  public UndeclaredIdentifierException(String identifier, String context) {
    this.identifier = identifier;
    this.context = context;
  }
  
  @Override
  public String getMessage(){
    return "undeclared identifier '" + this.identifier + "' " + this.context;
  }
}
