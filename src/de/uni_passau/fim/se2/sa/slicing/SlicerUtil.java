package de.uni_passau.fim.se2.sa.slicing;

import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import de.uni_passau.fim.se2.sa.slicing.coverage.CoverageTracker;
import de.uni_passau.fim.se2.sa.slicing.graph.ProgramDependenceGraph;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.util.Set;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

public class SlicerUtil {

    /**
     * Executes the provided test case on the given class via the JUnit test framework.
     *
     * @param className The name of the class to be tested.
     * @param testCase  The name of the test case to be executed.
     */
    public static void executeTest(String className, String testCase) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(
                        selectMethod(className + "Test", testCase)
                )
                .build();
        Launcher launcher = LauncherFactory.create();
        launcher.execute(request);
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
