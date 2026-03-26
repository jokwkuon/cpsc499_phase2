package org.lsmr.pdg;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.lsmr.cfg.ControlFlowGraph;

public class CFGBuilderSmokeTest {

    @Test
    void buildsAtLeastOneCFG() throws Exception {
        Path input = Paths.get("sample/IfTest.java");

        List<ControlFlowGraph> cfgs = Main.buildCFGs(input);

        assertNotNull(cfgs);
        assertFalse(cfgs.isEmpty());
    }
}