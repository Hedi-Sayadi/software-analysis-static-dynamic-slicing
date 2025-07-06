package de.uni_passau.fim.se2.sa.slicing.graph;

import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Provides an analysis computing a post-dominator tree for a CFG. */
public class PostDominatorTree extends Graph {

  PostDominatorTree(ClassNode pClassNode, MethodNode pMethodNode) {
    super(pClassNode, pMethodNode);
  }

  PostDominatorTree(ProgramGraph pCFG) {
    super(pCFG);
  }

  /**
   * Computes the post-dominator tree of the method.
   *
   * <p>The implementation uses the {@link #cfg} graph as the starting point.
   *
   * @return The post-dominator tree of the control-flow graph
   */
  @Override
  public ProgramGraph computeResult() {
    //Slide 67 and 68 in 4_controlflow + hep of llm
    ProgramGraph postDominatorTree = new ProgramGraph();

    Map<Node, Set<Node>> postDominators = new HashMap<>();
    Set<Node> allNodes = new HashSet<>(cfg.getNodes());
    Node exitNode = cfg.getExit().orElseThrow(() -> new IllegalStateException("There is no exit node"));

    for (Node node : cfg.getNodes()) {
      if (node.equals(exitNode)) {
        postDominators.put(node, Set.of(node));
      } else {
          postDominators.put(node, new HashSet<>(allNodes));
      }
      postDominatorTree.addNode(node);
    }

    boolean changed;
    do {
      changed = false;
      for (Node node : cfg.getNodes()) {
        if (node.equals(exitNode)) continue;

        Set<Node> newSet = new HashSet<>(allNodes);
        for (Node succ : cfg.getSuccessors(node)) {
          newSet.retainAll(postDominators.get(succ));
        }
        newSet.add(node);

        if (!newSet.equals(postDominators.get(node))) {
          postDominators.put(node, newSet);
          changed = true;
        }
      }
    } while (changed);

    // Build post dominator tree
    for (Node node : cfg.getNodes()) {
      if (node.equals(exitNode)) continue;

      Set<Node> pdoms = new HashSet<>(postDominators.get(node));
      pdoms.remove(node);

      Node ipdom = null;
      for (Node pdom : pdoms) {
        boolean isImmediate = true;
        for (Node other : pdoms) {
          if (!other.equals(pdom) && postDominators.get(other).contains(pdom)) {
            isImmediate = false;
            break;
          }
        }
        if (isImmediate) {
          ipdom = pdom;
          break;
        }
      }

      if (ipdom != null) {
        postDominatorTree.addEdge(ipdom, node);
      }
    }

    return postDominatorTree;
  }
}
