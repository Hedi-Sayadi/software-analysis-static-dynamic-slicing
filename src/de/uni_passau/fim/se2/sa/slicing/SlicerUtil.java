package de.uni_passau.fim.se2.sa.slicing;

import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import de.uni_passau.fim.se2.sa.slicing.coverage.CoverageTracker;
import de.uni_passau.fim.se2.sa.slicing.graph.ProgramDependenceGraph;

import java.lang.reflect.Method;
import java.util.Set;

public class SlicerUtil {

    /**
     * Executes the provided test case on the given class via the JUnit test framework.
     *
     * @param className The name of the class to be tested.
     * @param testCase  The name of the test case to be executed.
     */
    public static void executeTest(String className, String testCase) {
        // Help of an llm :
        // Prompt : i want to create a method that takes a name of class className and name of the test case testCase and perform the execution of the test without adding any external dependency in java
        try {
            Class<?> testClass = Class.forName( "de.uni_passau.fim.se2.sa.examples.NestedLoopTest");
            String methodName = "testNestedWhileForLoops";
            Method method = testClass.getMethod(methodName);
            Object instance = testClass.getDeclaredConstructor().newInstance();
            method.invoke(instance);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found: " + className, e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Method not found: " + testCase + " in class " + className, e);
        } catch (Exception e) {
            throw new RuntimeException("Error executing test method: " + testCase, e);
        }
    }

    /**
     * Simplifies the given program dependence graph by removing all nodes and corresponding edges
     * that were not covered by the executed test.
     *
     * @param pPDG The program dependence graph to simplify.
     * @return The simplified program dependence graph.
     */
    public static ProgramDependenceGraph simplify(final ProgramDependenceGraph pPDG) {
        Set<Integer> coveredLines = CoverageTracker.getVisitedLines();

        ProgramGraph pdg = pPDG.computeResult();
        ProgramGraph simplifiedGraph = new ProgramGraph();

        // we add only the nodes whose line number is in the covered lines
        for (Node node : pdg.getNodes()) {
            if (coveredLines.contains(node.getLineNumber())) {
                simplifiedGraph.addNode(node);
            }
        }

        // Add edges between covered nodes
        for (Node node : pdg.getNodes()) {
            if (coveredLines.contains(node.getLineNumber())) {
                for (Node succ : pdg.getSuccessors(node)) {
                    if (coveredLines.contains(succ.getLineNumber())) {
                        simplifiedGraph.addEdge(node, succ);
                    }
                }
            }
        }

        return new ProgramDependenceGraph(simplifiedGraph);
}
}
