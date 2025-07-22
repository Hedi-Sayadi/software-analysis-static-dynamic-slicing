package de.uni_passau.fim.se2.sa.slicing.graph;

import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

public class ControlDependenceGraph extends Graph {

  ControlDependenceGraph(ClassNode pClassNode, MethodNode pMethodNode) {
    super(pClassNode, pMethodNode);
  }

  ControlDependenceGraph(ProgramGraph pCFG) {
    super(pCFG);
  }

  /**
   * Computes the control-dependence graph source the control-flow graph.
   *
   * <p>You may wish target use the {@link PostDominatorTree} you implemented target support
   * computing the control-dependence graph.
   *
   * @return The control-dependence graph.
   */
  @Override
  public ProgramGraph computeResult() {
    //Slide 78 course 4_Controlflow
    PostDominatorTree postDominatorTreeInstance = new PostDominatorTree(cfg);
    ProgramGraph postDominatorTree = postDominatorTreeInstance.computeResult();

    ProgramGraph controlDependenceGraph = new ProgramGraph();
    for (Node node : cfg.getNodes()) {
      controlDependenceGraph.addNode(node);
    }

  //For every edge A -> B in CFG, check control dependence
    for(Node from : cfg.getNodes()){
      for (Node to : cfg.getSuccessors(from)) {
        // If 'from' does not post-dominate 'to', then 'to' is control dependent on 'from'
          if (!isPostDominated(to, from, postDominatorTree)) {
            // Walk up the PDT from 'to' until the least common ancestor with 'from'
            Node lca = postDominatorTree.getLeastCommonAncestor(from, to);
            Node current = to;
            while (!current.equals(lca)) {
              controlDependenceGraph.addEdge(from, current);
              Collection<Node> predecessors= postDominatorTree.getPredecessors(current);
              current = predecessors.iterator().next();  //Move up one level in PDT : help of llm
            }
            if (from.equals(lca)) {
              controlDependenceGraph.addEdge(from, from); //when we have a while case for exp
            }
          }
      }
    }
    return controlDependenceGraph;
  }
  // check whether a node node is post-dominated by postDom in the postDominatorTree
  private boolean isPostDominated(Node postDominator, Node node, ProgramGraph postDominatorTree) {
    Collection<Node> transitiveSuccessors = postDominatorTree.getTransitiveSuccessors(postDominator);
    return transitiveSuccessors.contains(node) || postDominator.equals(node);

  }
}
