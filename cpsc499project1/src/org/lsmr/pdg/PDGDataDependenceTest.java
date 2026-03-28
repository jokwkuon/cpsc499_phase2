package org.lsmr.pdg;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for data dependence edges in the PDG.
 *
 * Node labels from the CFG builder look like: "N: int x = 0 ; "
 * so we match fragments like "int x = 0", "x = 5", "while", etc.
 */
public class PDGDataDependenceTest {

    // ------------------------------------------------------------------
    // Straight-line def-use
    // ------------------------------------------------------------------

    @Test
    void defUseProducesAtLeastOneDataEdge() throws Exception {
        ProgramDependenceGraph pdg = PDGTestUtils.buildPDG("sample/DefUseTest.java");
        long count = pdg.getEdges().stream()
                .filter(e -> e.getType() == PDGEdge.Type.DATA)
                .count();
        assertTrue(count >= 1, "Expected at least one data edge");
    }

    @Test
    void defUseXReachesY() throws Exception {
        ProgramDependenceGraph pdg = PDGTestUtils.buildPDG("sample/DefUseTest.java");
        // "int x = 1 ; " -> "int y = x + 2 ; "
        assertTrue(PDGTestUtils.hasDataEdge(pdg, "x", "int x = 1", "int y = x + 2"),
                   "Expected data edge: int x = 1 --x--> int y = x + 2");
    }

    // ------------------------------------------------------------------
    // Redefinition: second def kills first
    // ------------------------------------------------------------------

    @Test
    void redefinitionKillsFirstDef() throws Exception {
        // int x = 1; x = 5; int y = x + 2;
        // Only x = 5 reaches int y = x + 2, not int x = 1.
        ProgramDependenceGraph pdg = PDGTestUtils.buildPDG("sample/RedefinitionTest.java");
        assertTrue(PDGTestUtils.hasDataEdge(pdg, "x", "x = 5", "int y = x + 2"),
                   "Expected data edge: x = 5 --x--> int y = x + 2");
    }

    @Test
    void firstDefDoesNotReachAfterRedefinition() throws Exception {
        ProgramDependenceGraph pdg = PDGTestUtils.buildPDG("sample/RedefinitionTest.java");
        assertFalse(PDGTestUtils.hasDataEdge(pdg, "x", "int x = 1", "int y = x + 2"),
                    "int x = 1 should NOT reach int y = x + 2 after redefinition by x = 5");
    }

    @Test
    void redefinitionProducesAtLeastOneDataEdge() throws Exception {
        ProgramDependenceGraph pdg = PDGTestUtils.buildPDG("sample/RedefinitionTest.java");
        long count = pdg.getEdges().stream()
                .filter(e -> e.getType() == PDGEdge.Type.DATA)
                .count();
        assertTrue(count >= 1, "Expected at least one data edge in redefinition test");
    }

    // ------------------------------------------------------------------
    // If-else: branch definitions present as PDG nodes
    // ------------------------------------------------------------------

    @Test
    void ifElseBothBranchNodesPresent() throws Exception {
        ProgramDependenceGraph pdg = PDGTestUtils.buildPDG("sample/IfElseTest.java");
        boolean foundY1 = false;
        boolean foundY2 = false;
        for (PDGNode node : pdg.getNodes()) {
            if (node.getLabel().contains("y = 1")) foundY1 = true;
            if (node.getLabel().contains("y = 2")) foundY2 = true;
        }
        assertTrue(foundY1, "Expected PDG node for 'y = 1'");
        assertTrue(foundY2, "Expected PDG node for 'y = 2'");
    }

    @Test
    void ifTestXDefReachesIfCondition() throws Exception {
        // "int x = 0 ; " defines x, "if ( x == 0 ) " uses x
        ProgramDependenceGraph pdg = PDGTestUtils.buildPDG("sample/IfTest.java");
        assertTrue(PDGTestUtils.hasDataEdge(pdg, "x", "int x = 0", "if"),
                   "Expected data edge: int x = 0 --x--> if (x == 0)");
    }

    // ------------------------------------------------------------------
    // While loop: loop-carried dependence
    // ------------------------------------------------------------------

    @Test
    void whileInitXReachesCondition() throws Exception {
        // "int x = 0 ; " -> "while ( x < 3 ) "
        ProgramDependenceGraph pdg = PDGTestUtils.buildPDG("sample/WhileTest.java");
        assertTrue(PDGTestUtils.hasDataEdge(pdg, "x", "int x = 0", "while"),
                   "Expected data edge: int x = 0 --x--> while (x < 3)");
    }

    @Test
    void whileBodyXReachesCondition() throws Exception {
        // "x = x + 1 ; " redefines x, must reach while condition (loop-carried)
        ProgramDependenceGraph pdg = PDGTestUtils.buildPDG("sample/WhileTest.java");
        assertTrue(PDGTestUtils.hasDataEdge(pdg, "x", "x = x + 1", "while"),
                   "Expected loop-carried data edge: x = x + 1 --x--> while");
    }

    @Test
    void whileBodyXUsesX() throws Exception {
        // x = x + 1 uses x; either int x = 0 or x = x + 1 itself reaches it
        ProgramDependenceGraph pdg = PDGTestUtils.buildPDG("sample/WhileTest.java");
        boolean found = PDGTestUtils.hasDataEdge(pdg, "x", "int x = 0", "x = x + 1")
                     || PDGTestUtils.hasDataEdge(pdg, "x", "x = x + 1", "x = x + 1");
        assertTrue(found, "Expected a data edge into the loop body for x");
    }

    // ------------------------------------------------------------------
    // Negative tests
    // ------------------------------------------------------------------

    @Test
    void noDataEdgeFromUnrelatedNodes() throws Exception {
        // In DefUseTest: y is defined but never used after, so no data edge FROM y
        ProgramDependenceGraph pdg = PDGTestUtils.buildPDG("sample/DefUseTest.java");
        assertFalse(PDGTestUtils.hasDataEdge(pdg, "y", "int y = x + 2", "int x = 1"),
                    "Should not have a backwards data edge from y to x's definition");
    }
}