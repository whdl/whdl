package org.manifold.compiler.back.digital;

import org.manifold.compiler.UndefinedBehaviourError;

public abstract class DesignRuleCheck {
  public abstract void check();

  protected Boolean result = null;

  public boolean passed() {
    if (result == null) {
      check();
      // ensure that the check set a result
      if (result == null) {
        throw new UndefinedBehaviourError(
            "Design rule check run produced no result");
      }
    }
    return result;
  }
  // TODO(murphy) get list of failures
}
