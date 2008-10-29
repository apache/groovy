
import java.security.CodeSource

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*

import static org.codehaus.groovy.syntax.Types.*

import org.codehaus.groovy.classgen.*

import org.codehaus.groovy.control.*
import org.codehaus.groovy.control.CompilationUnit.PrimaryClassNodeOperation
import org.codehaus.groovy.control.messages.ExceptionMessage
import org.codehaus.groovy.control.CompilationUnit.SourceUnitOperation

/**
 * The arithmetic shell is similar to a GroovyShell in that it can evaluate text as
 * code and return a result. It is not a subclass of GroovyShell because it does not widen
 * the contract of GroovyShell, instead it narrows it. Using one of these shells like a
 * GroovyShell would result in many runtime errors.
 *
 * @author Hamlet D'Arcy (hamletdrc@gmail.com)
 * @author Guillaume Laforge
 */
class ArithmeticShell {

    /**
     * Compiles the text into a Groovy object and then executes it, returning the result.
     * @param text
     *       the script to evaluate typed as a string
     * @throws SecurityException
     *       most likely the script is doing something other than arithmetic
     * @throws IllegalStateException
     *       if the script returns something other than a number
     */
    Number evaluate(String text) {
        try {
            SecureArithmeticClassLoader loader = new SecureArithmeticClassLoader()
            Class clazz = loader.parseClass(text)
            Script script = (Script)clazz.newInstance();
            Object result = script.run()
            if (!(result instanceof Number)) throw new IllegalStateException("Script returned a non-number: $result");
            return (Number)result
        } catch (SecurityException ex) {
            throw new SecurityException("Could not evaluate script: $text", ex)
        } catch (MultipleCompilationErrorsException mce) {
            //this allows compilation errors to be seen by the user       
            mce.errorCollector.errors.each {
                if (it instanceof ExceptionMessage && it.cause instanceof SecurityException) {
                    throw it.cause
                }
            }
            throw mce
        }
    }
}


/**
 * This classloader hooks the security enforcer into the compilation process.
 */
class SecureArithmeticClassLoader extends GroovyClassLoader {

    protected CompilationUnit createCompilationUnit(CompilerConfiguration config, CodeSource codeSource) {
        CompilationUnit cu = super.createCompilationUnit(config, codeSource)
        // adding static imports should be done in a phase before the StaticImportVisitor kicks in
        // (CONVERSION before SEMANTIC_ANALYSIS)
        cu.addPhaseOperation(new StaticImportOperation(), Phases.CONVERSION)
        // wiring into the SEMANTIC_ANALYSIS phase will provide more type information that the CONVERSION phase.
        cu.addPhaseOperation(new SecurityFilteringNodeOperation(), Phases.SEMANTIC_ANALYSIS)
        return cu
    }
}


/**
 * Transparently add <code>import static java.lang.Math.*</code>
 * to give access to constants like PI or E, and to methods like cos(), sin(), etc. 
 */
private class StaticImportOperation extends SourceUnitOperation {

    public void call(SourceUnit source) {
        ModuleNode ast = source.getAST()

        // add a static import for java.lang.Math
        ast.addStaticImportClass('java.lang.Math', ClassHelper.make(java.lang.Math))
    }
}


/**
 * This operation will force only arithmetic operations to be compiled.
 */
private class SecurityFilteringNodeOperation extends PrimaryClassNodeOperation {

    void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
        ModuleNode ast = source.getAST()

        if (ast.getImportPackages())        { throw new SecurityException("Package import statements are not allowed.") }
        if (ast.getImports())               { throw new SecurityException("Import statements are not allowed.") }
        if (ast.getStaticImportAliases())   { throw new SecurityException("Static import aliases are not allowed.") }
        if (ast.getStaticImportFields())    { throw new SecurityException("Static field import statements are not allowed.") }

        if (ast.getStaticImportClasses() != ['java.lang.Math': ClassHelper.make(java.lang.Math)]) {
            throw new SecurityException("Only java.lang.Math is allowed for static imports.")
        }

        // do not allow package names
        if (ast.getPackageName()) { throw new SecurityException("Package names are not allowed.") }

        // do not allow method definitions
        if (ast.getMethods()) { throw new SecurityException("Method definition is not allowed.") }

        // enforce arithmetic only expressions
        ast.getStatementBlock().visit(new ArithmeticExpressionEnforcer())
    }
}


/**
 * This code visitor throws a SecurityException if anything but an arithmetic expression is found.
 * Normally, it would be easier to extend CodeVisitorSupport because that provides all the base
 * methods to perform visits on the syntax tree and would make upgrading to newer versions of
 * Groovy easier. However, that would mean that any new syntax in Groovy would be supported by
 * this shell by default which is undesireable in this case. For instance, if a new metaprogramming
 * trick gets introduced, this shell should _not_ allow it to be accessed without considerationg
 * from the developer.
 */
private class ArithmeticExpressionEnforcer implements GroovyCodeVisitor {

    private static final allowedTokens = [
        PLUS,
        MINUS,
        MULTIPLY,
        DIVIDE,
        MOD,
        POWER,
        PLUS_PLUS,
        MINUS_MINUS,
        COMPARE_EQUAL,
        COMPARE_NOT_EQUAL,
        COMPARE_LESS_THAN,
        COMPARE_LESS_THAN_EQUAL,
        COMPARE_GREATER_THAN,
        COMPARE_GREATER_THAN_EQUAL, 
    ].asImmutable()

    private static final allowedConstantTypes = [
        Integer,
        Float,
        Long,
        Double,
        BigDecimal
    ].asImmutable()

    private static final allowedReceivers = [
        Math,
        Integer,
        Float,
        Double,
        Long,
        BigDecimal
    ].asImmutable()

    private static final allowedStaticImports = [
        Math,
    ].asImmutable()

    /**
     * Block statements are allowed and traversal continues.
     */
    void visitBlockStatement(BlockStatement statement) {
        //keep walking...
        statement.getStatements().each { ASTNode child ->
            child.visit(this)
        }
    }

    /**
     * Expression statements must continue traversal.
     */
    void visitExpressionStatement(ExpressionStatement statement) {
        Expression exp = statement.getExpression()
        exp.visit(this)    //keep walking...
    }

    /**
     * Binary expressions must have a numeric token (+, -, /, etc) and continue traversal.
     */
    void visitBinaryExpression(BinaryExpression expression) {
        if (!allowedTokens.contains(expression.getOperation().getType())) {
            throw new SecurityException("Unsupported token: ${expression.getOperation().getText() }")
        }
        expression.getLeftExpression().visit(this)
        expression.getRightExpression().visit(this)
    }

    /**
     * Constants may be of only numeric core types.
     */
    void visitConstantExpression(ConstantExpression expression) {
        Object value = expression.getValue()
        if (!(allowedConstantTypes.contains(value.getClass()))) {
            throw new SecurityException("""Unsupported constant type: ${ value.getClass() }, value: $value""")
        }
    }

    /**
     * Method calls may only be invoked on a few core types
     */
    void visitMethodCallExpression(MethodCallExpression expression) {
        Expression receiver = expression.getObjectExpression()
        if (!(receiver instanceof ClassExpression)) {
            throw new SecurityException("Unsupported method call: $receiver")
        }
        if (!allowedReceivers.contains(receiver.getType().getTypeClass())) {
            throw new SecurityException("Unsupported method receiver: ${receiver.getText()}")
        }
        expression.getArguments().visit(this)    //enforce arguments
    }

    /**
     * Static method calls may only be invoked on java.lang.Math, to access math functions like cos(), sin(), etc.
     */
    void visitStaticMethodCallExpression(StaticMethodCallExpression staticMethodCallExpression) {
        if (!allowedStaticImports.contains(staticMethodCallExpression.ownerType.getTypeClass())) {
            throw new SecurityException("Static method call expressions forbidden in arithmetic shell.")
        }
    }

    /**
     * Argument expressions must continue to be processed
     */
    void visitArgumentlistExpression(ArgumentListExpression expression) {
        expression.getExpressions().each { it.visit(this) }
    }

    /**
     * Property access allowed only on a few core Java types.
     */
    void visitPropertyExpression(PropertyExpression expression) {
        Expression receiver = expression.getObjectExpression()
        if (!(receiver instanceof ClassExpression)) {
            throw new SecurityException("Unsupported method call: $receiver")
        }
        if (!allowedReceivers.contains(receiver.getType().getTypeClass())) {
            throw new SecurityException("Unsupported method receiver: ${receiver.getText()}")
        }
    }

    /**
     * The unary minus is allowed.
     */
    void visitUnaryMinusExpression(UnaryMinusExpression expression) {
        expression.getExpression().visit(this)
    }

    /**
     * The unary plus operation is allowed.
     */
    void visitUnaryPlusExpression(UnaryPlusExpression expression) {
        expression.getExpression().visit(this)
    }

    /**
     * Prefix operations like ++ and -- are allowed.
     */
    void visitPrefixExpression(PrefixExpression expression) {
        if (!allowedTokens.contains(expression.getOperation().getType())) {
            throw new SecurityException("Unsupported token: ${expression.getOperation().getText() }")
        }
        expression.getExpression().visit(this)
    }

    /**
     * Postfix operations like ++ and -- are allowed.
     */
    void visitPostfixExpression(PostfixExpression expression) {
        if (!allowedTokens.contains(expression.getOperation().getType())) {
            throw new SecurityException("Unsupported token: ${expression.getOperation().getText() }")
        }
        expression.getExpression().visit(this)
    }

    /**
     * Ternary exprîessions are allowed as long as they are arithmetic
     */
    void visitTernaryExpression(TernaryExpression expression) {
        expression.getBooleanExpression().visit(this)
        expression.getTrueExpression().visit(this)
        expression.getFalseExpression().visit(this)                        
    }

    /**
     * Elvis operator is allowed
     * Delegates to the ternary expression visitor method, as Elvis is just a shortcut.
     */
    void visitShortTernaryExpression(ElvisOperatorExpression elvisOperatorExpression) {
        visitTernaryExpression(elvisOperatorExpression)
    }

    /**
     * Boolean expressions are allowed.
     */
    void visitBooleanExpression(BooleanExpression expression) {
        expression.getExpression().visit(this)
    }

    void visitForLoop(ForStatement forStatement) {
        throw new SecurityException("For statements forbidden in arithmetic shell.")
    }
    void visitWhileLoop(WhileStatement whileStatement) {
        throw new SecurityException("While statements forbidden in arithmetic shell.")
    }
    void visitDoWhileLoop(DoWhileStatement doWhileStatement) {
        throw new SecurityException("Do/while statements forbidden in arithmetic shell.")
    }
    void visitIfElse(IfStatement ifStatement) {
        throw new SecurityException("If statements forbidden in arithmetic shell.")
    }
    void visitReturnStatement(ReturnStatement returnStatement) {
        throw new SecurityException("Return statements forbidden in arithmetic shell.")
    }
    void visitAssertStatement(AssertStatement assertStatement) {
        throw new SecurityException("Assert statements forbidden in arithmetic shell.")
    }
    void visitTryCatchFinally(TryCatchStatement tryCatchStatement) {
        throw new SecurityException("Try/Catch statements forbidden in arithmetic shell.")
    }
    void visitSwitch(SwitchStatement switchStatement) {
        throw new SecurityException("Switch statements forbidden in arithmetic shell.")
    }
    void visitCaseStatement(CaseStatement caseStatement) {
        throw new SecurityException("Case statements forbidden in arithmetic shell.")
    }
    void visitBreakStatement(BreakStatement breakStatement) {
        throw new SecurityException("Break statements forbidden in arithmetic shell.")
    }
    void visitContinueStatement(ContinueStatement continueStatement) {
        throw new SecurityException("Continue statements forbidden in arithmetic shell.")
    }
    void visitThrowStatement(ThrowStatement    throwStatement) {
        throw new SecurityException("Throw statements forbidden in arithmetic shell.")
    }
    void visitSynchronizedStatement(SynchronizedStatement synchronizedStatement) {
        throw new SecurityException("Synchronized statements forbidden in arithmetic shell.")
    }
    void visitCatchStatement(CatchStatement catchStatement) {
        throw new SecurityException("Catch statements forbidden in arithmetic shell.")
    }
    void visitConstructorCallExpression(ConstructorCallExpression constructorCallExpression) {
        throw new SecurityException("Constructor call expressions forbidden in arithmetic shell.")
    }
    void visitClosureExpression(ClosureExpression closureExpression) {
        throw new SecurityException("Closure expressions forbidden in arithmetic shell.")
    }
    void visitTupleExpression(TupleExpression tupleExpression) {
        throw new SecurityException("Tuple expressions forbidden in arithmetic shell.")
    }
    void visitMapExpression(MapExpression mapExpression) {
        throw new SecurityException("Map expressions forbidden in arithmetic shell.")
    }
    void visitMapEntryExpression(MapEntryExpression mapEntryExpression) {
        throw new SecurityException("Map entry expressions forbidden in arithmetic shell.")
    }
    void visitListExpression(ListExpression listExpression) {
        throw new SecurityException("List expressions forbidden in arithmetic shell.")
    }
    void visitRangeExpression(RangeExpression rangeExpression) {
        throw new SecurityException("Range expressions forbidden in arithmetic shell.")
    }
    void visitAttributeExpression(AttributeExpression attributeExpression) {
        throw new SecurityException("Attribute expressions forbidden in arithmetic shell.")
    }
    void visitFieldExpression(FieldExpression fieldExpression) {
        throw new SecurityException("Field expressions forbidden in arithmetic shell.")
    }
    void visitMethodPointerExpression(MethodPointerExpression methodPointerExpression) {
        throw new SecurityException("Method pointer expressions forbidden in arithmetic shell.")
    }
    void visitVariableExpression(VariableExpression variableExpression) {
        throw new SecurityException("Variable expressions forbidden in arithmetic shell.")
    }
    void visitDeclarationExpression(DeclarationExpression declarationExpression) {
        throw new SecurityException("Declaraion expressions forbidden in arithmetic shell.")
    }
    void visitRegexExpression(RegexExpression regexExpression) {
        throw new SecurityException("Regex expressions forbidden in arithmetic shell.")
    }
    void visitGStringExpression(GStringExpression gStringExpression) {
        throw new SecurityException("Groovy String expressions forbidden in arithmetic shell.")
    }
    void visitArrayExpression(ArrayExpression arrayExpression) {
        throw new SecurityException("Array expressions forbidden in arithmetic shell.")
    }
    void visitSpreadExpression(SpreadExpression spreadExpression) {
        throw new SecurityException("Spread expressions forbidden in arithmetic shell.")
    }
    void visitSpreadMapExpression(SpreadMapExpression spreadMapExpression) {
        throw new SecurityException("Spread map expressions forbidden in arithmetic shell.")
    }
    void visitNotExpression(NotExpression notExpression) {
        throw new SecurityException("Not expressions forbidden in arithmetic shell.")
    }
    void visitBitwiseNegationExpression(BitwiseNegationExpression bitwiseNegationExpression) {
        throw new SecurityException("Bitwise Negation expressions forbidden in arithmetic shell.")
    }
    void visitCastExpression(CastExpression castExpression) {
        throw new SecurityException("Cast expressions forbidden in arithmetic shell.")
    }
    void visitClosureListExpression(ClosureListExpression closureListExpression) {
        throw new SecurityException("Closure expressions forbidden in arithmetic shell.")
    }
    void visitBytecodeExpression(BytecodeExpression bytecodeExpression) {
        throw new SecurityException("Bytecode expressions forbidden in arithmetic shell.")
    }
    void visitClassExpression(ClassExpression classExpression) {
        throw new SecurityException("Class expressions forbidden in arithmetic shell.")
    }
}
