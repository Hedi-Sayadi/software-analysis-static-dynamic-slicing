package de.uni_passau.fim.se2.sa.slicing.graph;

import br.usp.each.saeg.asm.defuse.Variable;
import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.util.*;

public class DataDependenceGraph extends Graph {

  DataDependenceGraph(ClassNode pClassNode, MethodNode pMethodNode) {
    super(pClassNode, pMethodNode);
  }

  /**
   * Computes the data-dependence graph from the control-flow graph.
   *
   * <p>This requires the computation of the reaching-definition algorithm. We recommend using the
   * provided {@link DataFlowAnalysis} implementation.
   *
   * <p>Remember that the CFG stores for each node the instruction at that node. With that, calling
   * {@link DataFlowAnalysis#definedBy(String, MethodNode, AbstractInsnNode)} provides a collection
   * of {@link Variable}s that are defined by this particular instruction; calling {@link
   * DataFlowAnalysis#usedBy(String, MethodNode, AbstractInsnNode)} provides a collection of {@link
   * Variable}s that are used by this particular instruction, respectively. From this information
   * you can compute for each node n in the CFG the GEN[n] and KILL[n] sets. Afterwards, it is
   * possible to compute the IN[n] and OUT[n] sets using the reaching-definitions algorithm.
   *
   * <p>Finally, you can compute all def-use pairs and construct the data-dependence graph from
   * these pairs.
   *
   * @return The data-dependence graph for a control-flow graph
   */
  @Override
  public ProgramGraph computeResult() {
    ProgramGraph ddg = new ProgramGraph();
    for (Node node : cfg.getNodes()) {
      ddg.addNode(node);
    }

    Map<Node, Set<Variable>> GEN = computeGenSets(cfg);
    Map<Node, Set<Variable>> KILL = computeKillSets(cfg, GEN);

    Map<Node, Set<Map.Entry<Node, Variable>>> IN = new HashMap<>();
    Map<Node, Set<Map.Entry<Node, Variable>>> OUT = new HashMap<>();
    initializeInOut(cfg, GEN, IN, OUT);
    computeReachingDefinitions(cfg, GEN, KILL, IN, OUT);

    addDataDependenceEdges(cfg, ddg, GEN, IN);
    return ddg;

  }

  private Map<Node, Set<Variable>> computeGenSets(ProgramGraph cfg) {
    Map<Node, Set<Variable>> gen = new HashMap<>();

    for (Node node : cfg.getNodes()) {
      AbstractInsnNode insn = node.getInstruction();
      if (insn == null) continue;

      Set<Variable> defined;
        try {
            defined = new HashSet<>(DataFlowAnalysis.definedBy(classNode.name, methodNode, insn));
        } catch (AnalyzerException e) {
            throw new RuntimeException(e);
        }
        gen.put(node, defined);
    }
    return gen;
  }


  private Map<Node, Set<Variable>> computeKillSets(ProgramGraph cfg, Map<Node, Set<Variable>> GEN) {
    Map<Node, Set<Variable>> kill = new HashMap<>();
    for (Node node : cfg.getNodes()) {
      Set<Variable> killSet = new HashSet<>();
      Set<Variable> genSet = GEN.getOrDefault(node, Set.of());

      for (Variable def : genSet) {
        for (Node other : cfg.getNodes()) {
          if (other.equals(node)) continue;

          AbstractInsnNode otherInsn = other.getInstruction();
          if (otherInsn == null) continue;
          Set<Variable> defs;
            try {
                defs = new HashSet<>(DataFlowAnalysis.definedBy(classNode.name, methodNode, otherInsn));
            } catch (AnalyzerException e) {
                throw new RuntimeException(e);
            }
            for (Variable otherDef : defs) {
            if (otherDef.equals(def)) {
              killSet.add(otherDef);
            }
          }
        }
      }
      kill.put(node, killSet);
    }
    return kill;
  }

  private void initializeInOut(ProgramGraph cfg, Map<Node, Set<Variable>> GEN, Map<Node, Set<Map.Entry<Node, Variable>>> IN, Map<Node, Set<Map.Entry<Node, Variable>>> OUT) {
    for (Node node : cfg.getNodes()) {
      IN.put(node, new HashSet<>());

      Set<Map.Entry<Node, Variable>> genEntries = new HashSet<>();
      for (Variable var : GEN.getOrDefault(node, Set.of())) {
        genEntries.add(Map.entry(node, var));
      }

      OUT.put(node, genEntries);
    }
  }


  private void computeReachingDefinitions(ProgramGraph cfg, Map<Node, Set<Variable>> GEN, Map<Node, Set<Variable>> KILL, Map<Node, Set<Map.Entry<Node, Variable>>> IN, Map<Node, Set<Map.Entry<Node, Variable>>> OUT) {
   //help of an llm
    boolean changed;
    do {
      changed = false;

      for (Node node : cfg.getNodes()) {
        Set<Map.Entry<Node, Variable>> in = new HashSet<>();

        // IN[n] = ⋃ OUT[p] for all predecessors
        for (Node pred : cfg.getPredecessors(node)) {
          in.addAll(OUT.getOrDefault(pred, Set.of()));
        }

        // OUT[n] = (IN[n] - KILL[n]) ∪ GEN[n]
        Set<Map.Entry<Node, Variable>> out = new HashSet<>();

        Set<Variable> killSet = KILL.getOrDefault(node, Set.of());
        for (Map.Entry<Node, Variable> entry : in) {
          if (!killSet.contains(entry.getValue())) {
            out.add(entry);
          }
        }

        for (Variable var : GEN.getOrDefault(node, Set.of())) {
          out.add(Map.entry(node, var));
        }

        if (!in.equals(IN.get(node)) || !out.equals(OUT.get(node))) {
          IN.put(node, in);
          OUT.put(node, out);
          changed = true;
        }
      }
    } while (changed);
  }


  private void addDataDependenceEdges(ProgramGraph cfg, ProgramGraph ddg, Map<Node, Set<Variable>> GEN, Map<Node, Set<Map.Entry<Node, Variable>>> IN) {
    for (Node useNode : cfg.getNodes()) {
      AbstractInsnNode useInsn = useNode.getInstruction();
      if (useInsn == null) continue;

      Set<Variable> usedVars;
        try {
            usedVars = new HashSet<>(DataFlowAnalysis.usedBy(classNode.name, methodNode, useInsn));
        } catch (AnalyzerException e) {
            throw new RuntimeException(e);
        }

      for (Map.Entry<Node, Variable> defEntry : IN.getOrDefault(useNode, Set.of())) {
        Node defNode = defEntry.getKey();
        Variable defVar = defEntry.getValue();

        if (usedVars.contains(defVar)) {
          ddg.addEdge(defNode, useNode);
      }
    }
  }
}
}

