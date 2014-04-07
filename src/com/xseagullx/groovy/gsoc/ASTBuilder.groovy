package com.xseagullx.groovy.gsoc
import com.sun.istack.internal.NotNull
import groovyjarjarasm.asm.Opcodes
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime.tree.TerminalNode
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.messages.Message
import org.codehaus.groovy.syntax.SyntaxException

import java.lang.reflect.Modifier

class ASTBuilder extends GroovyBaseListener {
    ModuleNode moduleNode

    private SourceUnit sourceUnit
    private ClassLoader classLoader

    ASTBuilder(SourceUnit sourceUnit, ClassLoader classLoader) {
        this.classLoader = classLoader
        this.sourceUnit = sourceUnit
        moduleNode = new ModuleNode(sourceUnit)

        def lexer = new GroovyLexer(new ANTLRInputStream(sourceUnit.source.reader))
        CommonTokenStream tokens = new CommonTokenStream(lexer)
        def parser = new GroovyParser(tokens)
        ParseTree tree = parser.compilationUnit()

        try {
            new ParseTreeWalker().walk(this, tree);
        }
        catch (CompilationFailedException ignored) {
            // Compilation failed.
        }
    }

    @Override void exitImportStatement(@NotNull GroovyParser.ImportStatementContext ctx) {
        def clazz = classLoader.loadClass(ctx.IDENTIFIER().join('.'))
        moduleNode.addImport(clazz.simpleName, ClassHelper.make(clazz))
        setupNodeLocation(moduleNode.imports.last(), ctx)
        setupNodeLocation(moduleNode.imports.last().type, ctx)
    }

    @Override void enterPackageDefinition(@NotNull GroovyParser.PackageDefinitionContext ctx) {
        moduleNode.packageName = ctx.IDENTIFIER().join('.') + '.'
        setupNodeLocation(moduleNode.package, ctx)
    }

    @Override void exitClassDeclaration(@NotNull GroovyParser.ClassDeclarationContext ctx) {
        def classNode = new ClassNode("$moduleNode.packageName${ctx.IDENTIFIER()}", Modifier.PUBLIC, ClassHelper.make("java.lang.Object"))
        setupNodeLocation(classNode, ctx)
        moduleNode.addClass(classNode)
        classNode.modifiers = parseClassModifiers(ctx.classModifiers())
        classNode.syntheticPublic = (classNode.modifiers & Opcodes.ACC_SYNTHETIC) != 0
        classNode.modifiers &= ~Opcodes.ACC_SYNTHETIC // FIXME Magic with syntetic modifier.
    }

    static void setupNodeLocation(ASTNode astNode, ParserRuleContext ctx) {
        astNode.lineNumber = ctx.start.line
        astNode.columnNumber = ctx.start.charPositionInLine + 1
        astNode.lastLineNumber = ctx.stop.line
        astNode.lastColumnNumber = ctx.stop.charPositionInLine + 1 + ctx.stop.text.length()
    }

    @Override void exitCompilationUnit(@org.antlr.v4.runtime.misc.NotNull GroovyParser.CompilationUnitContext ctx) {
        moduleNode.mainClassName = moduleNode.classes[0].name
    }

    int parseClassModifiers(@org.antlr.v4.runtime.misc.NotNull GroovyParser.ClassModifiersContext ctx) {
        checkModifierIsSingle(ctx.KW_ABSTRACT())
        checkModifierIsSingle(ctx.KW_FINAL())
        checkModifierIsSingle(ctx.KW_STATIC())
        checkModifierIsSingle(ctx.KW_STRICTFP())
        if (ctx.VISIBILITY_MODIFIER().size() > 1) {
            def modifier = ctx.VISIBILITY_MODIFIER(1).symbol

            def line = modifier.line
            def col = modifier.charPositionInLine + 1

            sourceUnit.errorCollector.addFatalError(Message.create(new SyntaxException("Cannot specify modifier: ${modifier.text} when access scope has already been defined at line: $line column: $col. File: $sourceUnit.name", line, col), sourceUnit))
        }

        int modifier = 0;
        modifier |= ctx.KW_STATIC() ? Opcodes.ACC_STATIC : 0
        modifier |= ctx.KW_ABSTRACT() ? Opcodes.ACC_ABSTRACT : 0
        modifier |= ctx.KW_FINAL() ? Opcodes.ACC_FINAL : 0
        modifier |= ctx.KW_STRICTFP() ? Opcodes.ACC_STRICT : 0

        if (!ctx.VISIBILITY_MODIFIER())
            modifier |= Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC
        else {
            switch (ctx.VISIBILITY_MODIFIER(0).symbol.text) {
                case "private": modifier |= Opcodes.ACC_PRIVATE; break
                case "protected": modifier |= Opcodes.ACC_PROTECTED; break
                case "public": modifier |= Opcodes.ACC_PUBLIC; break
            }
        }

        modifier
    }

    void checkModifierIsSingle(Collection<TerminalNode> nodes) {
        if (nodes.size() > 1) {
            def modifier = nodes[1].symbol

            def line = modifier.line
            def col = modifier.charPositionInLine + 1
            sourceUnit.addError(new SyntaxException("Cannot repeat modifier: $modifier.text at line: $line column: $col. File: $sourceUnit.name", line, col))
        }
    }
}
