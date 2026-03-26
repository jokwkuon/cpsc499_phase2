package org.lsmr.pdg;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.lsmr.cfg.ControlFlowGraph;
import org.lsmr.cfg.Node;

public class DataDependenceBuilder {

    public void addDataDependences(ControlFlowGraph cfg,
                                   ProgramDependenceGraph pdg,
                                   Map<Node, PDGNode> nodeMap) {

        List<Node> nodes = new ArrayList<>(cfg.nodes());
        nodes.sort(Comparator.comparingInt(this::nodeOrder));

        for (int i = 0; i < nodes.size(); i++) {
            Node defNode = nodes.get(i);
            String definedVar = getDefinedVariable(defNode.label());

            if (definedVar == null) {
                continue;
            }

            for (int j = i + 1; j < nodes.size(); j++) {
                Node laterNode = nodes.get(j);

                if (usesVariable(laterNode.label(), definedVar)) {
                    pdg.addEdge(new PDGEdge(
                        nodeMap.get(defNode),
                        nodeMap.get(laterNode),
                        PDGEdge.Type.DATA,
                        definedVar
                    ));
                    break;
                }

                String redefinedVar = getDefinedVariable(laterNode.label());
                if (definedVar.equals(redefinedVar)) {
                    break;
                }
            }
        }
    }

    private int nodeOrder(Node node) {
        String label = node.label().trim();

        if (label.equals("*ENTRY*")) {
            return -1000;
        }
        if (label.equals("*EXIT*")) {
            return 1000000;
        }
        if (label.equals("*THROWN*")) {
            return 1000001;
        }

        int colon = label.indexOf(':');
        if (colon > 0) {
            String prefix = label.substring(0, colon).trim();
            if (prefix.matches("\\d+")) {
                return Integer.parseInt(prefix);
            }
        }

        return 500000;
    }

    private String getDefinedVariable(String label) {
        String cleaned = stripLinePrefix(label).trim();

        if (cleaned.startsWith("int ")) {
            String rest = cleaned.substring(4).trim();
            int eq = rest.indexOf('=');
            if (eq > 0) {
                return rest.substring(0, eq).trim();
            }
        }

        int eq = cleaned.indexOf('=');
        if (eq > 0) {
            String left = cleaned.substring(0, eq).trim();
            if (isSimpleIdentifier(left)) {
                return left;
            }
        }

        return null;
    }

    private boolean usesVariable(String label, String variable) {
        String cleaned = stripLinePrefix(label).trim();

        int eq = cleaned.indexOf('=');
        if (eq >= 0) {
            String rhs = cleaned.substring(eq + 1);
            return containsIdentifier(rhs, variable);
        }

        if (cleaned.contains("if") || cleaned.contains("while") || cleaned.contains("return")) {
            return containsIdentifier(cleaned, variable);
        }

        return false;
    }

    private String stripLinePrefix(String label) {
        int colon = label.indexOf(':');
        if (colon >= 0 && colon + 1 < label.length()) {
            return label.substring(colon + 1).trim();
        }
        return label.trim();
    }

    private boolean isSimpleIdentifier(String text) {
        return text.matches("[A-Za-z_][A-Za-z0-9_]*");
    }

    private boolean containsIdentifier(String text, String variable) {
        return Pattern.compile("\\b" + Pattern.quote(variable) + "\\b").matcher(text).find();
    }
}