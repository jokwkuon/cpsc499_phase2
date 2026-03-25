package org.lsmr.pdg;

import java.util.ArrayList;
import java.util.List;

public class ProgramDependenceGraph {
    private final List<PDGNode> nodes = new ArrayList<>();
    private final List<PDGEdge> edges = new ArrayList<>();

    public void addNode(PDGNode node) {
        nodes.add(node);
    }

    public void addEdge(PDGEdge edge) {
        edges.add(edge);
    }

    public List<PDGNode> getNodes() {
        return nodes;
    }

    public List<PDGEdge> getEdges() {
        return edges;
    }
}