package org.lsmr.pdg;

public class PDGEdge {

    public enum Type {
        CONTROL,
        DATA
    }

    private final PDGNode from;
    private final PDGNode to;
    private final Type type;
    private final String variable;

    public PDGEdge(PDGNode from, PDGNode to, Type type, String variable) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.variable = variable;
    }

    public PDGNode getFrom() {
        return from;
    }

    public PDGNode getTo() {
        return to;
    }

    public Type getType() {
        return type;
    }

    public String getVariable() {
        return variable;
    }

    @Override
    public String toString() {
        if (type == Type.CONTROL) {
            return "CONTROL: " + from + " -> " + to;
        }
        return "DATA(" + variable + "): " + from + " -> " + to;
    }
}