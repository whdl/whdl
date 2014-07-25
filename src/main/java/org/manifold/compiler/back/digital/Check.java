package org.manifold.compiler.back.digital;

import org.manifold.compiler.UndefinedBehaviourError;

public abstract class Check {
  protected abstract void verify();

  protected Boolean result = null;

  public boolean run() {
    if (result == null) {
      verify();
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
