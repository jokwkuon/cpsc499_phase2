package org.lsmr.pdg;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.lsmr.cfg.ControlFlowGraph;

public class PDGControlDependenceTest {

    @Test
    void ifTestProducesAtLeastOneControlDependence() throws Exception {
        ControlFlowGraph cfg = PDGTestUtils.firstCFG("sample/IfTest.java");

        ProgramDependenceGraphBuilder builder = new ProgramDependenceGraphBuilder();
        ProgramDependenceGraph pdg = builder.build(cfg);

        boolean foundControl = pdg.getEdges().stream()
                .anyMatch(e -> e.getType() == PDGEdge.Type.CONTROL);

        assertTrue(foundControl);
    }

    @Test
    void ifTestHasControlEdgeFromIfNode() throws Exception {
        ControlFlowGraph cfg = PDGTestUtils.firstCFG("sample/IfTest.java");

        ProgramDependenceGraphBuilder builder = new ProgramDependenceGraphBuilder();
        ProgramDependenceGraph pdg = builder.build(cfg);

        boolean foundIfSource = pdg.getEdges().stream()
                .filter(e -> e.getType() == PDGEdge.Type.CONTROL)
                .anyMatch(e -> e.getFrom().getLabel().contains("if"));

        assertTrue(foundIfSource);
    }

    @Test
    void ifElseHasControlEdgeFromIfNode() throws Exception {
        ControlFlowGraph cfg = PDGTestUtils.firstCFG("sample/IfElseTest.java");

        ProgramDependenceGraphBuilder builder = new ProgramDependenceGraphBuilder();
        ProgramDependenceGraph pdg = builder.build(cfg);

        boolean foundIfSource = pdg.getEdges().stream()
                .filter(e -> e.getType() == PDGEdge.Type.CONTROL)
                .anyMatch(e -> e.getFrom().getLabel().contains("if"));

        assertTrue(foundIfSource);
    }

    @Test
    void ifElseProducesAtLeastTwoControlEdges() throws Exception {
        ControlFlowGraph cfg = PDGTestUtils.firstCFG("sample/IfElseTest.java");

        ProgramDependenceGraphBuilder builder = new ProgramDependenceGraphBuilder();
        ProgramDependenceGraph pdg = builder.build(cfg);

        long count = pdg.getEdges().stream()
                .filter(e -> e.getType() == PDGEdge.Type.CONTROL)
                .count();

        assertTrue(count >= 2);
    }

    @Test
    void whileHasControlEdgeFromWhileNode() throws Exception {
        ControlFlowGraph cfg = PDGTestUtils.firstCFG("sample/WhileTest.java");

        ProgramDependenceGraphBuilder builder = new ProgramDependenceGraphBuilder();
        ProgramDependenceGraph pdg = builder.build(cfg);

        boolean foundWhileSource = pdg.getEdges().stream()
                .filter(e -> e.getType() == PDGEdge.Type.CONTROL)
                .anyMatch(e -> e.getFrom().getLabel().contains("while"));

        assertTrue(foundWhileSource);
    }
}