package com.xseagullx.groovy.gsoc
import com.sun.istack.internal.NotNull
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.control.SourceUnit

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

        new ParseTreeWalker().walk(this, tree);
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
        classNode.syntheticPublic = true
        setupNodeLocation(classNode, ctx)
        moduleNode.addClass(classNode)
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
}
