# Static and Dynamic Slicing

This project computes backward slices of Java methods. It builds control-flow
and program-dependence graphs, then identifies the instructions that can
influence a selected variable at a selected source line.

The tool supports both:

- **Static slicing**, based on the bytecode and dependence graph.
- **Dynamic slicing**, which instruments the class and executes a supplied
  test before calculating the slice.

## Requirements

- Java 21
- Maven
- Bash, when using `run.sh`

## Build and test

Run the test suite with:

```bash
mvn clean test
```

Build the executable JAR with:

```bash
mvn -DskipUnitTests package
```

This creates `target/slicer.jar`.

## Running a static slice

The main options are:

- `--class` (`-c`): fully qualified class name
- `--method` (`-m`): method name and JVM descriptor
- `--variablename` (`-v`): variable used as the slicing criterion
- `--linenumber` (`-l`): source line of the slicing criterion
- `--sourcefile` (`-s`): optional source file for readable output
- `--targetfile` (`-t`): optional file for the extracted slice
- `--xmlfile` (`-x`): extract the result as XML

For example:

```bash
java -jar target/slicer.jar \
  --class de.uni_passau.fim.se2.sa.examples.Calculator \
  --method "calculate:(I)I" \
  --variablename result \
  --linenumber 10 \
  --sourcefile src/de/uni_passau/fim/se2/sa/examples/Calculator.java
```

The method descriptor must use JVM notation. For example, `()I` means a
method with no parameters that returns an integer.

## Running a dynamic slice

Dynamic slicing additionally requires the test method that should be executed:

```bash
java -javaagent:target/slicer.jar \
  -jar target/slicer.jar \
  --class de.uni_passau.fim.se2.sa.examples.Calculator \
  --method "calculate:(I)I" \
  --variablename result \
  --linenumber 10 \
  --dynamic "testCalculate"
```

The `run.sh` script provides the same workflow and builds the JAR before
running the slicer:

```bash
./run.sh \
  --class de.uni_passau.fim.se2.sa.examples.Calculator \
  --method "calculate:(I)I" \
  --variablename result \
  --linenumber 10
```

Use `--xmlfile` when an XML representation is required:

```bash
./run.sh \
  --class de.uni_passau.fim.se2.sa.examples.Calculator \
  --method "calculate:(I)I" \
  --variablename result \
  --linenumber 10 \
  --xmlfile
```

## Project structure

```text
src/               Slicing implementation and example classes
test/              Unit and property-based tests
expected-results/  Expected text and XML slicing results
run.sh             Command-line wrapper for building and running the slicer
pom.xml            Maven build configuration
```

The main implementation is in `SlicerMain`, with supporting components for
control-flow graphs, data-dependence graphs, program-dependence graphs,
instrumentation, and result extraction.
