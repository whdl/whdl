package org.manifold.compiler.back;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.manifold.compiler.ConnectionValue;
import org.manifold.compiler.MultipleDefinitionException;
import org.manifold.compiler.NodeValue;
import org.manifold.compiler.back.digital.CodeGenerationError;
import org.manifold.compiler.back.digital.VHDLCodeGenerator;
import org.manifold.compiler.middle.Schematic;
import org.manifold.compiler.middle.SchematicException;

public class TestVHDLCodeGenerator {

  @BeforeClass
  public static void setupClass() {
    LogManager.getRootLogger().setLevel(Level.ALL);
    PatternLayout layout = new PatternLayout(
        "%d{ISO8601} [%t] %-5p %c %x - %m%n");
    LogManager.getRootLogger().addAppender(
        new ConsoleAppender(layout, ConsoleAppender.SYSTEM_ERR));
  }

  /*
   * This test class does not provide verification or "correctness" testing for
   * output products of the code generator. Its real function is to perform very
   * basic unit testing to catch simple errors.
   */

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void testOutputProductPresent() throws SchematicException {
    // Connect an input pin straight across to an output pin.
    Schematic schematic = UtilSchematicConstruction
        .instantiateSchematic("test");
    NodeValue in0 = UtilSchematicConstruction.instantiateInputPin();
    schematic.addNode("in0", in0);
    NodeValue out0 = UtilSchematicConstruction.instantiateOutputPin();
    schematic.addNode("out0", out0);
    ConnectionValue in0_to_out0 = UtilSchematicConstruction.instantiateWire(
        in0.getPort("out"), out0.getPort("in"));
    schematic.addConnection("in0_to_out0", in0_to_out0);

    VHDLCodeGenerator codegen = new VHDLCodeGenerator(schematic);
    File tempdir = folder.getRoot();
    String temppath = tempdir.getAbsolutePath();
    codegen.setOutputDirectory(temppath);
    codegen.generateOutputProducts();

    List<Path> outputFiles = new ArrayList<Path>();

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths
        .get(temppath))) {
      for (Path file : stream) {
        outputFiles.add(file);
      }
    } catch (IOException | DirectoryIteratorException x) {
      fail(x.getMessage());
    }

    // check that "test.vhd" was generated
    boolean found = false;
    Path testVHD = null;
    for (Path file : outputFiles) {
      String filename = file.getFileName().toString();
      if (filename.equals("test.vhd")) {
        found = true;
        testVHD = file;
        break;
      }
    }
    assertTrue("output product 'test.vhd' not found", found);
  }

  @Test
  public void testTopLevelIOGeneration() 
      throws SchematicException, IOException {
    // Connect an input pin straight across to an output pin.
    Schematic schematic = UtilSchematicConstruction
        .instantiateSchematic("test");
    NodeValue in0 = UtilSchematicConstruction.instantiateInputPin();
    schematic.addNode("in0", in0);
    NodeValue out0 = UtilSchematicConstruction.instantiateOutputPin();
    schematic.addNode("out0", out0);
    ConnectionValue net0 = UtilSchematicConstruction.instantiateWire(
        in0.getPort("out"), out0.getPort("in"));
    schematic.addConnection("net0", net0);

    VHDLCodeGenerator codegen = new VHDLCodeGenerator(schematic);
    File tempdir = folder.getRoot();
    String temppath = tempdir.getAbsolutePath();
    codegen.setOutputDirectory(temppath);
    codegen.generateOutputProducts();

    // open test.vhd
    String testOutputFilename = temppath + "/test.vhd";
    Path testOutputPath = Paths.get(testOutputFilename);
    List<String> testLines = Files.readAllLines(testOutputPath);
    // we are looking for the following two lines:
    // "in0: in std_logic"
    // "out0: out std_logic"
    // somewhere between a line starting with "entity"
    // and the first following line starting with "end"
    boolean foundEntity = false;
    boolean scanningEntity = false;
    boolean foundInputPort = false;
    boolean foundOutputPort = false;
    
    Pattern beginEntity = Pattern.compile(
        "^\\s*entity\\s+\\\\test\\\\\\s+is");
    Pattern endEntity = Pattern.compile(
        "^\\s*end\\s+entity", Pattern.CASE_INSENSITIVE);
    Pattern inputPort = Pattern.compile(
        "^\\s*in0\\s*:\\s*(?i)in\\s+std_logic");
    Pattern outputPort = Pattern.compile(
        "^\\s*out0\\s*:\\s*(?i)out\\s+std_logic");
    for (String line : testLines) {
      if (scanningEntity) {
        Matcher mEndEntity = endEntity.matcher(line);
        if (mEndEntity.find()) {
          scanningEntity = false;
          break;
        }
        // look for an input port or an output port
        Matcher mInputPort = inputPort.matcher(line);
        if (mInputPort.find()) {
          if (foundInputPort) {
            fail("multiple input port declarations");
          }
          foundInputPort = true;
        }
        Matcher mOutputPort = outputPort.matcher(line);
        if (mOutputPort.find()) {
          if (foundOutputPort) {
            fail("multiple output port declarations");
          }
          foundOutputPort = true;
        }
      } else {
        Matcher mBeginEntity = beginEntity.matcher(line);
        if (mBeginEntity.find()) {
          foundEntity = true;
          scanningEntity = true;
        }
      }
    }
    // collect results
    assertTrue("no entity declaration present in generated code",
        foundEntity);
    assertTrue("no input port declaration found", foundInputPort);
    assertTrue("no output port declaration found", foundOutputPort);
  }
  
  @Test
  public void testRegisterSignalGeneration() 
      throws SchematicException, IOException {
    // Create a schematic that breaks out all I/Os on a register.
    Schematic schematic = UtilSchematicConstruction
        .instantiateSchematic("test");
    NodeValue clock = UtilSchematicConstruction.instantiateInputPin();
    schematic.addNode("clock", clock);
    NodeValue reset = UtilSchematicConstruction.instantiateInputPin();
    schematic.addNode("reset", reset);
    NodeValue in0 = UtilSchematicConstruction.instantiateInputPin();
    schematic.addNode("in0", in0);
    NodeValue out0 = UtilSchematicConstruction.instantiateOutputPin();
    schematic.addNode("out0", out0);
    // the important parameter for this register is the first one:
    // the initial value is HIGH, or '1'.
    NodeValue reg0 = UtilSchematicConstruction.instantiateRegister(
        true, true, false, true);
    schematic.addNode("reg0", reg0);
    ConnectionValue nClock = UtilSchematicConstruction.instantiateWire(
        clock.getPort("out"), reg0.getPort("clock"));
    schematic.addConnection("nClock", nClock);
    ConnectionValue nReset = UtilSchematicConstruction.instantiateWire(
        reset.getPort("out"), reg0.getPort("reset"));
    schematic.addConnection("nReset", nReset);
    ConnectionValue nIn0 = UtilSchematicConstruction.instantiateWire(
        in0.getPort("out"), reg0.getPort("in"));
    schematic.addConnection("nIn0", nIn0);
    ConnectionValue nOut0 = UtilSchematicConstruction.instantiateWire(
        reg0.getPort("out"), out0.getPort("in"));
    schematic.addConnection("nOut0", nOut0);

    VHDLCodeGenerator codegen = new VHDLCodeGenerator(schematic);
    File tempdir = folder.getRoot();
    String temppath = tempdir.getAbsolutePath();
    codegen.setOutputDirectory(temppath);
    codegen.generateOutputProducts();

    // open test.vhd
    String testOutputFilename = temppath + "/test.vhd";
    Path testOutputPath = Paths.get(testOutputFilename);
    List<String> testLines = Files.readAllLines(testOutputPath);
    // the signal corresponding to the register should be named after
    // the net connected to the register's "out" port,
    // needs to be declared somewhere in the architecture declarations
    // (i.e. between "ARCHITECTURE" and "BEGIN"),
    // and needs to have the correct initial value (" := '1'; ").
    Pattern beginArchDecls = Pattern.compile(
        "^\\s*architecture\\s+manifold", Pattern.CASE_INSENSITIVE);
    Pattern endArchDecls = Pattern.compile(
        "^\\s*begin", Pattern.CASE_INSENSITIVE);
    Pattern regDecl = Pattern.compile(
        "^\\s*signal\\s+\\\\\\w*nOut0\\\\\\s*:\\s*std_logic");
    Pattern regInit = Pattern.compile(
        "\\s*:=\\s*'1'\\s*;");
    boolean foundArchDecls = false;
    boolean scanningArchDecls = false;
    boolean foundRegDecl = false;
    String capturedRegDecl = "";
    boolean foundCorrectInitializer = false;
    for (String line : testLines) {
      if (scanningArchDecls) {
        Matcher mEndArchDecls = endArchDecls.matcher(line);
        if (mEndArchDecls.find()) {
          scanningArchDecls = false;
          break;
        }
        Matcher mRegDecl = regDecl.matcher(line);
        if (mRegDecl.find()) {
          if (foundRegDecl) {
            fail("multiple register signal declarations");
          }
          foundRegDecl = true;
          capturedRegDecl = line;
          // check initializer
          Matcher mRegInit = regInit.matcher(line);
          if (mRegInit.find()) {
            foundCorrectInitializer = true;
          }
        }
      } else {
        Matcher mBeginArchDecls = beginArchDecls.matcher(line);
        if (mBeginArchDecls.find()) {
          foundArchDecls = true;
          scanningArchDecls = true;
        }
      }
    }
    // collect results
    assertTrue("no architecture declaration section present"
        +" in generated code", foundArchDecls);
    assertTrue("no register signal declaration found", foundRegDecl);
    assertTrue("register signal declaration has wrong initializer: "
        + capturedRegDecl, foundCorrectInitializer);
  }
  
  @Test
  public void testNonExistentOutputDirectoryThrowsException() 
      throws SchematicException {
    // Establish a simple demo schematic
    Schematic schematic = UtilSchematicConstruction
        .instantiateSchematic("test");
    NodeValue in0 = UtilSchematicConstruction.instantiateInputPin();
    schematic.addNode("in0", in0);
    NodeValue out0 = UtilSchematicConstruction.instantiateOutputPin();
    schematic.addNode("out0", out0);
    ConnectionValue in0_to_out0 = UtilSchematicConstruction.instantiateWire(
        in0.getPort("out"), out0.getPort("in"));
    schematic.addConnection("in0_to_out0", in0_to_out0);

    VHDLCodeGenerator codegen = new VHDLCodeGenerator(schematic);
    String currentDir = Paths.get("").toAbsolutePath().toString();
    String bogusPath = currentDir + "/__bogusDirectory";
    codegen.setOutputDirectory(bogusPath);
    try {
      codegen.generateOutputProducts();
      fail("non-existant output directory was not detected as an error");
    } catch (CodeGenerationError e) {
      assertTrue("wrong error message: '" + e.getMessage() + "'",
          e.getMessage().contains("does not exist"));
    }
  }

}
