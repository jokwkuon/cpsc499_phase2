package org.lsmr.pdg;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import ca.ucalgary.cpsc499_02.w26.Java1_2ANTLRLexer;
import ca.ucalgary.cpsc499_02.w26.Java1_2ANTLRParser;
import ca.ucalgary.cpsc499_02.w26.Java1_2ANTLRParser.CompilationUnitContext;
import org.lsmr.cfg.ControlFlowGraph;
import org.lsmr.cfg.Edge;
import org.lsmr.cfg.Node;
import org.lsmr.cfg.StatementNodeBuilder;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java org.lsmr.pdg.Main <Java-source-file>");
            System.exit(1);
        }

        Path input = Paths.get(args[0]);

        try {
            List<ControlFlowGraph> cfgs = buildCFGs(input);

            System.out.println("Built " + cfgs.size() + " CFG(s) from: " + input);
            System.out.println();

            for (ControlFlowGraph cfg : cfgs) {
                printCFG(cfg);
                System.out.println("--------------------------------------------------");
            }
        } catch (IOException e) {
            System.err.println("Could not read file: " + input);
            e.printStackTrace();
            System.exit(2);
        } catch (Exception e) {
            System.err.println("Failed to parse/build CFG for: " + input);
            e.printStackTrace();
            System.exit(3);
        }
    }

    public static List<ControlFlowGraph> buildCFGs(Path inputFile) throws IOException {
        Java1_2ANTLRLexer lexer =
            new Java1_2ANTLRLexer(CharStreams.fromPath(inputFile));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Java1_2ANTLRParser parser = new Java1_2ANTLRParser(tokens);

        CompilationUnitContext root = parser.compilationUnit();

        StatementNodeBuilder builder = new StatementNodeBuilder();
        root.accept(builder);

        return builder.getCFGs();
    }

    private static void printCFG(ControlFlowGraph cfg) {
        System.out.println("CFG name: " + cfg.name());
        System.out.println("Entry: " + cfg.entry.label());
        System.out.println("Normal exit: " + cfg.normalExit.label());
        System.out.println("Abrupt exit: " + cfg.abruptExit.label());
        System.out.println();

        System.out.println("Nodes:");
        for (Node node : cfg.nodes()) {
            System.out.println("  " + node.label());
        }

        System.out.println();
        System.out.println("Edges:");
        for (Edge edge : cfg.edges()) {
            String label = edge.label().toString();
            String extra = edge.extendedLabel();

            String edgeText = "  " + edge.source().label()
                + " -> "
                + (edge.target() == null ? "null" : edge.target().label());

            if (!label.isEmpty()) {
                edgeText += " [" + label;
                if (extra != null && !extra.isEmpty()) {
                    edgeText += ": " + extra;
                }
                edgeText += "]";
            }

            System.out.println(edgeText);
        }

        System.out.println();
    }
}