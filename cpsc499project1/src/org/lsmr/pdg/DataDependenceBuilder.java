package org.lsmr.pdg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lsmr.cfg.ControlFlowGraph;
import org.lsmr.cfg.Edge;
import org.lsmr.cfg.Node;

public class DataDependenceBuilder {

    private static final String ENTRY  = "*ENTRY*";
    private static final String EXIT   = "*EXIT*";
    private static final String THROWN = "*THROWN*";

    public void addDataDependences(ControlFlowGraph cfg,
                                   ProgramDependenceGraph pdg,
                                   Map<Node, PDGNode> nodeMap) {

        List<Node> allNodes = cfg.nodes();

        // GEN/KILL sets
        Map<Node, Set<String>> genVar   = new HashMap<>();
        Map<Node, Set<Node>>   killDefs = new HashMap<>();
        Map<String, List<Node>> allDefsOf = new HashMap<>();

        for (Node n : allNodes) {
            if (isBookkeeping(n)) continue;
            String var = getDefinedVariable(n.label());
            if (var != null)
                allDefsOf.computeIfAbsent(var, k -> new ArrayList<>()).add(n);
        }

        for (Node n : allNodes) {
            genVar.put(n, new HashSet<>());
            killDefs.put(n, new HashSet<>());
            if (isBookkeeping(n)) continue;
            String var = getDefinedVariable(n.label());
            if (var != null) {
                genVar.get(n).add(var);
                List<Node> others = allDefsOf.get(var);
                if (others != null)
                    for (Node other : others)
                        if (!other.equals(n)) killDefs.get(n).add(other);
            }
        }

        Map<Node, Set<Node>> inDefs  = new HashMap<>();
        Map<Node, Set<Node>> outDefs = new HashMap<>();
        for (Node n : allNodes) {
            inDefs.put(n, new HashSet<>());
            outDefs.put(n, new HashSet<>());
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            for (Node n : allNodes) {
                Set<Node> newIn = new HashSet<>();
                for (Edge e : n.inEdges()) {
                    Set<Node> predOut = outDefs.get(e.source());
                    if (predOut != null) newIn.addAll(predOut);
                }
                Set<Node> newOut = new HashSet<>(newIn);
                newOut.removeAll(killDefs.get(n));
                if (!genVar.get(n).isEmpty()) newOut.add(n);
                inDefs.put(n, newIn);
                if (!newOut.equals(outDefs.get(n))) {
                    outDefs.put(n, newOut);
                    changed = true;
                }
            }
        }

        for (Node u : allNodes) {
            if (isBookkeeping(u)) continue;
            PDGNode pdgU = nodeMap.get(u);
            if (pdgU == null) continue;
            Set<String> usedVars = getUsedVariables(u.label());
            Set<Node> reaching = inDefs.get(u);
            if (reaching == null) continue;
            for (String var : usedVars) {
                for (Node def : reaching) {
                    if (isBookkeeping(def)) continue;
                    Set<String> defVars = genVar.get(def);
                    if (defVars != null && defVars.contains(var)) {
                        PDGNode pdgDef = nodeMap.get(def);
                        if (pdgDef != null)
                            pdg.addEdge(new PDGEdge(pdgDef, pdgU, PDGEdge.Type.DATA, var));
                    }
                }
            }
        }
    }

    private String getDefinedVariable(String label) {
        String s = stripLinePrefix(label).trim();

        if (s.endsWith(" ++ ;") || s.endsWith("++ ;") || s.endsWith("++")) {
            String c = s.replaceAll("\\+\\+\\s*;?\\s*$", "").trim();
            if (isSimpleIdentifier(c)) return c;
        }
        if (s.endsWith(" -- ;") || s.endsWith("-- ;") || s.endsWith("--")) {
            String c = s.replaceAll("--\\s*;?\\s*$", "").trim();
            if (isSimpleIdentifier(c)) return c;
        }
        if (s.startsWith("++") || s.startsWith("--")) {
            String c = s.substring(2).replaceAll(";$", "").trim();
            if (isSimpleIdentifier(c)) return c;
        }

        String stripped = stripTypePrefix(s);
        int eq = indexOfAssignmentOp(stripped);
        if (eq > 0) {
            String left = stripped.substring(0, eq).trim();
            int bracket = left.indexOf('[');
            if (bracket > 0) left = left.substring(0, bracket).trim();
            if (isSimpleIdentifier(left)) return left;
        }
        return null;
    }

    private Set<String> getUsedVariables(String label) {
        Set<String> used = new HashSet<>();
        String s = stripLinePrefix(label).trim();

        if (s.matches(".*[A-Za-z_][A-Za-z0-9_]*\\s*\\+\\+\\s*;?")) {
            String c = s.replaceAll("\\s*\\+\\+\\s*;?\\s*$", "").trim();
            if (isSimpleIdentifier(c)) { used.add(c); return used; }
        }
        if (s.matches(".*[A-Za-z_][A-Za-z0-9_]*\\s*--\\s*;?")) {
            String c = s.replaceAll("\\s*--\\s*;?\\s*$", "").trim();
            if (isSimpleIdentifier(c)) { used.add(c); return used; }
        }
        if (s.startsWith("++") || s.startsWith("--")) {
            String c = s.substring(2).replaceAll(";$", "").trim();
            if (isSimpleIdentifier(c)) { used.add(c); return used; }
        }

        String stripped = stripTypePrefix(s);
        int eq = indexOfAssignmentOp(stripped);
        if (eq > 0) {
            String lhs = stripped.substring(0, eq).trim();
            String rhs = stripped.substring(eq + 1).trim();
            collectIdentifiers(rhs, used);
            int bracket = lhs.indexOf('[');
            if (bracket > 0) {
                int close = lhs.indexOf(']', bracket);
                String idx = close > bracket ? lhs.substring(bracket + 1, close) : lhs.substring(bracket + 1);
                collectIdentifiers(idx, used);
            }
            if (eq > 0 && "+-*/%&|^".indexOf(stripped.charAt(eq - 1)) >= 0) {
                String lhsVar = lhs;
                int b = lhsVar.indexOf('[');
                if (b > 0) lhsVar = lhsVar.substring(0, b).trim();
                if (isSimpleIdentifier(lhsVar)) used.add(lhsVar);
            }
            return used;
        }

        collectIdentifiers(s, used);
        return used;
    }

    private String stripTypePrefix(String s) {
        String[] primitives = {"byte ", "short ", "char ", "int ", "long ",
                               "float ", "double ", "boolean "};
        for (String p : primitives)
            if (s.startsWith(p)) return s.substring(p.length()).trim();
        int space = s.indexOf(' ');
        if (space > 0) {
            String first = s.substring(0, space);
            String rest  = s.substring(space).trim();
            if (first.matches("[A-Z][A-Za-z0-9_]*") && indexOfAssignmentOp(rest) >= 0)
                return rest;
        }
        return s;
    }

    private int indexOfAssignmentOp(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '=') {
                if (i + 1 < s.length() && s.charAt(i + 1) == '=') { i++; continue; }
                if (i > 0 && "!<>".indexOf(s.charAt(i - 1)) >= 0) continue;
                return i;
            }
            if ("+-*/%&|^".indexOf(c) >= 0 && i + 1 < s.length() && s.charAt(i + 1) == '=')
                return i + 1;
        }
        return -1;
    }

    private void collectIdentifiers(String text, Set<String> result) {
        Matcher m = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*").matcher(text);
        while (m.find()) {
            String token = m.group();
            if (!isKeyword(token)) result.add(token);
        }
    }

    private String stripLinePrefix(String label) {
        int colon = label.indexOf(':');
        if (colon >= 0 && colon + 1 < label.length())
            return label.substring(colon + 1).trim();
        return label.trim();
    }

    private boolean isSimpleIdentifier(String text) {
        return text != null && text.matches("[A-Za-z_][A-Za-z0-9_]*");
    }

    private boolean isBookkeeping(Node n) {
        String label = n.label();
        return label.equals(ENTRY) || label.equals(EXIT) || label.equals(THROWN);
    }

    private boolean isKeyword(String token) {
        switch (token) {
            case "if": case "else": case "while": case "for": case "do":
            case "return": case "break": case "continue": case "throw":
            case "try": case "catch": case "finally": case "switch": case "case":
            case "new": case "this": case "super": case "instanceof":
            case "true": case "false": case "null":
            case "int": case "long": case "double": case "float": case "boolean":
            case "byte": case "short": case "char": case "void":
            case "class": case "interface": case "extends": case "implements":
            case "static": case "final": case "public": case "private":
            case "protected": case "abstract": case "synchronized":
                return true;
            default: return false;
        }
    }
}