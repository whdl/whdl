package org.manifold.compiler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.HashMap;
import org.manifold.compiler.back.SchematicException;

public class TestValue {

  @Test
  public void testRetrieveType() throws SchematicException {
    NodeType nDef = new NodeType(new HashMap<>(), new HashMap<>());
    Value dom = new Node(nDef, new HashMap<>(), new HashMap<>());
    Type expected = nDef;
    Type actual = dom.getType();
    assertEquals(expected, actual);
  }

}
