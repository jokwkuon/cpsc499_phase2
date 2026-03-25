package org.lsmr.pdg;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.lsmr.cfg.ControlFlowGraph;

public class PDGControlDependenceTest {

    @Test
    void ifTestProducesAtLeastOneControlDependence() throws Exception {
        ControlFlowGraph cfg = PDGTestUtils.firstCFG("src/IfTest.java");

        ProgramDependenceGraphBuilder builder = new ProgramDependenceGraphBuilder();
        ProgramDependenceGraph pdg = builder.build(cfg);

        boolean foundControl = pdg.getEdges().stream()
                .anyMatch(e -> e.getType() == PDGEdge.Type.CONTROL);

        assertTrue(foundControl);
    }
}