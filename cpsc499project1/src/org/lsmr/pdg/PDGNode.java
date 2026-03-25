package org.lsmr.pdg;

import org.lsmr.cfg.Node;

public class PDGNode {
    private final Node cfgNode;

    public PDGNode(Node cfgNode) {
        this.cfgNode = cfgNode;
    }

    public Node getCfgNode() {
        return cfgNode;
    }

    public String getLabel() {
        return cfgNode.label();
    }

    @Override
    public String toString() {
        return cfgNode.label();
    }
}