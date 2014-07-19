package org.manifold.compiler.back;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.manifold.compiler.ConnectionValue;
import org.manifold.compiler.NodeValue;
import org.manifold.compiler.back.digital.Net;
import org.manifold.compiler.back.digital.Netlist;
import org.manifold.compiler.middle.Schematic;
import org.manifold.compiler.middle.SchematicException;

public class TestNetlist {

  @BeforeClass
  public static void setupClass() {
    UtilSchematicConstruction.setupIntermediateTypes();
  }

  @Test
  public void testConstruction() throws SchematicException {
    // [digitalIn] -> [digitalOut]
    Schematic sch = UtilSchematicConstruction.instantiateSchematic("case0");
    NodeValue in = UtilSchematicConstruction.instantiateInputPin();
    NodeValue out = UtilSchematicConstruction.instantiateOutputPin();
    ConnectionValue in_to_out = UtilSchematicConstruction.instantiateWire(
        in.getPort("out"), out.getPort("in"));
    sch.addNode("in", in);
    sch.addNode("out", out);
    sch.addConnection("in_to_out", in_to_out);

    Netlist netlist = new Netlist(sch);
  }

  @Test
  public void testGetNets() throws SchematicException {
    // [digitalIn] -> [digitalOut]
    Schematic sch = UtilSchematicConstruction.instantiateSchematic("case0");
    NodeValue in = UtilSchematicConstruction.instantiateInputPin();
    NodeValue out = UtilSchematicConstruction.instantiateOutputPin();
    ConnectionValue in_to_out = UtilSchematicConstruction.instantiateWire(
        in.getPort("out"), out.getPort("in"));
    sch.addNode("in", in);
    sch.addNode("out", out);
    sch.addConnection("in_to_out", in_to_out);

    Netlist netlist = new Netlist(sch);

    Map<String, Net> nets = netlist.getNets();
    // there should be exactly one net
    assertEquals(1, nets.values().size());
    // and this net should contain both ports
    Net n_in_to_out = (Net) nets.values().toArray()[0];
    assertTrue(n_in_to_out.getConnectedPorts().contains(in.getPort("out")));
    assertTrue(n_in_to_out.getConnectedPorts().contains(out.getPort("in")));
  }

  @Test
  public void testGetConnectedNet() throws SchematicException {
    // [digitalIn] -> [digitalOut]
    Schematic sch = UtilSchematicConstruction.instantiateSchematic("case0");
    NodeValue in = UtilSchematicConstruction.instantiateInputPin();
    NodeValue out = UtilSchematicConstruction.instantiateOutputPin();
    ConnectionValue in_to_out = UtilSchematicConstruction.instantiateWire(
        in.getPort("out"), out.getPort("in"));
    sch.addNode("in", in);
    sch.addNode("out", out);
    sch.addConnection("in_to_out", in_to_out);

    Netlist netlist = new Netlist(sch);

    // `in` and `out` should both be connected to the same net
    Net n_in = netlist.getConnectedNet(in.getPort("out"));
    assertNotNull(n_in);
    Net n_out = netlist.getConnectedNet(out.getPort("in"));
    assertNotNull(n_out);
    assertEquals(n_in, n_out);
  }

}
