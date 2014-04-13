package com.xseagullx.groovy.gsoc

import groovyjarjarasm.asm.Opcodes
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.misc.NotNull
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime.tree.TerminalNode
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.SourceUnit
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
        def classNode = new ClassNode("${moduleNode.packageName ?: ""}${ctx.IDENTIFIER()}", Modifier.PUBLIC, ClassHelper.make("java.lang.Object"))
        setupNodeLocation(classNode, ctx)
        moduleNode.addClass(classNode)
        classNode.modifiers = parseClassModifiers(ctx.classModifiers())
        classNode.syntheticPublic = (classNode.modifiers & Opcodes.ACC_SYNTHETIC) != 0
        classNode.modifiers &= ~Opcodes.ACC_SYNTHETIC // FIXME Magic with syntetic modifier.

        parseMembers(classNode, ctx.classMember())
    }

    def parseMembers(ClassNode classNode, List<GroovyParser.ClassMemberContext> ctx) {
        for (member in ctx) {
            def memberContext = member.children[0]
            assert memberContext instanceof GroovyParser.ConstructorDeclarationContext ||
                    memberContext instanceof GroovyParser.MethodDeclarationContext ||
                    memberContext instanceof GroovyParser.FieldDeclarationContext

            // This inspection is suppressed cause I use Runtime multimethods dispatching mechanics of Groovy.
            //noinspection GroovyAssignabilityCheck
            parseMember(classNode, memberContext)
        }
    }

    static def parseMember(ClassNode classNode, GroovyParser.ConstructorDeclarationContext ctx) {
        int modifiers = ctx.VISIBILITY_MODIFIER() ? parseVisibilityModifiers(ctx.VISIBILITY_MODIFIER()) : Opcodes.ACC_PUBLIC

        def constructorNode = classNode.addConstructor(modifiers, parseParameters(ctx.argumentDeclarationList()), [] as ClassNode[], new BlockStatement())
        setupNodeLocation(constructorNode, ctx)
        constructorNode.syntheticPublic = ctx.VISIBILITY_MODIFIER() == null
    }

    def parseMember(ClassNode classNode, GroovyParser.MethodDeclarationContext ctx) {
        int modifiers = parseVisibilityModifiers(ctx.VISIBILITY_MODIFIER(), Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC)
        modifiers |= parseModifier(ctx.KW_STATIC(), Opcodes.ACC_STATIC)
        modifiers |= parseModifier(ctx.KW_ABSTRACT(), Opcodes.ACC_ABSTRACT)
        modifiers |= parseModifier(ctx.KW_FINAL(), Opcodes.ACC_FINAL)
        modifiers |= parseModifier(ctx.KW_NATIVE(), Opcodes.ACC_NATIVE)
        modifiers |= parseModifier(ctx.KW_SYNCHRONIZED(), Opcodes.ACC_SYNCHRONIZED)
        modifiers |= parseModifier(ctx.KW_TRANSIENT(), Opcodes.ACC_TRANSIENT)
        modifiers |= parseModifier(ctx.KW_VOLATILE(), Opcodes.ACC_VOLATILE)

        def statement = new BlockStatement()

        def params = parseParameters(ctx.argumentDeclarationList())
        def methodNode = classNode.addMethod(ctx.IDENTIFIER().text, modifiers, parseTypeDeclaration(ctx.typeDeclaration()), params, [] as ClassNode[], statement)
        setupNodeLocation(methodNode, ctx)
        methodNode.syntheticPublic = (methodNode.modifiers & Opcodes.ACC_SYNTHETIC) != 0
        methodNode.modifiers &= ~Opcodes.ACC_SYNTHETIC // FIXME Magic with syntetic modifier.
    }

    def parseMember(ClassNode classNode, GroovyParser.FieldDeclarationContext ctx) {
        int modifiers = parseVisibilityModifiers(ctx.VISIBILITY_MODIFIER(), Opcodes.ACC_PRIVATE) // FIXME Why?
        modifiers |= parseModifier(ctx.KW_STATIC(), Opcodes.ACC_STATIC)
        modifiers |= parseModifier(ctx.KW_ABSTRACT(), Opcodes.ACC_ABSTRACT)
        modifiers |= parseModifier(ctx.KW_FINAL(), Opcodes.ACC_FINAL)
        modifiers |= parseModifier(ctx.KW_NATIVE(), Opcodes.ACC_NATIVE)
        modifiers |= parseModifier(ctx.KW_SYNCHRONIZED(), Opcodes.ACC_SYNCHRONIZED)
        modifiers |= parseModifier(ctx.KW_TRANSIENT(), Opcodes.ACC_TRANSIENT)
        modifiers |= parseModifier(ctx.KW_VOLATILE(), Opcodes.ACC_VOLATILE)

        def fieldNode = classNode.addField(ctx.IDENTIFIER().text, modifiers, parseTypeDeclaration(ctx.typeDeclaration()), null)
        setupNodeLocation(fieldNode, ctx)
        fieldNode.synthetic = modifiers & Opcodes.ACC_PUBLIC //modifiers (ctx?.typeDeclaration()?.KW_DEF() as boolean) // TODO what it means?
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Utility methods.
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static ClassNode parseTypeDeclaration(GroovyParser.TypeDeclarationContext ctx) {
        !ctx || ctx.KW_DEF() ? ClassHelper.OBJECT_TYPE : setupNodeLocation(ClassHelper.make(ctx.IDENTIFIER().text), ctx)
    }

    static Parameter[] parseParameters(GroovyParser.ArgumentDeclarationListContext ctx) {
        ctx.argumentDeclaration().collect {
            setupNodeLocation(new Parameter(parseTypeDeclaration(it.typeDeclaration()), it.IDENTIFIER().text), it)
        }
    }


    /**
     * Sets location(lineNumber, colNumber, lastLineNumber, lastColumnNumber) for node using standard context information.
     * Note: this method is implemented to be closed over ASTNode. It returns same node as it received in arguments.
     * @param astNode Node to be modified.
     * @param ctx Context from which information is obtained.
     * @return Modified astNode.
     */
    static <T extends ASTNode> T setupNodeLocation(T astNode, ParserRuleContext ctx) {
        astNode.lineNumber = ctx.start.line
        astNode.columnNumber = ctx.start.charPositionInLine + 1
        astNode.lastLineNumber = ctx.stop.line
        astNode.lastColumnNumber = ctx.stop.charPositionInLine + 1 + ctx.stop.text.length()
        astNode
    }

    int parseClassModifiers(@NotNull GroovyParser.ClassModifiersContext ctx) {
        int modifier = parseVisibilityModifiers(ctx.VISIBILITY_MODIFIER(), Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC);
        modifier |= parseModifier(ctx.KW_ABSTRACT(), Opcodes.ACC_ABSTRACT)
        modifier |= parseModifier(ctx.KW_FINAL(), Opcodes.ACC_FINAL)
        modifier |= parseModifier(ctx.KW_STATIC(), Opcodes.ACC_STATIC)
        modifier |= parseModifier(ctx.KW_STRICTFP(), Opcodes.ACC_STRICT)
        modifier
    }

    int parseModifier(Collection<TerminalNode> nodes, int opcode) {
        if (!nodes)
            return 0

        if (nodes.size() > 1) {
            def modifier = nodes[1].symbol

            def line = modifier.line
            def col = modifier.charPositionInLine + 1
            sourceUnit.addError(new SyntaxException("Cannot repeat modifier: $modifier.text at line: $line column: $col. File: $sourceUnit.name", line, col))
        }
        opcode
    }

    void reportError(String text, int line, int col) {
        sourceUnit.addError(new SyntaxException(text, line, col))
    }

    static int parseVisibilityModifiers(TerminalNode modifier) {
        assert modifier.symbol.type == GroovyLexer.VISIBILITY_MODIFIER
        switch (modifier.symbol.text) {
            case "public": Opcodes.ACC_PUBLIC; break
            case "private": Opcodes.ACC_PRIVATE; break
            case "protected": Opcodes.ACC_PROTECTED; break
            default: throw new AssertionError("$modifier.symbol.text is not a valid visibility modifier!")
        }
    }

    int parseVisibilityModifiers(List<TerminalNode> modifiers, int defaultValue) {
        if (!modifiers)
            return defaultValue

        if (modifiers.size() > 1) {
            def modifier = modifiers[1].symbol

            def line = modifier.line
            def col = modifier.charPositionInLine + 1

            reportError("Cannot specify modifier: $modifier.text when access scope has already been defined at line: $line column: $col. File: $sourceUnit.name", line, col)
        }

        parseVisibilityModifiers(modifiers[0])
    }

}
