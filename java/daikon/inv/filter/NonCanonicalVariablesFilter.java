package daikon.inv.filter;

import daikon.inv.*;

public class NonCanonicalVariablesFilter extends InvariantFilter {
  public String getDescription() {
    return "Suppress invariants containing non-canonical variables [deprecated]";
  }

  //  We should discard this invariant only if it has non-canonical
  //  variables AND it is not an equality Comparison invariant.  We
  //  need to keep equality Comparison invariants so that later on,
  //  Equality invariants will be made out of them.
  boolean shouldDiscardInvariant( Invariant invariant ) {
    return false;
  }
}
