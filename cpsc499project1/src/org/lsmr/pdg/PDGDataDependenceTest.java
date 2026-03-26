package org.lsmr.pdg;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.lsmr.cfg.ControlFlowGraph;

public class PDGDataDependenceTest {

    @Test
    void defUseTestProducesAtLeastOneDataDependence() throws Exception {
        ControlFlowGraph cfg = PDGTestUtils.firstCFG("sample/DefUseTest.java");

        ProgramDependenceGraphBuilder builder = new ProgramDependenceGraphBuilder();
        ProgramDependenceGraph pdg = builder.build(cfg);

        boolean foundData = pdg.getEdges().stream()
                .anyMatch(e -> e.getType() == PDGEdge.Type.DATA);

        assertTrue(foundData);
    }

    @Test
    void defUseTestHasDataDependenceOnX() throws Exception {
        ControlFlowGraph cfg = PDGTestUtils.firstCFG("sample/DefUseTest.java");

        ProgramDependenceGraphBuilder builder = new ProgramDependenceGraphBuilder();
        ProgramDependenceGraph pdg = builder.build(cfg);

        boolean foundX = pdg.getEdges().stream()
                .filter(e -> e.getType() == PDGEdge.Type.DATA)
                .anyMatch(e -> "x".equals(e.getVariable()));

        assertTrue(foundX);
    }

    @Test
    void defUseTestConnectsXDefinitionToLaterUse() throws Exception {
        ControlFlowGraph cfg = PDGTestUtils.firstCFG("sample/DefUseTest.java");

        ProgramDependenceGraphBuilder builder = new ProgramDependenceGraphBuilder();
        ProgramDependenceGraph pdg = builder.build(cfg);

        boolean foundExpectedEdge = pdg.getEdges().stream()
                .filter(e -> e.getType() == PDGEdge.Type.DATA)
                .anyMatch(e ->
                        "x".equals(e.getVariable())
                     && e.getFrom().getLabel().contains("int x = 1")
                     && e.getTo().getLabel().contains("int y = x + 2")
                );

        assertTrue(foundExpectedEdge);
    }

    @Test
    void redefinitionTestProducesAtLeastOneDataDependence() throws Exception {
        ControlFlowGraph cfg = PDGTestUtils.firstCFG("sample/RedefinitionTest.java");

        ProgramDependenceGraphBuilder builder = new ProgramDependenceGraphBuilder();
        ProgramDependenceGraph pdg = builder.build(cfg);

        boolean foundData = pdg.getEdges().stream()
                .anyMatch(e -> e.getType() == PDGEdge.Type.DATA);

        assertTrue(foundData);
    }
}