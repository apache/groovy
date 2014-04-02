package com.xseagullx.groovy.gsoc
import com.sun.istack.internal.NotNull
import groovy.transform.Memoized
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeWalker
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

    @Memoized classNode(Class c) {
        def classNode = new ClassNode(c)
        classNode.module = moduleNode
        classNode
    }

    @Memoized classNode(String c) {
        new ClassNode(classLoader.loadClass(c))
    }

    @Override void exitImportStatement(@NotNull GroovyParser.ImportStatementContext ctx) {
        def clazz = classLoader.loadClass(ctx.IDENTIFIER().join('.'))
        moduleNode.addImport(clazz.simpleName, classNode(clazz))
    }

    @Override void enterPackageDefinition(@NotNull GroovyParser.PackageDefinitionContext ctx) {
        moduleNode.packageName = ctx.IDENTIFIER().join('.') + '.'
    }

    @Override void exitClassDeclaration(@NotNull GroovyParser.ClassDeclarationContext ctx) {
        def classNode = new ClassNode("$moduleNode.packageName${ctx.IDENTIFIER()}", Modifier.PUBLIC, classNode("java.lang.Object"))
        classNode.module = moduleNode
        moduleNode.classes.add(classNode)
    }
}
