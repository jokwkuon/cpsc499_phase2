package org.lsmr.pdg;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.lsmr.cfg.ControlFlowGraph;

public final class PDGTestUtils {

    private PDGTestUtils() {
    }

    public static List<ControlFlowGraph> buildCFGs(String relativePath) throws Exception {
        Path input = Paths.get(relativePath);
        return Main.buildCFGs(input);
    }

    public static ControlFlowGraph firstCFG(String relativePath) throws Exception {
        List<ControlFlowGraph> cfgs = buildCFGs(relativePath);
        if (cfgs.isEmpty()) {
            throw new IllegalStateException("No CFGs were built for: " + relativePath);
        }
        return cfgs.get(0);
    }
}