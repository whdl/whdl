package org.manifold.compiler.back.digital;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.manifold.compiler.BooleanValue;
import org.manifold.compiler.NodeTypeValue;
import org.manifold.compiler.NodeValue;
import org.manifold.compiler.PortTypeValue;
import org.manifold.compiler.PortValue;
import org.manifold.compiler.TypeMismatchException;
import org.manifold.compiler.UndeclaredAttributeException;
import org.manifold.compiler.UndeclaredIdentifierException;
import org.manifold.compiler.UndefinedBehaviourError;
import org.manifold.compiler.middle.Schematic;

public class VHDLCodeGenerator {
  private static Logger log = LogManager.getLogger("VHDLCodeGenerator");
  private static String newline = System.getProperty("line.separator");

  private Schematic schematic;
  private Netlist netlist = null;

  private String outputDirectory;

  public void setOutputDirectory(String dir) {
    this.outputDirectory = dir;
  }

  // name of VHDL architecture corresponding to generated entities
  private String architecture = "MANIFOLD";

  private PortTypeValue inputPortType = null;
  private PortTypeValue outputPortType = null;
  
  private NodeTypeValue inputPinType = null;
  private NodeTypeValue outputPinType = null;

  private NodeTypeValue registerType = null;

  public VHDLCodeGenerator(Schematic schematic) {
    this.schematic = schematic;
    // by default, output to current working directory
    this.outputDirectory = Paths.get("").toAbsolutePath().toString();
  }

  private void err(String message) {
    throw new CodeGenerationError(message);
  }

  private String escapeIdentifier(String id) {
    // VHDL-93 extended identifiers are delimited by backslashes,
    // and can contain ANY printing character from the VHDL-93 character set.
    // This allows reserved words to be used as identifiers,
    // and makes identifiers case-sensitive.

    if (!id.isEmpty() && id.charAt(0) == '\\'
        && id.charAt(id.length() - 1) == '\\') {
      // already escaped, nothing to do
      return id;
    } else {
      return "\\" + id + "\\";
    }
  }

  public void generateOutputProducts() {
    // check if directory exists
    Path outDir = Paths.get(outputDirectory);
    if (!Files.exists(outDir)) {
      err("output directory '" + outputDirectory + "' does not exist");
    }
    if (!Files.isWritable(outDir)) {
      err("output directory '" + outputDirectory + "' is not writable");
    }
    log.info("Will generate output products in '" + outputDirectory + "'");

    // build netlist from schematic
    try {
      log.info("Building netlist");
      netlist = new Netlist(schematic);
    } catch (UndeclaredIdentifierException | TypeMismatchException e) {
      err(e.getMessage());
    }

    // get information from the schematic about which node types to use

    try {
      inputPortType = schematic.getPortType("digitalIn");
      outputPortType = schematic.getPortType("digitalOut");
      inputPinType = schematic.getNodeType("inputPin");
      outputPinType = schematic.getNodeType("outputPin");
      registerType = schematic.getNodeType("register");
    } catch (UndeclaredIdentifierException e) {
      err(e.getMessage());
    }

    // we don't support multiple output files yet, but we set up
    // the "hierarchical elaboration" method now so that when we do,
    // this will work with minimal effort

    // at each level we need to know this much:
    // * the name of this entity
    // * which nets are I/O pins (i.e. connections from a higher level)
    // * which nodes belong to the current entity
    // * which groups of nodes are components (i.e. elaborated
    // at a lower level)

    String entityName = schematic.getName();
    Set<Net> inputNets = new HashSet<>();
    Set<Net> outputNets = new HashSet<>();
    Map<String, NodeValue> currentNodes = new HashMap<>();
    // our current nodes will be everything in the Schematic
    // that is not a digitalIn or digitalOut; our input nets
    // are nets driven by a digitalIn; and output nets
    // are nets that go to any digitalOut

    // iterate over all nodes
    try {
      for (Entry<String, NodeValue> entry : schematic.getNodes().entrySet()) {
        String nodeName = entry.getKey();
        NodeValue node = entry.getValue();
        if (node.getType().equals(inputPinType)) {
          // this is a top-level input
          log.debug("Identified top-level input " + nodeName);
          PortValue inputPort = node.getPort("out");
          Net inputNet = netlist.getConnectedNet(inputPort);
          inputNets.add(inputNet);
        } else if (node.getType().equals(outputPinType)) {
          // this is a top-level output
          log.debug("Identified top-level output " + nodeName);
          PortValue outputPort = node.getPort("in");
          Net outputNet = netlist.getConnectedNet(outputPort);
          outputNets.add(outputNet);
        } else {
          // this is an internal node
          currentNodes.put(nodeName, node);
        }
      }
    } catch (UndeclaredIdentifierException e) {
      err(e.getMessage());
    }
    // for now, we have no components, so this stays empty
    Map<String, Map<String, NodeValue>> components = new HashMap<>();

    generateEntity(entityName, inputNets, outputNets, currentNodes, components);
    
    log.info("Finished generating top-level entity " + entityName);
  }

  private void generateEntity(String entityName, Set<Net> inputNets,
      Set<Net> outputNets, Map<String, NodeValue> currentNodes,
      Map<String, Map<String, NodeValue>> components) {
    String filename = entityName + ".vhd";
    Path outpath = Paths.get(outputDirectory + File.separator + filename);
    File outfile = new File(outpath.toString());
    log.info("Generating " + filename);
    try (PrintWriter writer = new PrintWriter(outfile, "US-ASCII");) {
      // VHDL preamble
      writer.println("library IEEE;");
      writer.println("use IEEE.std_logic_1164.ALL;");
      writer.println();
      // entity declaration
      writer.print("entity ");
      writer.print(escapeIdentifier(entityName));
      writer.println(" is");
      // I/O ports come from I/O nets
      writer.println(generatePortDeclarations(inputNets, outputNets));
      writer.print("end entity ");
      writer.print(escapeIdentifier(entityName));
      writer.println(";");
      writer.print("architecture ");
      writer.print(architecture);
      writer.print(" of ");
      writer.print(escapeIdentifier(entityName));
      writer.println(" is");
      // component and signal declarations
      // TODO(murphy) components
      // each signal corresponds to a net attached to some node at this level
      Set<Net> signals = new HashSet<>();
      for (NodeValue node : currentNodes.values()) {
        for (PortValue port : node.getPorts().values()) {
          signals.add(netlist.getConnectedNet(port));
        }
      }
      // additionally add nets from top-level inputs
      signals.addAll(inputNets);
      for (Net net : signals) {
        String netName = net.getName();
        log.debug("found net " + netName);
        writer.print("signal ");
        writer.print(escapeIdentifier(netName));
        writer.print(" : std_logic");
        // we need to check whether this is a register, and if so,
        // assign the signal an initial value
        NodeValue node = getDriver(net);
        if (node.getType().equals(registerType)) {
          try {
            boolean initialValue = ((BooleanValue) node
                .getAttribute("initialValue")).toBoolean();
            writer.print(" := ");
            writer.print(booleanToBit(initialValue));
          } catch (UndeclaredAttributeException e) {
            err(e.getMessage());
          }
        }
        writer.println(";");
      }
      writer.println("begin");
      // concurrent statements
      writer.println(generateInputAssignments(inputNets));
      writer.println(generateOutputAssignments(outputNets));
      for (Entry<String, NodeValue> entry : currentNodes.entrySet()) {
        String nodeName = entry.getKey();
        NodeValue node = entry.getValue();
        writer.println(generateNode(nodeName, node));
      }
      writer.print("end ");
      writer.print(architecture);
      writer.println(";");
    } catch (IOException e) {
      err(e.getMessage());
    }
    
    log.info("Finished generating entity " + entityName);
  }

  private String generatePortDeclarations(Set<Net> inputNets,
      Set<Net> outputNets) {
    // TODO this code assumes that the nodes driving I/O ports
    // are inputPin(s)/outputPin(s). make this more general
    // so that any node (i.e. one from a higher-level entity)
    // can be used to instantiate a uniquely-named I/O pin
    StringBuilder decl = new StringBuilder();
    if (inputNets.size() > 0 || outputNets.size() > 0) {
      decl.append("port (");
      decl.append(newline);
      // all ports except the last one need to end with a semicolon,
      // so we make adding this semicolon the responsibility of
      // the NEXT port to be emitted; then the first one
      // we emit doesn't do this
      boolean first = true;
      for (Net inNet : inputNets) {
        if (!first) {
          // terminate previous port
          decl.append(";").append(newline);
        }
        first = false;
        NodeValue node = getDriver(inNet);
        String inputName = schematic.getNodeName(node);
        decl.append(inputName).append(" : in STD_LOGIC");
      }
      for (Net outNet : outputNets) {
        if (!first) {
          // terminate previous port
          decl.append(";").append(newline);
        }
        first = false;
        // look for the outputPin(s) driven by this net
        for (PortValue p : outNet.getConnectedPorts()) {
          NodeValue node = p.getParent();
          if (node.getType().equals(outputPinType)) {
            String outputName = schematic.getNodeName(node);
            decl.append(outputName);
            decl.append(" : out STD_LOGIC");
            // do not break; it is possible that there is more than one
          }
        }
      }
      decl.append(newline);
      decl.append(");");
      decl.append(newline);
    }
    return decl.toString();
  }

  // Generate assignment statements from input ports to net signals.
  private String generateInputAssignments(Set<Net> inputNets) {
    StringBuilder stmts = new StringBuilder();
    for (Net inNet : inputNets) {
      NodeValue node = getDriver(inNet);
      String inputName = schematic.getNodeName(node);
      String netName = escapeIdentifier(inNet.getName());
      log.debug("input '" + inputName + "' maps to net '" + netName + "'");
      stmts.append(netName).append(" <= ").append(inputName).append(";")
          .append(newline);
    }
    return stmts.toString();
  }

  // Generate assignment statements from net signals to output ports.
  private String generateOutputAssignments(Set<Net> outputNets) {
    StringBuilder stmts = new StringBuilder();
    for (Net outNet : outputNets) {
      String netName = escapeIdentifier(outNet.getName());
      for (PortValue p : outNet.getConnectedPorts()) {
        NodeValue node = p.getParent();
        if (node.getType().equals(outputPinType)) {
          String outputName = schematic.getNodeName(node);
          log.debug("net '" + netName + "' maps to output '" 
              + outputName + "'");
          stmts.append(outputName).append(" <= ").append(netName).append(";")
              .append(newline);
        }
      }
    }
    return stmts.toString();
  }

  private String generateNode(String nodeName, NodeValue node) {
    StringBuilder stmts = new StringBuilder();
    if (node.getType().equals(registerType)) {
      /*
       * Registers have a number of attributes that we care about for codegen:
       * initialValue (boolean), resetActiveHigh (boolean), resetAsynchronous
       * (boolean), clockActiveHigh (boolean).
       * 
       * They are connected to the following ports: in (digitalIn), out
       * (digitalOut), clock (digitalIn), reset (digitalIn)
       */
      try {
        // Start by getting the names of all signals connected to the register.
        String sigIn = escapeIdentifier(netlist.getConnectedNet(
            node.getPort("in")).getName());
        String sigOut = escapeIdentifier(netlist.getConnectedNet(
            node.getPort("out")).getName());
        String sigClock = escapeIdentifier(netlist.getConnectedNet(
            node.getPort("clock")).getName());
        String sigReset = escapeIdentifier(netlist.getConnectedNet(
            node.getPort("reset")).getName());

        // Now get the values of all important attributes.
        boolean initialValue = ((BooleanValue) node
            .getAttribute("initialValue")).toBoolean();
        boolean resetActiveHigh = ((BooleanValue) node
            .getAttribute("resetActiveHigh")).toBoolean();
        boolean resetAsynchronous = ((BooleanValue) node
            .getAttribute("resetAsynchronous")).toBoolean();
        boolean clockActiveHigh = ((BooleanValue) node
            .getAttribute("clockActiveHigh")).toBoolean();

        String processName = escapeIdentifier("register_" + nodeName);
        stmts.append(processName);
        stmts.append(": process (");
        // sensitivity list
        stmts.append(sigClock).append(",").append(sigReset).append(",")
            .append(sigIn).append(")").append(newline);
        stmts.append("begin").append(newline);
        // sequential statements
        // the structure is a bit different depending on whether
        // the reset signal is synchronous or asynchronous.
        if (resetAsynchronous) {
          // asynchronous reset
          stmts.append("if (").append(sigReset).append(" = ")
              .append(booleanToBit(resetActiveHigh)).append(") then")
              .append(newline);
          stmts.append(sigOut).append(" <= ")
              .append(booleanToBit(initialValue)).append(";").append(newline);
          // clocked logic
          stmts.append("elsif ");
          if (clockActiveHigh) {
            stmts.append("rising_edge(");
          } else {
            stmts.append("falling_edge(");
          }
          stmts.append(sigClock).append(") then").append(newline);
          stmts.append(sigOut).append(" <= ").append(sigIn).append(";")
              .append(newline);
          stmts.append("end if;").append(newline);
        } else {
          // clocked logic
          stmts.append("if ");
          if (clockActiveHigh) {
            stmts.append("rising_edge(");
          } else {
            stmts.append("falling_edge(");
          }
          stmts.append(sigClock).append(") then").append(newline);
          // synchronous reset
          stmts.append("if (").append(sigReset).append(" = ")
              .append(booleanToBit(resetActiveHigh)).append(") then")
              .append(newline);
          stmts.append(sigOut).append(" <= ")
              .append(booleanToBit(initialValue)).append(";").append(newline);
          stmts.append("else").append(newline);
          stmts.append(sigOut).append(" <= ").append(sigIn).append(";")
              .append(newline);
          stmts.append("end if;").append(newline);
        }
        stmts.append("end if;").append(newline);
        stmts.append("end process ").append(processName).append(";")
            .append(newline);
      } catch (UndeclaredIdentifierException | UndeclaredAttributeException e) {
        err(e.getMessage());
      }
    } else {
      err("could not generate code for node '" + nodeName 
            + "' of unknown type");
    }
    return stmts.toString();
  }

  private String booleanToBit(boolean b) {
    if (b) {
      return "'1'";
    } else {
      return "'0'";
    }
  }

  private NodeValue getDriver(Net net) {
    // PRECONDITION: DRC has verified that exactly one digitalOut is connected
    for (PortValue port : net.getConnectedPorts()) {
      if (port.getType() == outputPortType) {
        return port.getParent();
      }
    }
    throw new UndefinedBehaviourError("undriven net '" + net.getName() + "'");
  }

}
