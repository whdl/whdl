package org.manifold.compiler.back;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

  private List<String> schematicToVHDL(Schematic schematic) throws IOException{
    VHDLCodeGenerator codegen = new VHDLCodeGenerator(schematic);
    File tempdir = folder.getRoot();
    String temppath = tempdir.getAbsolutePath();
    codegen.setOutputDirectory(temppath);
    codegen.generateOutputProducts();

    // open generated code
    String testOutputFilename = temppath + "/" + schematic.getName() + ".vhd";
    Path testOutputPath = Paths.get(testOutputFilename);
    List<String> lines = Files.readAllLines(testOutputPath);
    return lines;
  }
  
  // Find all lines between, but not including, an occurrence of "begin"
  // and an occurrence of "end" (surrounded by whitespace).
  private List<String> findBlock(List<String> lines, String begin, String end){
    List<String> block = new ArrayList<String>();
    
    Pattern beginBlock = Pattern.compile(
        "\\s*" + begin + "\\s*", Pattern.CASE_INSENSITIVE);
    Pattern endBlock = Pattern.compile(
        "\\s*" + begin + "\\s*", Pattern.CASE_INSENSITIVE);
    
    boolean scanningBlock = false;
    for(String line : lines){
      if(scanningBlock){
        Matcher mEnd = endBlock.matcher(line);
        if(mEnd.find()){
          break;
        }else{
          block.add(line);
        }
      }else{
        Matcher mBegin = beginBlock.matcher(line);
        if(mBegin.find()){
          scanningBlock = true;
        }
      }
    }
    return block;
  }
  
  private int countMatches(List<String> block, String pattern){
    Pattern p = Pattern.compile(pattern);
    int matchCount = 0;
    for(String target : block){
      Matcher mTarget = p.matcher(target);
      if(mTarget.find()){
        ++matchCount;
      }
    }
    return matchCount;
  }
  
  @Test
  public void testOutputProductPresent() throws SchematicException {
    // Connect an input pin straight across to an output pin.
    Schematic schematic = UtilSchematicConstruction
        .instantiateSchematic("test");
    NodeValue in0 = UtilSchematicConstruction.instantiateInputPin();
    schematic.addNode("in0", in0);
    NodeValue out0 = UtilSchematicConstruction.instantiateOutputPin();
    schematic.addNode("out0", out0);
    ConnectionValue in0ToOut0 = UtilSchematicConstruction.instantiateWire(
        in0.getPort("out"), out0.getPort("in"));
    schematic.addConnection("in0_to_out0", in0ToOut0);
    
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

    List<String> testLines = schematicToVHDL(schematic);
    
    // we are looking for the following two lines:
    // "in0: in std_logic"
    // "out0: out std_logic"
    // somewhere between a line starting with "entity"
    // and the first following line starting with "end"
    
    List<String> entityBlock = findBlock(testLines, "entity\\s+\\\\test\\\\\\s",
        "end\\s+entity");

    int inputPorts = countMatches(entityBlock, 
        "^\\s*in0\\s*:\\s*(?i)in\\s+std_logic");
    int outputPorts = countMatches(entityBlock,
        "^\\s*out0\\s*:\\s*(?i)out\\s+std_logic");
    
    // collect results
    assertFalse("no entity declaration present in generated code",
        entityBlock.isEmpty());
    assertEquals("expect exactly one input port declaration", 
        1, inputPorts);
    assertEquals("expect exactly one output port declaration", 
        1, outputPorts);
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

    List<String> testLines = schematicToVHDL(schematic);
    
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
        + " in generated code", foundArchDecls);
    assertTrue("no register signal declaration found", foundRegDecl);
    assertTrue("register signal declaration has wrong initializer: "
        + capturedRegDecl, foundCorrectInitializer);
  }
  
  // TODO(murphy) tests for register process logic
  // pay attention to which combinations of register parameters to check

  @Test
  public void testRegisterProcessGenerationAsynchronousReset() 
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
        true, true, true, true);
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

    List<String> testLines = schematicToVHDL(schematic);
    
    // For an asynchronous reset, we need to see the conditional for the
    // reset signal before the conditional for the clock edge
    // (which is rising edge here)
    Pattern clockStmt = Pattern.compile("if.*rising_edge",
        Pattern.CASE_INSENSITIVE);
    Pattern resetStmt = Pattern.compile("if.*nReset",
        Pattern.CASE_INSENSITIVE);
    Pattern processBegin = Pattern.compile(":\\s*process",
        Pattern.CASE_INSENSITIVE);
    Pattern processEnd = Pattern.compile("end\\s*process",
        Pattern.CASE_INSENSITIVE);
    boolean foundProcess = false;
    boolean scanningProcess = false;
    int lineNumber = 0;
    // line numbers on which we have found statements
    int lineClockStmt = 0;
    int lineResetStmt = 0;
    for (String line : testLines) {
      ++lineNumber;
      if (scanningProcess) {
        Matcher mProcessEnd = processEnd.matcher(line);
        if (mProcessEnd.find()) {
          scanningProcess = false;
          break;
        }
        Matcher mClockStmt = clockStmt.matcher(line);
        if (mClockStmt.find()) {
          if (lineClockStmt != 0) {
            fail("found multiple clock statements");
          }
          lineClockStmt = lineNumber;
        }
        Matcher mResetStmt = resetStmt.matcher(line);
        if (mResetStmt.find()) {
          if (lineResetStmt != 0) {
            fail("found multiple reset statements");
          }
          lineResetStmt = lineNumber;
        }
      } else {
        Matcher mProcessBegin = processBegin.matcher(line);
        if (mProcessBegin.find()) {
          foundProcess = true;
          scanningProcess = true;
        }
      }
    }
    assertTrue("no process block present in generated code", foundProcess);
    assertTrue("no clock statement found", lineClockStmt != 0);
    assertTrue("no reset statement found", lineResetStmt != 0);
    // check the order in which these statements were found
    assertTrue("wrong statement order for asynchronous reset",
        lineResetStmt < lineClockStmt);
  }
  
  @Test
  public void testANDSignalGeneration() throws SchematicException, IOException {
    // Connect two inputs through an AND gate to an output.
    Schematic schematic = UtilSchematicConstruction
        .instantiateSchematic("test");
    NodeValue in0 = UtilSchematicConstruction.instantiateInputPin();
    schematic.addNode("in0", in0);
    NodeValue in1 = UtilSchematicConstruction.instantiateInputPin();
    schematic.addNode("in1", in1);
    NodeValue and0 = UtilSchematicConstruction.instantiateAnd();
    schematic.addNode("and0", and0);
    NodeValue out0 = UtilSchematicConstruction.instantiateOutputPin();
    schematic.addNode("out0", out0);
    ConnectionValue net0 = UtilSchematicConstruction.instantiateWire(
        in0.getPort("out"), and0.getPort("in0"));
    schematic.addConnection("net0", net0);
    ConnectionValue net1 = UtilSchematicConstruction.instantiateWire(
        in1.getPort("out"), and0.getPort("in1"));
    schematic.addConnection("net1", net1);
    ConnectionValue net2 = UtilSchematicConstruction.instantiateWire(
        and0.getPort("out"), out0.getPort("in"));
    schematic.addConnection("net2", net2);

    List<String> testLines = schematicToVHDL(schematic);
    
    Pattern archBegin = Pattern.compile("^\\s*architecture",
        Pattern.CASE_INSENSITIVE);
    Pattern archEnd = Pattern.compile("end\\s*architecture",
        Pattern.CASE_INSENSITIVE);
    Pattern andAssign = Pattern.compile("<=.*[aA][nN][dD]");
    
    boolean foundArchitecture = false;
    boolean foundGate = false;
    boolean scanningArchitecture = false;
    for (String line : testLines) {
      if (scanningArchitecture) {
        Matcher mArchEnd = archEnd.matcher(line);
        if (mArchEnd.find()) {
          scanningArchitecture = false;
          break;
        }
        Matcher mAndAssign = andAssign.matcher(line);
        if (mAndAssign.find()) {
          if (foundGate) {
            fail("multiple AND gates found");
          } else {
            foundGate = true;
          }
        }
      } else {
        Matcher mArchBegin = archBegin.matcher(line);
        if (mArchBegin.find()) {
          foundArchitecture = true;
          scanningArchitecture = true;
        }
      }
    }
    assertTrue("no architecture block found in generated code", 
        foundArchitecture);
    assertTrue("no AND gate found in generated code", foundGate);
  }
  
  @Test
  public void testORSignalGeneration() throws SchematicException, IOException {
    // Connect two inputs through an OR gate to an output.
    Schematic schematic = UtilSchematicConstruction
        .instantiateSchematic("test");
    NodeValue in0 = UtilSchematicConstruction.instantiateInputPin();
    schematic.addNode("in0", in0);
    NodeValue in1 = UtilSchematicConstruction.instantiateInputPin();
    schematic.addNode("in1", in1);
    NodeValue or0 = UtilSchematicConstruction.instantiateOr();
    schematic.addNode("or0", or0);
    NodeValue out0 = UtilSchematicConstruction.instantiateOutputPin();
    schematic.addNode("out0", out0);
    ConnectionValue net0 = UtilSchematicConstruction.instantiateWire(
        in0.getPort("out"), or0.getPort("in0"));
    schematic.addConnection("net0", net0);
    ConnectionValue net1 = UtilSchematicConstruction.instantiateWire(
        in1.getPort("out"), or0.getPort("in1"));
    schematic.addConnection("net1", net1);
    ConnectionValue net2 = UtilSchematicConstruction.instantiateWire(
        or0.getPort("out"), out0.getPort("in"));
    schematic.addConnection("net2", net2);

    List<String> testLines = schematicToVHDL(schematic);
    
    Pattern archBegin = Pattern.compile("^\\s*architecture",
        Pattern.CASE_INSENSITIVE);
    Pattern archEnd = Pattern.compile("end\\s*architecture",
        Pattern.CASE_INSENSITIVE);
    Pattern orAssign = Pattern.compile("<=.*[oO][rR]");
    
    boolean foundArchitecture = false;
    boolean foundGate = false;
    boolean scanningArchitecture = false;
    for (String line : testLines) {
      if (scanningArchitecture) {
        Matcher mArchEnd = archEnd.matcher(line);
        if (mArchEnd.find()) {
          scanningArchitecture = false;
          break;
        }
        Matcher mOrAssign = orAssign.matcher(line);
        if (mOrAssign.find()) {
          if (foundGate) {
            fail("multiple OR gates found");
          } else {
            foundGate = true;
          }
        }
      } else {
        Matcher mArchBegin = archBegin.matcher(line);
        if (mArchBegin.find()) {
          foundArchitecture = true;
          scanningArchitecture = true;
        }
      }
    }
    assertTrue("no architecture block found in generated code", 
        foundArchitecture);
    assertTrue("no OR gate found in generated code", foundGate);
  }
  
  @Test
  public void testNOTSignalGeneration() throws SchematicException, IOException {
    // Connect one input through a NOT gate to an output.
    Schematic schematic = UtilSchematicConstruction
        .instantiateSchematic("test");
    NodeValue in0 = UtilSchematicConstruction.instantiateInputPin();
    schematic.addNode("in0", in0);
    NodeValue not0 = UtilSchematicConstruction.instantiateNot();
    schematic.addNode("not0", not0);
    NodeValue out0 = UtilSchematicConstruction.instantiateOutputPin();
    schematic.addNode("out0", out0);
    ConnectionValue net0 = UtilSchematicConstruction.instantiateWire(
        in0.getPort("out"), not0.getPort("in"));
    schematic.addConnection("net0", net0);
    ConnectionValue net1 = UtilSchematicConstruction.instantiateWire(
        not0.getPort("out"), out0.getPort("in"));
    schematic.addConnection("net1", net1);

    List<String> testLines = schematicToVHDL(schematic);
    
    Pattern archBegin = Pattern.compile("^\\s*architecture",
        Pattern.CASE_INSENSITIVE);
    Pattern archEnd = Pattern.compile("end\\s*architecture",
        Pattern.CASE_INSENSITIVE);
    Pattern notAssign = Pattern.compile("<=.*[nN][oO][tT]");
    
    boolean foundArchitecture = false;
    boolean foundGate = false;
    boolean scanningArchitecture = false;
    for (String line : testLines) {
      if (scanningArchitecture) {
        Matcher mArchEnd = archEnd.matcher(line);
        if (mArchEnd.find()) {
          scanningArchitecture = false;
          break;
        }
        Matcher mNotAssign = notAssign.matcher(line);
        if (mNotAssign.find()) {
          if (foundGate) {
            fail("multiple NOT gates found");
          } else {
            foundGate = true;
          }
        }
      } else {
        Matcher mArchBegin = archBegin.matcher(line);
        if (mArchBegin.find()) {
          foundArchitecture = true;
          scanningArchitecture = true;
        }
      }
    }
    assertTrue("no architecture block found in generated code", 
        foundArchitecture);
    assertTrue("no NOT gate found in generated code", foundGate);
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
    ConnectionValue in0ToOut0 = UtilSchematicConstruction.instantiateWire(
        in0.getPort("out"), out0.getPort("in"));
    schematic.addConnection("in0_to_out0", in0ToOut0);

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
