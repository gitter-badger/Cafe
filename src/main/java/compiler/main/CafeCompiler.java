package compiler.main;

import cafelang.ir.CafeModule;
import compiler.analyzer.SemanticsChecker;
import compiler.ast.Node;
import compiler.ast.Node.ProgramNode;
import compiler.gen.ASTToCafeIrVisitor;
import compiler.gen.JVMByteCodeGenVisitor;
import compiler.gen.SymbolReferenceAssignmentVisitor;
import compiler.main.Main.Result;
import compiler.parser.Parser;
import compiler.parser.ParserFactory;
import compiler.parser.ParserType;
import compiler.util.Context;
import compiler.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static compiler.util.Messages.success;

public class CafeCompiler {
    private ParserFactory parserFactory;
    private Log log;
    private SourceFileManager fileManager;
    private final String moduleName;

    private Parser parser;
    private SemanticsChecker analyzer;

    private String outputFilePath;

    public CafeCompiler(String source) {
        Context context = new Context();

        log = Log.instance(context);
        fileManager = SourceFileManager.instance(context);
        fileManager.setSourceFile(source);

        String fileName = source.substring( source.lastIndexOf('\\')+1);
        moduleName = fileName.substring(0, fileName.lastIndexOf('.'));

        outputFilePath = source.substring( 0, source.lastIndexOf('\\')+1);
        outputFilePath += moduleName + ".class";

        parserFactory = ParserFactory.instance(context);
        parser = parserFactory.newParser(ParserType.MAINPARSER, fileManager.asCharList());

        analyzer = SemanticsChecker.instance(context);
    }

    enum Phase {
        PARSE,
        ANALYZE,
        IR,
        GEN
    }

    boolean checkErrors() {
        if (log.entries() > 0) {
            return true;
        }
        return false;
    }

    public Result compile() {
        Node programNode = null;
        CafeModule module = null;
        byte[] byteCode = null;
        for (Phase phase : Phase.values()) {
            switch (phase) {
                case PARSE:
                    programNode = parser.parse();
                    break;
                case ANALYZE:
//                    System.out.println((char) 27 + "[33m" + "\nPrettyPrint");
//                    new PrettyPrinter().prettyPrint(programNode);
                    analyzer.visitProgram((ProgramNode) programNode);
                    break;
                case IR:
                    module = new ASTToCafeIrVisitor().transform((ProgramNode) programNode, moduleName);
                    module.accept(new SymbolReferenceAssignmentVisitor());
                    break;
                case GEN:
                    byteCode = new JVMByteCodeGenVisitor().generateByteCode(module, moduleName);
                    File op = new File(outputFilePath);
                    try (FileOutputStream out = new FileOutputStream(op)) {
                        out.write(byteCode);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
            if (checkErrors()) {
                return Result.ERROR;
            }
        }
        success("");
        return Result.OK;
    }
}