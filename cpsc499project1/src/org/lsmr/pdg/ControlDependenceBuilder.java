package org.lsmr.pdg;

import java.util.Map;

import org.lsmr.cfg.ControlFlowGraph;
import org.lsmr.cfg.Edge;
import org.lsmr.cfg.Node;

public class ControlDependenceBuilder {

    public void addControlDependences(ControlFlowGraph cfg,
                                      ProgramDependenceGraph pdg,
                                      Map<Node, PDGNode> nodeMap) {

        for (Node node : cfg.nodes()) {
            boolean isBranchNode = false;

            for (Edge edge : node.outEdges()) {
                String label = edge.label().toString();
                if (label.equals("TRUE") || label.equals("FALSE") || label.equals("CASE")) {
                    isBranchNode = true;
                    break;
                }
            }

            if (!isBranchNode) {
                continue;
            }

            for (Edge edge : node.outEdges()) {
                Node target = edge.target();

                if (target == null) {
                    continue;
                }

                String label = edge.label().toString();
                if (label.equals("TRUE") || label.equals("FALSE") || label.equals("CASE")) {
                    pdg.addEdge(new PDGEdge(
                        nodeMap.get(node),
                        nodeMap.get(target),
                        PDGEdge.Type.CONTROL,
                        null
                    ));
                }
            }
        }
    }
}