package de.uni_passau.fim.se2.sa.slicing.graph;

import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/** Provides an analysis that calculates the program-dependence graph. */
public class ProgramDependenceGraph extends Graph implements Sliceable<Node> {

  private ProgramGraph pdg;
  private final ProgramGraph cdg;
  private final ProgramGraph ddg;

  public ProgramDependenceGraph(ClassNode pClassNode, MethodNode pMethodNode) {
    super(pClassNode, pMethodNode);
    pdg = null;

    if (cfg != null) {
      cdg = new ControlDependenceGraph(pClassNode, pMethodNode).computeResult();
      ddg = new DataDependenceGraph(pClassNode, pMethodNode).computeResult();
    } else {
      cdg = null;
      ddg = null;
    }
  }

  public ProgramDependenceGraph(ProgramGraph pProgramGraph) {
    super(null);
    pdg = pProgramGraph;
    cdg = null;
    ddg = null;
  }

  /**
   * Computes the program-dependence graph from a control-flow graph.
   *
   * <p>You may wish to use the {@link ControlDependenceGraph} and {@link DataDependenceGraph} you
   * have already implemented to support computing the program-dependence graph.
   *
   * @return A program-dependence graph.
   */
  @Override
  public ProgramGraph computeResult() {
    pdg = new ProgramGraph();
    for (Node node : ddg.getNodes()) {
      pdg.addNode(node);
    }
    for (Node node : cdg.getNodes()) {
      pdg.addNode(node);
    }
    for (Node node : ddg.getNodes()) {
      for (Node target : ddg.getSuccessors(node)) {
        pdg.addEdge(node, target);
      }
    }
    for (Node node : cdg.getNodes()) {
      for (Node target : cdg.getSuccessors(node)) {
        pdg.addEdge(node, target);
      }
    }

    return pdg;
  }

  /** {@inheritDoc} */
  @Override
  public Set<Node> backwardSlice(Node pCriterion) {
    Set<Node> result = new HashSet<>();
    Stack<Node> toProcess = new Stack<>();
    toProcess.push(pCriterion);
    while (!toProcess.isEmpty()) {
      Node node = toProcess.pop();
      if (!result.contains(node)) {
        result.add(node);
        for (Node pred : pdg.getPredecessors(node)) {
          toProcess.push(pred);
        }
      }
    }
    return result;
}
}
