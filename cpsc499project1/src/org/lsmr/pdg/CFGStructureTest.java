package org.lsmr.pdg;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.lsmr.cfg.ControlFlowGraph;
import org.lsmr.cfg.Edge;
import org.lsmr.cfg.Node;

public class CFGStructureTest {

    @Test
    void ifTestHasIfNode() throws Exception {
        ControlFlowGraph cfg = PDGTestUtils.firstCFG("sample/IfTest.java");

        boolean foundIfNode = false;
        for (Node node : cfg.nodes()) {
            if (node.label().contains("if")) {
                foundIfNode = true;
                break;
            }
        }

        assertTrue(foundIfNode);
    }

    @Test
    void ifTestHasBranchEdge() throws Exception {
        ControlFlowGraph cfg = PDGTestUtils.firstCFG("sample/IfTest.java");

        boolean foundBranch = false;
        for (Edge edge : cfg.edges()) {
            String label = edge.label().toString();
            if (label.equals("TRUE") || label.equals("FALSE")) {
                foundBranch = true;
                break;
            }
        }

        assertTrue(foundBranch);
    }

    @Test
    void cfgHasNodes() throws Exception {
        ControlFlowGraph cfg = PDGTestUtils.firstCFG("sample/IfTest.java");
        assertFalse(cfg.nodes().isEmpty());
    }
}