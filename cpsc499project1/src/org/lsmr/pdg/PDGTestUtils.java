package org.lsmr.pdg;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.lsmr.cfg.ControlFlowGraph;

public final class PDGTestUtils {

    private PDGTestUtils() {}

    public static List<ControlFlowGraph> buildCFGs(String relativePath) throws Exception {
        Path input = Paths.get(System.getProperty("user.dir"), relativePath);
        return Main.buildCFGs(input);
    }

    public static ControlFlowGraph firstCFG(String relativePath) throws Exception {
        List<ControlFlowGraph> cfgs = buildCFGs(relativePath);
        if (cfgs.isEmpty())
            throw new IllegalStateException("No CFGs built for: " + relativePath);
        return cfgs.get(0);
    }

    public static ProgramDependenceGraph buildPDG(String relativePath) throws Exception {
        return new ProgramDependenceGraphBuilder().build(firstCFG(relativePath));
    }

    public static boolean hasControlEdge(ProgramDependenceGraph pdg,
                                         String fromFragment,
                                         String toFragment) {
        for (PDGEdge edge : pdg.getEdges())
            if (edge.getType() == PDGEdge.Type.CONTROL
                    && edge.getFrom().getLabel().contains(fromFragment)
                    && edge.getTo().getLabel().contains(toFragment))
                return true;
        return false;
    }

    public static boolean hasDataEdge(ProgramDependenceGraph pdg,
                                      String variable,
                                      String fromFragment,
                                      String toFragment) {
        for (PDGEdge edge : pdg.getEdges())
            if (edge.getType() == PDGEdge.Type.DATA
                    && variable.equals(edge.getVariable())
                    && edge.getFrom().getLabel().contains(fromFragment)
                    && edge.getTo().getLabel().contains(toFragment))
                return true;
        return false;
    }

    public static boolean hasNoControlEdge(ProgramDependenceGraph pdg,
                                            String fromFragment,
                                            String toFragment) {
        return !hasControlEdge(pdg, fromFragment, toFragment);
    }
}